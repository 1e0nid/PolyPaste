package com.atta.PolyPaste.controllers;

import com.atta.PolyPaste.entitys.VkUserEntity;
import com.atta.PolyPaste.services.auth.VkAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/vk")
public class VkAuthController {

    private final VkAuthService vkAuthService;
    private static final Logger log = LoggerFactory.getLogger(VkAuthController.class);

    @GetMapping("/login")
    public RedirectView loginWithVk(HttpSession session) {
        log.info("call /login");
        String codeVerifier = UUID.randomUUID().toString() + UUID.randomUUID();
        session.setAttribute("vk_code_verifier", codeVerifier);
        return new RedirectView(vkAuthService.getAuthUrl(codeVerifier));
    }

    @GetMapping("/callback")
    public RedirectView callback(
            @RequestParam("code") String code,
            @RequestParam("device_id") String deviceId,
            HttpSession session) {
        try {
            log.info("call /callback");
            String codeVerifier = (String) session.getAttribute("vk_code_verifier");

            if (codeVerifier == null) {
                return new RedirectView("http://localhost:5173/login?error=session_expired");
            }

            log.info(codeVerifier);

            VkUserEntity user = vkAuthService.processVkAuth(code, codeVerifier, deviceId);
            String jwtToken = vkAuthService.createToken(user.getVkId(), user.getFirstName(), user.getLastName());

            log.info(user.getAvatarUrl());

            return new RedirectView("http://localhost:5173/login?token=" + jwtToken +
                    "&username=" + URLEncoder.encode(user.getFirstName() + " " + user.getLastName(), StandardCharsets.UTF_8)
                    + "&avatar=" + URLEncoder.encode(user.getAvatarUrl(), StandardCharsets.UTF_8)
                    + "&provider=vk");

        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("http://localhost:5173/login?error=auth_failed");
        }
    }
}