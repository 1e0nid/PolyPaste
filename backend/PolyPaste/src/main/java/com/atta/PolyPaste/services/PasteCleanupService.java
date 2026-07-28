package com.atta.PolyPaste.services;

import com.atta.PolyPaste.entitys.PasteEntity;
import com.atta.PolyPaste.repository.PasteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasteCleanupService {

    private final PasteRepository pasteRepository;
    private final MinioService minioService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void removeExpiredPastes() {
        LocalDateTime now = LocalDateTime.now();
        List<PasteEntity> expired = pasteRepository.findAllByExpirationTimeBefore(now);

        if (expired.isEmpty()) return;

        log.info("Обнаружено {} просроченных паст. Начинаю зачистку...", expired.size());

        for (PasteEntity paste : expired) {
            try {
                minioService.deleteContent(paste.getShortUrl());
                pasteRepository.delete(paste);

                log.debug("Паста {} успешно удалена.", paste.getShortUrl());
            } catch (Exception e) {
                log.error("Ошибка при удалении пасты {}: {}", paste.getShortUrl(), e.getMessage());
            }
        }

        log.info("Очистка завершена.");
    }
}
