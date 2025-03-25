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

import java.time.Duration;
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
    // 아바타 이미지 변경
    @ModelAttribute("user")
    public UserWithAvatar addUser(@SessionAttribute("user") UserWithAvatar user) {
        System.out.println("addUser");
        return user;
    }

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
                                     @SessionAttribute("user") UserWithAvatar user) {

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
        model.addAttribute("count", convertedResult.size());  // 검색 결과 개수 추가
        model.addAttribute("result", convertedResult);  // 검색 결과 추가


        return "study/search";  // 검색 결과 페이지 반환
    }

    // =======================================================================================
    // 스터디 그룹 상세 핸들러
    // 사용자가 특정 그룹을 조회할 때, 현재 로그인한 사용자의 가입 상태를 함께 확인하여 보여줌
    @RequestMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model,
                             @SessionAttribute("user") UserWithAvatar user) {

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

        // 게시글 리스트를 순회하면서 필요한 정보를 변환하여 postMetas 리스트에 추가
        for (Post post : posts) {

            // 작성된 시간과 현재 시간의 차이를 초 단위로 계산
            long b = Duration.between(post.getWroteAt(), LocalDateTime.now()).getSeconds();
            System.out.println(b);

            // 게시글 작성자의 정보 조회
            User writer = userRepository.findById(post.getWriterId());

            // 작성자의 프로필 이미지 조회
            String writerAvatar = avatarRepository.findById(writer.getAvatarId()).getImageUrl();

            // 게시글에 대한 반응(좋아요, 싫어요 등) 조회
            List<PostReaction> reactions = postReactionRepository.findByPostId(post.getId());

            // PostMeta 객체를 생성하여 변환된 게시글 정보를 저장
            PostMeta cvt = PostMeta.builder()
                    .id(post.getId())                         // 게시글 ID
                    .content(post.getContent())               // 게시글 내용
                    .writerName(writer.getName())             // 작성자 이름
                    .writerAvatar(writerAvatar)               // 작성자 프로필 이미지
                    .time(prettyTime.format(post.getWroteAt())) // 변환된 시간
                    .reactions(reactions)                     // 게시글에 대한 반응 정보
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
    public String joinHandle(@PathVariable("id") String id,
                             @SessionAttribute("user") UserWithAvatar user) {

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
            StudyMember member = StudyMember.builder()
                    .userId(user.getId())
                    .groupId(id)
                    .role("멤버")
                    .build();

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

    // =======================================================================================
    // 그룹 탈퇴 핸들러
    @RequestMapping("/{groupId}/leave")
    public String leaveHandle(@PathVariable("groupId") String groupId,
                              @SessionAttribute("user") UserWithAvatar user, Model model) {

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
    @RequestMapping("/{groupId}/cancel") // groupId를 URL 경로에서 받아오는 매핑 설정
    public String cancelHandle(@PathVariable("groupId") String groupId,   // groupId를 URL에서 받아오는 파라미터
                               @SessionAttribute("user") UserWithAvatar user,   // 세션에 저장된 user 정보를 받아옴
                               Model model) {

        // 세션에서 user 객체를 받아오고, userId를 추출
        String userId = user.getId();

        // groupId와 userId를 Map으로 묶어 가입 요청 정보를 조회하기 위한 키 값으로 사용
        Map<String, String> map = Map.of("groupId", groupId, "userId", userId);

        // studyMemberRepository에서 userId와 groupId를 기준으로 가입 요청 정보를 찾음
        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);

        // 가입 요청이 존재하고, 가입이 승인되지 않은 상태에서 역할이 '멤버'인 경우에만 삭제 가능
        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            // 가입 요청을 철회하기 위해 해당 가입 요청을 데이터베이스에서 삭제
            studyMemberRepository.deleteById(found.getId());
        }

        // 철회 후 해당 그룹 페이지로 리다이렉트
        return "redirect:/study/" + groupId; // 'groupId'를 이용하여 해당 그룹의 페이지로 리다이렉트
    }


    // =======================================================================================
    // 그룹 해산 핸들러 (그룹 생성자만 가능)
    @Transactional
    @RequestMapping("/{groupId}/remove")
    public String removeHandle(@PathVariable("groupId") String groupId,
                               @SessionAttribute("user") UserWithAvatar user) {

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
                                @SessionAttribute("user") UserWithAvatar user) {

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
                             @SessionAttribute("user") UserWithAvatar user) {

        // 현재 로그인한 사용자의 ID를 게시글 작성자로 설정
        post.setWriterId(user.getId());

        // 현재 시간을 작성 시간으로 설정
        post.setWroteAt(LocalDateTime.now());
        //  post.setGroupId();
        //  post.setContent();

        // 게시글을 데이터베이스에 저장
        postRepository.create(post);

        return "redirect:/study/" + id;
    }

    // =======================================================================================
    // 글에 감정 남기기 요청 처리 핸들
    @RequestMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@ModelAttribute PostReaction postReaction,
                                     @SessionAttribute("user") UserWithAvatar user) {

        // 사용자가 해당 게시글에 남긴 감정이 있는지 확인
        PostReaction found = postReactionRepository.findByWriterIdAndPostId(
                Map.of("writerId", user.getId(), "postId", postReaction.getPostId())   // userId와 postId를 Map 형태로 전달
        );

        if (found != null) {   // 이미 감정이 남겨져 있을 경우
            postReactionRepository.deleteById(found.getId());   // 기존 감정을 삭제

        } else {   // 그렇지 않으면 새 감정 추가
            postReaction.setWriterId(user.getId());   // 현재 로그인한 사용자의 ID를 설정
            postReactionRepository.create(postReaction);   // 새로운 감정을 데이터베이스에 저장
        }

        // 원래 게시글 목록 페이지로 리디렉션 (해당 그룹의 게시글 목록 페이지로 이동)
        return "redirect:/study/" + postReaction.getGroupId();
    }
}