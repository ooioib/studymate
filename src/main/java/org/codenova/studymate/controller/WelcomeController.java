package org.codenova.studymate.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.codenova.studymate.model.Avatar;
import org.codenova.studymate.model.User;
import org.codenova.studymate.repository.AvatarRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
public class WelcomeController {
    private AvatarRepository avatarRepository;

    @RequestMapping({"/", "/index"})
    public String indexHandle(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "index";
        } else {
            User user = (User) session.getAttribute("user");
            model.addAttribute("user", user);

            Avatar userAvatar = avatarRepository.findById(user.getAvatarId());
            model.addAttribute("userAvatar", userAvatar);


            return "index-authenticated";
        }

    }
}
