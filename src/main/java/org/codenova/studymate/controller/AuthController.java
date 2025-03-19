package org.codenova.studymate.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.codenova.studymate.model.Avatar;
import org.codenova.studymate.model.LoginLog;
import org.codenova.studymate.model.User;
import org.codenova.studymate.repository.AvatarRepository;
import org.codenova.studymate.repository.LoginLogRepository;
import org.codenova.studymate.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private AvatarRepository avatarRepository;
    private UserRepository userRepository;
    private LoginLogRepository loginLogRepository;


    @RequestMapping("/signup")
    public String signupHandle(Model model) {


        model.addAttribute("avatars", avatarRepository.findAll());

        return "auth/signup";
    }

    @RequestMapping("/signup/verify")
    public String signupVerifyHandle(@ModelAttribute @Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // 에러처리를 하고,
            return "auth/signup/verify-failed";
        }
        if (userRepository.findById(user.getId()) != null) {
            // 에러 처리하고
            return "auth/signup/verify-failed";
        }
        userRepository.create(user);
        // 밴드 기준으로 가입이 성공하면 로그인 처리되고, 모임 개설로 리다이렉트를 시키는 걸로 확인함.
        return "redirect:/index";
    }


    @RequestMapping("/login")
    public String loginHandle(Model model) {
        return "auth/login";
    }

    @RequestMapping("/login/verify")
    public String loginVerifyHandle(@RequestParam("id") String id,
                                    @RequestParam("password") String password,
                                    HttpSession session) {
        User found = userRepository.findById(id);
        if (found == null || !found.getPassword().equals(password)) {

            return "auth/login/verify-failed";
        } else {
            userRepository.updateLoginCountByUserId(id);
            loginLogRepository.create(id);

            session.setAttribute("user", found);

            return "redirect:/index";
        }
    }

    @RequestMapping("/logout")
    public String logoutHandle(HttpSession session) {
        // session.removeAttribute("user");
        session.invalidate();
        return "redirect:/index";
    }



}
