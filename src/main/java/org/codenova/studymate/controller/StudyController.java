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

@Controller
@RequestMapping("/study")
@AllArgsConstructor
public class StudyController {
    private StudyGroupRepository studyGroupRepository;
    private StudyMemberRepository studyMemberRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private AvatarRepository avatarRepository;
    private PostReactionRepository postReactionRepository;

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
        studyGroup.setId(randomId);
        studyGroup.setCreatorId(user.getId());

        // 그룹을 데이터베이스에 저장
        studyGroupRepository.create(studyGroup);

        // 그룹 생성자가 자동으로 '리더' 역할로 가입되도록 설정
        StudyMember studyMember = new StudyMember();
        studyMember.setUserId(user.getId());
        studyMember.setGroupId(studyGroup.getId());
        studyMember.setRole("리더");

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
            User found = userRepository.findById(one.getCreatorId());
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
                    .id(post.getId())
                    .content(post.getContent())
                    .writerName(userRepository.findById(post.getWriterId()).getName())
                    .writerAvatar(avatarRepository.findById(userRepository.findById(post.getWriterId()).getAvatarId()).getImageUrl())
                    .time(prettyTime.format(post.getWroteAt()))
                    .reactions(postReactionRepository.countFeelingByPostId(post.getId()))
                    .build();
            postMetas.add(cvt);
        }

        model.addAttribute("postMetas", postMetas);

        return "study/view";
    }


    @Transactional
    @RequestMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id, @SessionAttribute("user") UserWithAvatar user) {

        boolean exist = false;
        List<StudyMember> list = studyMemberRepository.findByUserId(user.getId());
        for (StudyMember one : list) {
            if (one.getGroupId().equals(id)) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            StudyMember member = StudyMember.builder().
                    userId(user.getId()).groupId(id).role("멤버").build();
            StudyGroup group = studyGroupRepository.findById(id);
            if (group.getType().equals("공개")) {
                studyMemberRepository.createApproved(member);
                studyGroupRepository.addMemberCountById(id);
            } else {
                studyMemberRepository.createPending(member);
            }
        }

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

    // 글에 감정 남기기 요청 처리 핸들
    @RequestMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@ModelAttribute PostReaction postReaction, @SessionAttribute("user") UserWithAvatar user) {

        PostReaction found =
                postReactionRepository.findByWriterIdAndPostId(Map.of("writerId", user.getId(), "postId", postReaction.getPostId()));

        if (found != null) {
            postReactionRepository.deleteById(found.getId());
        }

        postReaction.setWriterId(user.getId());
        postReactionRepository.create(postReaction);

        return "redirect:/study/" + postReaction.getGroupId();
    }

    @ModelAttribute("user")
    public UserWithAvatar addUser(@SessionAttribute("user") UserWithAvatar user) {
        System.out.println("addUser...");
        return user;
    }
}
