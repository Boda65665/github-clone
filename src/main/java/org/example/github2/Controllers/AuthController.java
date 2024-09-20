package org.example.github2.Controllers;

import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.example.github2.Entity.User;
import org.example.github2.Form.LoginForm;
import org.example.github2.Form.RegisterForm;
import org.example.github2.Model.Role;
import org.example.github2.Security.SHA256;
import org.example.github2.Services.DB.UserService;
import org.example.github2.Services.EmailService;
import org.example.github2.Services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@RequestMapping("/auth")
@Controller
public class AuthController {
    private final EmailService emailService;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(EmailService emailService, UserService userService, JwtService jwtService) {
        this.emailService = emailService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/reg")
    public String regGet(Model model) {
        RegisterForm registerForm = new RegisterForm();
        model.addAttribute("registerForm", registerForm);
        return "auth/reg";
    }

    @PostMapping("/reg")
    public String regPost(@ModelAttribute("registerForm") @Valid RegisterForm registerForm, BindingResult bindingResult, HttpServletResponse response, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/reg";
        }
        if (userService.isExistsUser(registerForm.getEmail())) {
            model.addAttribute("err", "User with so email already created");
            return "auth/reg";
        } else {
            if (userService.isBusyLogin(registerForm.getLogin())){
                model.addAttribute("err", "Login is busy");
                return "auth/reg";
            }
            int code = emailService.sendCode(registerForm.getEmail());
            User newUser = new User(registerForm.getEmail(),registerForm.getLogin(), SHA256.hash(registerForm.getPassword()), Role.UNVERIFIED,SHA256.hash(String.valueOf(code)),LocalDateTime.now());
            userService.save(newUser);
            setJWT(registerForm.getEmail(),response);
        }
        return "redirect:/auth/email_confirmation";
    }

    @GetMapping("/email_confirmation")
    public String emailConfirmationTemplate() {
        return "auth/email_confirmation";
    }

    @PostMapping("/email_confirmation")
    public String emailConfirmation(HttpServletRequest httpServletRequest,Model model) {
        User updateUser = getUserByRequest(httpServletRequest);
        String inputCode = httpServletRequest.getParameter("code");
        if (userService.isCorrectInputValidEmailCode(updateUser.getId(),inputCode)){
            updateUser.setRole(Role.USER);
            userService.update(updateUser);
            return "redirect:/";
        }
        else {
            model.addAttribute("err","Введен неверный код");
            return "auth/email_confirmation";
        }
    }

    private User getUserByRequest(HttpServletRequest request){
        String email = jwtService.extractEmail(request);
        return userService.findUserByEmail(email);
    }

    @PostMapping("/email_confirmation/resend")
    public String resendCodeForEmailConfirmation(HttpServletRequest httpServletRequest,Model model){
        String email = jwtService.extractEmail(httpServletRequest);
        User updateUser = getUserByRequest(httpServletRequest);
        if (userService.isAllowedResendEmail(email)){
            resendCode(updateUser);
        }
        else {
            model.addAttribute("err","запрашивать потвторную отправку кода можно каждые 2 минуты ");
            return "auth/email_confirmation";
        }
        return "redirect:/auth/email_confirmation";
    }

    private void resendCode(User updateUser) {
        int code = emailService.sendCode(updateUser.getEmail());
        updateUser.setEmailCode(SHA256.hash(String.valueOf(code)));
        updateUser.setTimeLastSendCode(LocalDateTime.now());
        userService.save(updateUser);
    }

    @GetMapping("/login")
    public String loginGet(Model model) {
        LoginForm loginForm = new LoginForm();
        model.addAttribute("loginForm", loginForm);
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPost(Model model, @ModelAttribute("loginForm") @Valid LoginForm loginForm,BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        String email = loginForm.getEmail();
        if (!userService.isExistsUser(email) || !userService.isCorrectPassword(email,loginForm.getPassword())){
            model.addAttribute("err", "Email or password not corrected");
            return "auth/login";
        }
        setJWT(loginForm.getEmail(),response);
        return "redirect:/";
    }

    private void setJWT(String email, HttpServletResponse response) {
        String token = jwtService.generateToken(email);
        Cookie newCookie = new Cookie("JWT",token);
        newCookie.setMaxAge(60 * 60);
        newCookie.setPath("/");
        response.addCookie(newCookie);
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response){
        Cookie cookie = new Cookie("JWT","");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }
}