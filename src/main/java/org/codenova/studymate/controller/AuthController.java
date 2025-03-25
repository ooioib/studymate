package org.codenova.studymate.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.User;
import org.codenova.studymate.model.query.UserWithAvatar;
import org.codenova.studymate.repository.AvatarRepository;
import org.codenova.studymate.repository.LoginLogRepository;
import org.codenova.studymate.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            // 유효성 검사 실패 시 처리
            return "auth/signup/verify-failed";
        }
        if (userRepository.findById(user.getId()) != null) {
            // 이미 존재하는 ID 처리
            return "auth/signup/verify-failed";
        }
        userRepository.create(user);

        // 가입 성공 시 메인 페이지로 이동
        return "redirect:/index";
    }

    @RequestMapping("/login")
    public String loginHandle(Model model) {
        return "auth/login";
    }

    @Transactional
    @RequestMapping("/login/verify")
    public String loginVerifyHandle(@RequestParam("id") String id,
                                    @RequestParam("password") String password,
                                    Model model,
                                    HttpSession session) {

        UserWithAvatar found = userRepository.findWithAvatarById(id);

        if (found == null || !found.getPassword().equals(password)) {
            // 로그인 실패 시 메시지를 모델에 추가하고 로그인 실패 페이지로 이동

            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "auth/verify-failed";
        } else {
            // 로그인 성공 시 처리
            userRepository.updateLoginCountByUserId(id);
            loginLogRepository.create(id);

            session.setAttribute("user", found);
            return "redirect:/index";
        }
    }

    @RequestMapping("/logout")
    public String logoutHandle(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }
}
