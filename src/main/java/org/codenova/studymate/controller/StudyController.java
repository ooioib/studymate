package org.codenova.studymate.controller;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.*;
import org.codenova.studymate.model.query.UserWithAvatar;
import org.codenova.studymate.model.vo.PostMeta;
import org.codenova.studymate.model.vo.StudyGroupWithCreator;
import org.codenova.studymate.repository.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller  // Spring MVC 컨트롤러로 등록
@RequestMapping("/study")  // URL 경로 "/study"로 시작하는 요청 처리
@AllArgsConstructor  // 생성자를 자동으로 생성하여 의존성 주입을 간결하게 처리
public class StudyController {
    private StudyGroupRepository studyGroupRepository;  // 스터디 그룹 관련 DB 접근 객체
    private StudyMemberRepository studyMemberRepository;  // 스터디 멤버 관련 DB 접근 객체
    private UserRepository userRepository;  // 사용자 관련 DB 접근 객체
    private PostRepository postRepository;  // 게시글 관련 DB 접근 객체
    private AvatarRepository avatarRepository;  // 아바타 이미지 관련 DB 접근 객체
    private PostReactionRepository postReactionRepository;  // 게시글 반응 관련 DB 접근 객체

    // =======================================================================================
    // 스터디 그룹 생성 핸들러
    // 사용자가 스터디 그룹을 만들기 위해 접근하는 화면을 반환
    @RequestMapping("/create")
    public String createHandle() {
        System.out.println("create...");
        return "study/create";   // study/create.jsp 페이지 반환
    }

    // =======================================================================================
    // 스터디 그룹 생성 처리 핸들러
    // 사용자 입력 데이터를 바탕으로 새로운 스터디 그룹을 생성
    @Transactional
    @RequestMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute StudyGroup studyGroup,
                                     @SessionAttribute("user") UserWithAvatar user) {

        // 랜덤 ID 생성 (UUID의 마지막 8자리 사용)
        String randomId = UUID.randomUUID().toString().substring(24);

        // 그룹 정보 설정
        studyGroup.setId(randomId);   // ID 할당
        studyGroup.setCreatorId(user.getId());   // 현재 로그인한 사용자를 그룹 생성자로 설정

        // 그룹을 데이터베이스에 저장
        studyGroupRepository.create(studyGroup);

        // 그룹 생성자가 자동으로 '리더' 역할로 가입되도록 설정
        StudyMember studyMember = new StudyMember();
        studyMember.setUserId(user.getId());   // 현재 로그인한 사용자 ID
        studyMember.setGroupId(studyGroup.getId());   // 생성한 그룹 ID
        studyMember.setRole("리더");   // 역할: 리더

        // 승인된 상태로 그룹 멤버 등록
        studyMemberRepository.createApproved(studyMember);

        // 그룹 멤버 수 증가
        studyGroupRepository.addMemberCountById(studyGroup.getId());

        // 생성된 그룹 상세 페이지로 이동
        return "redirect:/study/" + randomId;
    }

    // =======================================================================================
    // 스터디 그룹 검색 핸들러
    // 사용자 입력 검색어를 바탕으로 그룹을 찾아 결과 반환
    @RequestMapping("/search")
    public String searchHandle(@RequestParam("word") Optional<String> word, Model model) {
        if (word.isEmpty()) {     // 검색어가 없으면
            return "redirect:/";  // 홈으로 이동
        }

        // 검색어 가져오기
        String wordValue = word.get();

        // 그룹 이름 또는 목표에 검색어가 포함된 그룹 목록 조회
        List<StudyGroup> result = studyGroupRepository.findByNameLikeOrGoalLike("%" + wordValue + "%");

        // 검색 결과를 StudyGroupWithCreator 객체로 변환
        List<StudyGroupWithCreator> convertedResult = new ArrayList<>();

        for (StudyGroup one : result) {
            User found = userRepository.findById(one.getCreatorId());  // 생성자 정보 찾기
            StudyGroupWithCreator c = StudyGroupWithCreator.builder().group(one).creator(found).build();
            convertedResult.add(c);
        }

        // 모델에 검색 결과 추가
        System.out.println("search count : " + result.size());
        model.addAttribute("count", convertedResult.size());  // 검색 결과 개수 추가
        model.addAttribute("result", convertedResult);  // 검색 결과 추가

        // 검색 결과 페이지 반환
        return "study/search";
    }

    // =======================================================================================
    // 스터디 그룹 상세 핸들러
    // 사용자가 특정 그룹을 조회할 때, 현재 로그인한 사용자의 가입 상태를 함께 확인하여 보여줌
    @RequestMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model, @SessionAttribute("user") UserWithAvatar user) {

        // 그룹 정보 조회
        // 데이터베이스에서 해당 그룹 ID에 해당하는 그룹 정보를 가져옴
        StudyGroup group = studyGroupRepository.findById(id);
        if (group == null) {   // 그룹이 존재하지 않으면
            return "redirect:/";  // 홈으로 리디렉션
        }

        // 현재 로그인한 사용자의 가입 상태 확인
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);   // 조회할 그룹 ID
        map.put("userId", user.getId());   // 현재 로그인한 사용자 ID
        StudyMember status = studyMemberRepository.findByUserIdAndGroupId(map);

        // 사용자가 가입하지 않은 경우
        if (status == null) {
            model.addAttribute("status", "NOT_JOINED");  // 가입하지 않음
            // 사용자가 가입 신청했지만 아직 승인이 나지 않은 경우
        } else if (status.getJoinedAt() == null) {
            model.addAttribute("status", "PENDING");  // 승인 대기 중
            // 사용자가 일반 멤버로 가입된 경우
        } else if (status.getRole().equals("멤버")) {
            model.addAttribute("status", "MEMBER");  // 일반 멤버
            // 사용자가 그룹 리더인 경우
        } else {
            model.addAttribute("status", "LEADER");  // 리더
        }

        // 그룹 정보를 모델에 추가하고 페이지 반환
        model.addAttribute("group", group);

        // 해당 그룹의 게시글 목록 조회

        List<Post> posts = postRepository.findByGroupId(id);
        // 게시글 정보를 가공하여 저장할 리스트 생성
        List<PostMeta> postMetas = new ArrayList<>();

        // 작성 시간을 '몇 분 전', '몇 시간 전'과 같이 보기 좋게 변환
        PrettyTime prettyTime = new PrettyTime();
        for (Post post : posts) {

            PostMeta cvt = PostMeta.builder()
                    .id(post.getId())   // 게시글 ID
                    .content(post.getContent())   // 게시글 내용
                    .writerName(userRepository.findById(post.getWriterId()).getName())   // 작성자 이름
                    .writerAvatar(avatarRepository.findById(userRepository.findById(post.getWriterId()).getAvatarId()).getImageUrl())    // 작성자 프로필 이미지
                    .time(prettyTime.format(post.getWroteAt()))   // 변환된 시간
                    .reactions(postReactionRepository.countFeelingByPostId(post.getId()))   // 게시글에 대한 반응 정보
                    .build();

            // 변환된 정보를 리스트에 추가
            postMetas.add(cvt);
        }

        // 변환된 게시글 정보를 모델에 추가
        model.addAttribute("postMetas", postMetas);

        // "study/view" 페이지를 반환
        return "study/view";
    }

    // =======================================================================================
    // 스터디 그룹 가입 요청 핸들러
    @Transactional
    @RequestMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id, @SessionAttribute("user") UserWithAvatar user) {

        // 이미 가입한 그룹인지 여부를 확인할 변수
        boolean exist = false;

        // 사용자가 가입한 모든 그룹 목록을 가져옴
        List<StudyMember> list = studyMemberRepository.findByUserId(user.getId());

        // 사용자가 이미 가입한 그룹인지 확인
        for (StudyMember one : list) {
            if (one.getGroupId().equals(id)) {   // 현재 그룹 ID와 일치하는지 검사
                exist = true;   // 이미 가입한 경우 exist = true 설정
                break;   // 반복문 중단
            }
        }

        // 가입한 적이 없는 경우에만 가입 요청 처리
        if (!exist) {
            // 새 멤버 객체 생성 (기본 역할: "멤버")
            StudyMember member = StudyMember.builder().
                    userId(user.getId()).groupId(id).role("멤버").build();

            // 그룹 정보를 가져와서 공개/비공개 여부 확인
            StudyGroup group = studyGroupRepository.findById(id);
            // 그룹이 공개 상태라면
            if (group.getType().equals("공개")) {
                studyMemberRepository.createApproved(member); // 바로 승인된 멤버로 등록
                studyGroupRepository.addMemberCountById(id); // 그룹 멤버 수 증가
            }
            // 그룹이 비공개 상태라면
            else {
                studyMemberRepository.createPending(member); // 승인 대기 상태로 저장
            }
        }

        // 가입 요청 후, 해당 그룹 페이지로 이동
        return "redirect:/study/" + id;
    }


    // 탈퇴 요청 처리 핸들러
    @RequestMapping("/{groupId}/leave")
    public String leaveHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") UserWithAvatar user, Model model) {
        String userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);
        studyMemberRepository.deleteById(found.getId());

        studyGroupRepository.subtractMemberCountById(groupId);
        return "redirect:/";
    }

    // 신청 철회 요청 핸들러
    @RequestMapping("/{groupId}/cancel")
    public String cancelHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") UserWithAvatar user, Model model) {
        String userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);
        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            studyMemberRepository.deleteById(found.getId());
        }

        return "redirect:/study/" + groupId;
    }

    @Transactional
    @RequestMapping("/{groupId}/remove")
    public String removeHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") UserWithAvatar user) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId);

        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {
            studyMemberRepository.deleteByGroupId(groupId);
            studyGroupRepository.deleteById(groupId);
            return "redirect:/";
        } else {
            return "redirect:/study/" + groupId;
        }
    }

    @RequestMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") UserWithAvatar user) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId);


        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {
            StudyMember found = studyMemberRepository.findByUserIdAndGroupId(
                    Map.of("userId", targetUserId, "groupId", groupId)
            );

            if (found != null) {
                studyMemberRepository.updateJoinedAtById(found.getId());
                studyGroupRepository.addMemberCountById(groupId);
            }
        }

        return "redirect:/study/" + groupId;
    }

    // 그룹내 새글 등록
    @RequestMapping("/{groupId}/post")
    public String postHandle(@PathVariable("groupId") String id,
                             @ModelAttribute Post post,
                             @SessionAttribute("user") UserWithAvatar user) {

        post.setWriterId(user.getId());
        post.setWroteAt(LocalDateTime.now());

        postRepository.create(post);


        return "redirect:/study/" + id;
    }

    // =======================================================================================
    // 글에 감정 남기기 요청 처리 핸들
    @RequestMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@ModelAttribute PostReaction postReaction, @SessionAttribute("user") UserWithAvatar user) {


        // 사용자가 해당 게시글에 남긴 감정이 있는지 확인
        PostReaction found =
                postReactionRepository.findByWriterIdAndPostId(Map.of("writerId", user.getId(), "postId", postReaction.getPostId()));

        if (found != null) {   // 이미 감정이 남겨져 있을 경우
            postReactionRepository.deleteById(found.getId());    // 기존 감정을 삭제
        }

        // 그렇지 않으면 새 감정 추가
        postReaction.setWriterId(user.getId());   // 현재 로그인한 사용자의 ID를 설정
        postReactionRepository.create(postReaction);    // 새로운 감정을 데이터베이스에 저장

        return "redirect:/study/" + postReaction.getGroupId();
    }

    // =======================================================================================
    // 아바타 이미지 변경
    @ModelAttribute("user")
    public UserWithAvatar addUser(@SessionAttribute("user") UserWithAvatar user) {
        System.out.println("addUser...");
        return user;
    }
}
