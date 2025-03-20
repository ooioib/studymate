package org.codenova.studymate.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.codenova.studymate.model.StudyGroup;
import org.codenova.studymate.model.StudyMember;
import org.codenova.studymate.model.User;
import org.codenova.studymate.repository.StudyGroupRepository;
import org.codenova.studymate.repository.StudyMemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.UUID;

@Controller
@RequestMapping("/study")
@AllArgsConstructor
public class StudyController {
    private StudyGroupRepository studyGroupRepository;
    private StudyMemberRepository studyMemberRepository;

    @RequestMapping("/create")
    public String createHandle() {
        return "study/create";
    }

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

        return "redirect:/";
    }
}
