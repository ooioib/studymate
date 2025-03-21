package org.codenova.studymate.controller;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.StudyGroup;
import org.codenova.studymate.model.entity.StudyMember;
import org.codenova.studymate.model.entity.User;
import org.codenova.studymate.model.vo.StudyGroupWithCreator;
import org.codenova.studymate.repository.StudyGroupRepository;
import org.codenova.studymate.repository.StudyMemberRepository;
import org.codenova.studymate.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/study")
@AllArgsConstructor
public class StudyController {
    private StudyGroupRepository studyGroupRepository;
    private StudyMemberRepository studyMemberRepository;
    private UserRepository userRepository;

    // 스터디 그룹 만들기 핸들러
    @RequestMapping("/create")
    public String createHandle() {
        return "study/create";
    }

    @Transactional
    @RequestMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute StudyGroup studyGroup,
                                     @SessionAttribute("user") User user) {

        String randomId = UUID.randomUUID().toString().substring(24);

        studyGroup.setId(randomId);
        studyGroup.setCreatorId(user.getId());
        studyGroupRepository.create(studyGroup);

        StudyMember studyMember = new StudyMember();
        studyMember.setUserId(user.getId());
        studyMember.setGroupId(studyGroup.getId());
        studyMember.setRole("리더");

        studyMemberRepository.createApproved(studyMember);
        studyGroupRepository.addMemberCountById(studyGroup.getId());

        return "redirect:/study/" + randomId;
    }

    // 그룹 검색 핸들러
    @RequestMapping("/search")
    public String searchHandle(@RequestParam("word") Optional<String> word, Model model) {
        if (word.isEmpty()) {

            return "redirect:/";
        }

        String wordValue = word.get();
        List<StudyGroup> result = studyGroupRepository.findByNameLikeOrGoalLike("%" + wordValue + "%");
        List<StudyGroupWithCreator> convertedResult = new ArrayList<>();

        for (StudyGroup one : result) {
            User found = userRepository.findById(one.getCreatorId());

            StudyGroupWithCreator c = StudyGroupWithCreator.builder().group(one).creator(found).build();
            convertedResult.add(c);
        }

        System.out.println("search count : " + result.size());
        model.addAttribute("count", convertedResult.size());
        model.addAttribute("result", convertedResult);

        return "study/search";
    }

    // 그룹 상세보기 핸들러
    @RequestMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model,
                             @SessionAttribute("user") User user) {

        StudyGroup group = studyGroupRepository.findById(id);
        if (group == null) {

            return "redirect:/";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);
        map.put("userId", user.getId());
        StudyMember status = studyMemberRepository.findByUserIdAndGroupId(map);

        // 아직 참여한적이 없다
        if (status == null) {
            model.addAttribute("status", "NOT_JOINED");

            // 승인대기중
        } else if (status.getJoinedAt() == null) {
            model.addAttribute("status", "PENDING");

            // 멤버이다
        } else if (status.getRole().equals("멤버")) {
            model.addAttribute("status", "MEMBER");

            // 리더이다.
        } else {
            model.addAttribute("status", "LEADER");
        }

        model.addAttribute("group", group);

        return "study/view";
    }

    // 스터디 그룹 가입 핸들러
    @Transactional
    @RequestMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id,
                             @SessionAttribute("user") User user) {

        boolean exist = false;

        List<StudyMember> list = studyMemberRepository.findByUserId(user.getId());

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
    public String leaveHandle(@PathVariable("groupId") String groupId,
                              @SessionAttribute("user") User user, Model model) {

        String userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);
        studyMemberRepository.deleteById(found.getId());
        studyGroupRepository.subtractMemberCountById(groupId);

        return "redirect:/";
    }

    // 가입 요청 철회 핸들러
    @RequestMapping("/{groupId}/cancel")
    public String cancelHandle(@PathVariable("groupId") String groupId,
                               @SessionAttribute("user") User user, Model model) {

        String userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);

        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            studyMemberRepository.deleteById(found.getId());
        }

        return "redirect:/study/" + groupId;
    }

    // 그룹 해산 핸들러
    @Transactional
    @RequestMapping("/{groupId}/remove")
    public String removeHandle(@PathVariable("groupId") String groupId,
                               @SessionAttribute("user") User user) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId);

        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {
            studyMemberRepository.deleteByGroupId(groupId);
            studyGroupRepository.deleteById(groupId);

            return "redirect:/";

        } else {
            return "redirect:/study/" + groupId;
        }
    }

    // 가입 승인 핸들러
    @Transactional
    @RequestMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") User user) {

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
}
