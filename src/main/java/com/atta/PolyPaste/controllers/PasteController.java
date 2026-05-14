package com.atta.PolyPaste.controllers;

import com.atta.PolyPaste.dto.RequestPasteDto;
import com.atta.PolyPaste.dto.UserPrincipalDto;
import com.atta.PolyPaste.services.PasteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pastes")
@RequiredArgsConstructor
public class PasteController {

    private final PasteService pasteService;
    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    @PostMapping("/create")
    public ResponseEntity<String> createPaste(
            @RequestBody RequestPasteDto request,
            @AuthenticationPrincipal UserPrincipalDto user) {
        log.info("call /create");
        String shortUrl = pasteService.createPaste(request, user);
        return ResponseEntity.ok("https://polypaste.com/" + shortUrl);
    }

    @PostMapping("/me")
    public ResponseEntity<String> me(@AuthenticationPrincipal UserPrincipalDto user) {
        return ResponseEntity.ok(user.toString());
    }
}
