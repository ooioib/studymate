package org.codenova.studymate.controller;

import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Select;
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


    // 스터디 상세보기 핸들러
    @RequestMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model, @SessionAttribute("user") User user) {
        // System.out.println(id);

        StudyGroup group = studyGroupRepository.findById(id);
        if (group == null) {
            return "redirect:/";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);
        map.put("userId", user.getId());
        StudyMember status = studyMemberRepository.findByUserIdAndGroupId(map);

        // 아직 참여한 적이 없다면
        if (status == null) {
            model.addAttribute("status", "NOT_JOINED");

            // 승인 대기 중
        } else if (status.getJoinedAt() == null) {
            model.addAttribute("status", "PENDING");

            // 이미 승인 받아 참여 중 이라면
            // 멤버이다
        } else if (status.getRole().equals("멤버")) {
            model.addAttribute("status", "MEMBER");

            // 리더이다
        } else {
            model.addAttribute("status", "LEADER");
        }

        model.addAttribute("group", group);
        return "study/view";
    }

    @Transactional
    @RequestMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id, @SessionAttribute("user") User user) {

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
}
