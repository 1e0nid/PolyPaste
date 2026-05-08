package com.atta.PolyPaste.controllers;

import com.atta.PolyPaste.services.auth.VkAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@RestController
@RequestMapping("/auth/vk")
public class VkAuthController {

    private final VkAuthService vkAuthService;

    public VkAuthController(VkAuthService vkAuthService) {
        this.vkAuthService = vkAuthService;
    }

    @GetMapping("/login")
    public RedirectView loginWithVk(HttpSession session) {
        String codeVerifier = UUID.randomUUID().toString() + UUID.randomUUID();
        session.setAttribute("vk_code_verifier", codeVerifier);
        return new RedirectView(vkAuthService.getAuthUrl(codeVerifier));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam("code") String code,
            @RequestParam("device_id") String deviceId,
            HttpSession session,
            HttpServletResponse response) {
        try {
            String codeVerifier = (String) session.getAttribute("vk_code_verifier");

            if (codeVerifier == null) {
                return ResponseEntity.status(400).body("Сессия истекла");
            }

            String jwtToken = vkAuthService.processVkAuth(code, codeVerifier, deviceId);
            Cookie cookie = new Cookie("jwt_token", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.ok("Авторизация успешна");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }
}