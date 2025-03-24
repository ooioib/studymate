package org.codenova.studymate.controller;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.*;
import org.codenova.studymate.model.vo.PostMeta;
import org.codenova.studymate.model.vo.StudyGroupWithCreator;
import org.codenova.studymate.repository.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller  // Spring MVC 컨트롤러로 등록
@RequestMapping("/study")  // URL 경로 "/study"로 시작하는 요청 처리
@AllArgsConstructor  // 생성자를 자동으로 생성하여 의존성 주입을 간결하게 처리
public class StudyController {
    private StudyGroupRepository studyGroupRepository;  // 스터디 그룹 데이터베이스 접근 객체
    private StudyMemberRepository studyMemberRepository;  // 스터디 멤버 데이터베이스 접근 객체
    private UserRepository userRepository;  // 사용자 데이터베이스 접근 객체
    private PostRepository postRepository;
    private AvatarRepository avatarRepository;
    private PostReactionRepository postReactionRepository;


    // =======================================================================================
    // 스터디 그룹 생성 핸들러
    // 사용자가 스터디 그룹을 만들기 위해 접근하는 화면을 반환
    @RequestMapping("/create")
    public String createHandle() {
        return "study/create";  // study/create.jsp 페이지 반환
    }

    // =======================================================================================
    // 스터디 그룹 생성 처리 핸들러
    // 사용자 입력 데이터를 바탕으로 새로운 스터디 그룹을 생성
    @Transactional  // 트랜잭션 단위로 실행
    @RequestMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute StudyGroup studyGroup,
                                     @SessionAttribute("user") User user) {

        // 랜덤 ID 생성 (UUID의 마지막 8자리 사용)
        String randomId = UUID.randomUUID().toString().substring(24);

        // 그룹 정보 설정
        studyGroup.setId(randomId);  // ID 할당
        studyGroup.setCreatorId(user.getId());  // 현재 로그인한 사용자를 그룹 생성자로 설정

        // 그룹을 데이터베이스에 저장
        studyGroupRepository.create(studyGroup);

        // 그룹 생성자가 자동으로 '리더' 역할로 가입되도록 설정
        StudyMember studyMember = new StudyMember();
        studyMember.setUserId(user.getId());  // 현재 로그인한 사용자 ID
        studyMember.setGroupId(studyGroup.getId());  // 생성한 그룹 ID
        studyMember.setRole("리더");  // 역할: 리더

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

        // 검색어를 포함하는 그룹 찾기 (이름 또는 목표에서 검색)
        List<StudyGroup> result = studyGroupRepository.findByNameLikeOrGoalLike("%" + wordValue + "%");

        // 검색 결과를 StudyGroupWithCreator 형태로 변환
        List<StudyGroupWithCreator> convertedResult = new ArrayList<>();
        for (StudyGroup one : result) {
            User found = userRepository.findById(one.getCreatorId());  // 생성자 정보 찾기
            StudyGroupWithCreator c = StudyGroupWithCreator.builder().group(one).creator(found).build();
            convertedResult.add(c);
        }

        // 모델에 검색 결과 추가
        model.addAttribute("count", convertedResult.size());
        model.addAttribute("result", convertedResult);

        return "study/search";  // 검색 결과 페이지 반환
    }

    // =======================================================================================
    // 스터디 그룹 상세 핸들러
    // 사용자가 특정 그룹을 조회할 때, 현재 로그인한 사용자의 가입 상태를 함께 확인하여 보여줌
    @RequestMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model,
                             @SessionAttribute("user") User user) {

        // 그룹 정보 조회
        StudyGroup group = studyGroupRepository.findById(id);
        if (group == null) {
            return "redirect:/";  // 그룹이 존재하지 않으면 홈으로 이동
        }

        // 현재 사용자의 가입 상태 확인
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);
        map.put("userId", user.getId());
        StudyMember status = studyMemberRepository.findByUserIdAndGroupId(map);

        // 가입 상태에 따라 다른 값을 모델에 추가
        if (status == null) {
            model.addAttribute("status", "NOT_JOINED");  // 가입하지 않음
        } else if (status.getJoinedAt() == null) {
            model.addAttribute("status", "PENDING");  // 승인 대기 중
        } else if (status.getRole().equals("멤버")) {
            model.addAttribute("status", "MEMBER");  // 일반 멤버
        } else {
            model.addAttribute("status", "LEADER");  // 리더
        }

        // 그룹 정보를 모델에 추가하고 페이지 반환
        model.addAttribute("group", group);

        List<Post> posts = postRepository.findByGroupId(id);

        List<PostMeta> postMetas = new ArrayList<>();

        PrettyTime prettyTime = new PrettyTime();
        for (Post post : posts) {

            long b = Duration.between(post.getWroteAt(), LocalDateTime.now()).getSeconds();
            System.out.println(b);

            PostMeta cvt = PostMeta.builder()
                    .id(post.getId())
                    .content(post.getContent())
                    .writerName(userRepository.findById(post.getWriterId()).getName())
                    .writerAvatar(avatarRepository.findById(userRepository.findById(post.getWriterId()).getAvatarId()).getImageUrl())
                    .time(prettyTime.format(post.getWroteAt()))
                    .reactions(postReactionRepository.findByPostId(post.getId()))
                    .build();
            postMetas.add(cvt);
        }

        model.addAttribute("postMetas", postMetas);

        return "study/view";
    }

    // =======================================================================================
    // 스터디 그룹 가입 요청 핸들러
    @Transactional
    @RequestMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id,
                             @SessionAttribute("user") User user) {

        boolean exist = false;
        List<StudyMember> list = studyMemberRepository.findByUserId(user.getId());

        // 사용자가 이미 가입한 그룹인지 확인
        for (StudyMember one : list) {
            if (one.getGroupId().equals(id)) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            StudyMember member = StudyMember.builder().userId(user.getId()).groupId(id).role("멤버").build();
            StudyGroup group = studyGroupRepository.findById(id);

            if (group.getType().equals("공개")) {
                studyMemberRepository.createApproved(member); // 즉시 가입 승인
                studyGroupRepository.addMemberCountById(id);
            } else {
                studyMemberRepository.createPending(member); // 가입 요청 대기 상태로 저장
            }
        }

        return "redirect:/study/" + id;
    }

    // =======================================================================================
    // 그룹 탈퇴 핸들러
    @RequestMapping("/{groupId}/leave")
    public String leaveHandle(@PathVariable("groupId") String groupId,
                              @SessionAttribute("user") User user, Model model) {

        String userId = user.getId();
        Map<String, String> map = Map.of("groupId", groupId, "userId", userId);

        // 사용자가 가입한 그룹 정보 조회
        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);

        // 해당 사용자의 가입 정보를 삭제
        studyMemberRepository.deleteById(found.getId());

        // 그룹의 멤버 수 감소 처리
        studyGroupRepository.subtractMemberCountById(groupId);

        return "redirect:/"; // 메인 페이지로 리다이렉트
    }

    // =======================================================================================
    // 가입 요청 철회 핸들러
    @RequestMapping("/{groupId}/cancel")
    public String cancelHandle(@PathVariable("groupId") String groupId,
                               @SessionAttribute("user") User user, Model model) {

        String userId = user.getId();
        Map<String, String> map = Map.of("groupId", groupId, "userId", userId);

        // 가입 요청 정보 조회
        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);

        // 가입 요청이 승인되지 않은 상태라면 삭제 가능
        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            studyMemberRepository.deleteById(found.getId());
        }

        return "redirect:/study/" + groupId; // 해당 그룹 페이지로 이동
    }

    // =======================================================================================
    // 그룹 해산 핸들러 (그룹 생성자만 가능)
    @Transactional
    @RequestMapping("/{groupId}/remove")
    public String removeHandle(@PathVariable("groupId") String groupId,
                               @SessionAttribute("user") User user) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId);

        // 그룹이 존재하고, 현재 사용자가 생성자라면 그룹 삭제 수행
        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {

            // 해당 그룹의 모든 멤버 데이터 삭제
            studyMemberRepository.deleteByGroupId(groupId);

            // 그룹 자체 삭제
            studyGroupRepository.deleteById(groupId);

            return "redirect:/"; // 메인 페이지로 리다이렉트
        } else {
            return "redirect:/study/" + groupId; // 그룹 페이지로 이동
        }
    }

    // =======================================================================================
    // 가입 요청 승인 핸들러 (그룹 생성자만 가능)
    @Transactional
    @RequestMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") User user) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId);

        // 그룹이 존재하고, 현재 사용자가 생성자인 경우에만 승인 가능
        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {
            StudyMember found = studyMemberRepository.findByUserIdAndGroupId(
                    Map.of("userId", targetUserId, "groupId", groupId)
            );

            if (found != null) {
                // 가입 승인 처리
                studyMemberRepository.updateJoinedAtById(found.getId());

                // 그룹 멤버 수 증가 처리
                studyGroupRepository.addMemberCountById(groupId);
            }
        }

        return "redirect:/study/" + groupId; // 그룹 페이지로 이동
    }

    // =======================================================================================
    // 그룹 내 새 글 등록 핸들러
    @RequestMapping("/{groupId}/post")
    public String postHandle(@PathVariable("groupId") String id,
                             @ModelAttribute Post post,
                             @SessionAttribute("user") User user) {

        post.setWriterId(user.getId());
        post.setWroteAt(LocalDateTime.now());
        //  post.setGroupId();
        //  post.setContent();
        postRepository.create(post);

        return "redirect:/study/" + id;
    }

    // =======================================================================================
    // 글에 감정 남기기 요청 처리 핸들
    @RequestMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@ModelAttribute PostReaction postReaction,
                                     @SessionAttribute("user") User user) {

        PostReaction found = postReactionRepository.findByWriterIdAndPostId(Map.of("writerId", user.getId(), "postId", postReaction.getPostId()));

        if (found == null) {
            postReaction.setWriterId(user.getId());
            postReactionRepository.create(postReaction);

        } else {
    //      postReactionRepository.delecteById(found.getId());
    //      postReactionRepository.create(postReaction);
        }

        return "redirect:/study/" + postReaction.getGroupId();
    }
}