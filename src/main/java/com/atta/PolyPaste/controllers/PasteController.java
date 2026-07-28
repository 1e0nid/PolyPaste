package com.atta.PolyPaste.controllers;

import com.atta.PolyPaste.dto.*;
import com.atta.PolyPaste.services.OpenSearchService;
import com.atta.PolyPaste.services.PasteService;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pastes")
@RequiredArgsConstructor
public class PasteController {

    private final PasteService pasteService;
    private static final Logger log = LoggerFactory.getLogger(PasteController.class);
    private final OpenSearchService openSearchService;

    @PostMapping("/create")
    public ResponseEntity<ShortUrlDto> createPaste(
            @RequestBody RequestPasteDto request,
            @AuthenticationPrincipal UserPrincipalDto user) {
        log.info("call /create");
        log.info(user.toString());
        String shortUrl = pasteService.createPaste(request, user);
        return ResponseEntity.ok(new ShortUrlDto(shortUrl));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<ResponsePasteDto> getPaste(@PathVariable String shortUrl,
                                                     @AuthenticationPrincipal UserPrincipalDto user) {
        log.info("call /{}", shortUrl);
        ResponsePasteDto response = pasteService.getPaste(shortUrl, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/myPastes")
    public ResponseEntity<List<InfoPasteDto>> getMyPastes(@AuthenticationPrincipal UserPrincipalDto user) {
        String userId = user.id();
        return ResponseEntity.ok(pasteService.getMyPastes(userId));
    }

    @GetMapping("/allPastes")
    public ResponseEntity<List<InfoPasteDto>> getAllPastes() {
        return ResponseEntity.ok(pasteService.getAllPastes());
    }

    @DeleteMapping("/{shortId}")
    public ResponseEntity<Void> delete(@PathVariable String shortId,
                                       @AuthenticationPrincipal UserPrincipalDto user) throws IOException {
        pasteService.deletePaste(shortId, user.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam("q") String query) throws IOException {
        log.info("start search {}", query);
        return openSearchService.search(query);
    }

    @PutMapping("/{shortUrl}")
    public ResponseEntity<Void> updatePaste(
            @PathVariable String shortUrl,
            @RequestBody RequestPasteUpdateDto request,
            @AuthenticationPrincipal UserPrincipalDto user) throws IOException, MinioException {
        log.info("call update /{}", shortUrl);

        pasteService.updatePaste(shortUrl, request, user.id());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{shortUrl}/unlock")
    public ResponseEntity<ResponsePasteDto> unlockPaste(
            @PathVariable String shortUrl,
            @RequestBody UnlockPasteDto dto) {

        log.info("Получен запрос на разблокировку пасты [{}]", shortUrl);

        // Вызываем метод сервиса, который мы прописали ранее
        ResponsePasteDto response = pasteService.unlockAndGetPaste(shortUrl, dto.password());

        return ResponseEntity.ok(response);
    }
}