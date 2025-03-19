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
        if (session.getAttribute("user") == null) {   // 인증 성공했으면
            return "index";
        } else {   // 인증을 받지 않았으면
            User user = (User)session.getAttribute("user");
            model.addAttribute("user", user);

            // int avatarId = user.getAvatarId();
            Avatar userAvatar = avatarRepository.findById(user.getAvatarId());

            System.out.println(userAvatar);
            model.addAttribute("userAvatar", userAvatar);
            model.addAttribute("user", user);

            return "index-authenticated";
        }
    }
}
