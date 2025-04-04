package org.codenova.studymate.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.Avatar;
import org.codenova.studymate.model.entity.StudyMember;
import org.codenova.studymate.model.entity.User;
import org.codenova.studymate.model.query.UserWithAvatar;
import org.codenova.studymate.repository.AvatarRepository;
import org.codenova.studymate.repository.StudyMemberRepository;
import org.codenova.studymate.repository.UserRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
@AllArgsConstructor
public class WelcomeController {
    private AvatarRepository avatarRepository;
    private StudyMemberRepository studyMemberRepository;

    @RequestMapping({"/", "/index"})
    public String indexHandle(@SessionAttribute("user") @Nullable UserWithAvatar user, Model model) {
        if (user == null) {
            return "index";

        } else {
            model.addAttribute("user", user);

            var studyList = studyMemberRepository.findWithGroupDetailByUserId(user.getId());

            model.addAttribute("studyList", studyList);

            return "index-authenticated";
        }
    }
}
