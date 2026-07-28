package com.atta.PolyPaste.services;

import com.atta.PolyPaste.dto.*;
import com.atta.PolyPaste.entitys.PasteEntity;
import com.atta.PolyPaste.enums.LanguageExtension;
import com.atta.PolyPaste.enums.Visibility;
import com.atta.PolyPaste.mapper.ResponsePasteMapper;
import com.atta.PolyPaste.repository.PasteRepository;
import com.atta.PolyPaste.services.rabbit.RabbitPasteProducer;
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PasteService {

    private final UrlService urlService;
    private final RabbitPasteProducer rabbitPasteProducer;
    private final PasteRepository pasteRepository;
    private final MinioService minioService;
    private final ResponsePasteMapper responsePasteMapper;
    private static final Logger log = LoggerFactory.getLogger(PasteService.class);
    private final OpenSearchService openSearchService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final StringRedisTemplate redisTemplate;


    public String createPaste(RequestPasteDto request, UserPrincipalDto user) {
        String shortUrl = urlService.getFreeUrl();

        String passwordCoder = null;

        if (request.password() != null && !request.password().isBlank()) {
            passwordCoder = passwordEncoder.encode(request.password().trim());
        }

        PasteMessageDto message = new PasteMessageDto(
                null,
                shortUrl,
                request.content(),
                user.id(),
                user.firstName(),
                user.lastName(),
                request.syntax(),
                request.expirationTime(),
                request.burnAfterRead(),
                request.visibility(),
                passwordCoder
        );

        log.info("create paste");
        rabbitPasteProducer.sendToQueue(message);
        return shortUrl;
    }

    public void savePaste(ModerationPasteMessageDto message) throws MinioException, IOException {
        PasteEntity entity = mapToEntity(message);

        CompletableFuture<Void> minioTask = CompletableFuture.runAsync(() -> {
            try {
                minioService.saveContent(message.content(), entity.getMinioKey());
            } catch (Exception e) {
                throw new RuntimeException("Ошибка записи в MinIO", e);
            }
        });

        CompletableFuture<Void> postgresTask = CompletableFuture.runAsync(() -> {
            pasteRepository.save(entity);
        });

        try {
            CompletableFuture.allOf(minioTask, postgresTask).join();
        } catch (Exception e) {
            log.error("Ошибка параллельной записи для пасты {}", message.shortUrl(), e);
            throw new IOException("Каскадный сбой хранилищ при асинхронном сохранении", e);
        }

        try {
            String cacheKey = "paste:content:" + message.shortUrl();
            redisTemplate.opsForValue().set(cacheKey, message.content(), Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Ошибка кратковременного кэширования пасты {}", message.shortUrl(), e);
        }

        if (entity.getVisibility() == Visibility.PUBLIC && message.password() == null) {
            openSearchService.createIndex(message);
        }
    }

    public ResponsePasteDto getPaste(String url, UserPrincipalDto user){
        PasteEntity pasteEntity = pasteRepository.findByShortUrl(url)
                .orElseThrow(() -> new EntityNotFoundException("Паста с url " + url + " не найдена"));

        boolean isAuthor = user != null && user.id().equals(pasteEntity.getCreatorId());
        boolean isProtected = pasteEntity.getPasswordHash() != null && !pasteEntity.getPasswordHash().isBlank();

        if (isProtected && !isAuthor) {
            return new ResponsePasteDto(pasteEntity.getShortUrl(), "", pasteEntity.getSyntax(), pasteEntity.getCreatedAt(), pasteEntity.getCreatedAt().toString(), "0 B", true);
        }

        String cacheKey = "paste:content:" + url;
        String content = null;

        try {
            content = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.error("Ошибка чтения Redis", e);
        }

        if (content == null) {
            content = minioService.getPasteContent(pasteEntity.getMinioKey());
            try {
                redisTemplate.opsForValue().set(cacheKey, content, Duration.ofHours(24));
            } catch (Exception e) {
                log.error("Ошибка обновления кэша Redis", e);
            }
        }

        return responsePasteMapper.toResponseDto(pasteEntity, content);
    }

    public List<InfoPasteDto> getMyPastes(String userId) {
        List<PasteEntity> listPastes = pasteRepository.findByCreatorId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Паста не найдена"));
        return responsePasteMapper.toInfoPasteDtoList(listPastes);
    }

    public List<InfoPasteDto> getAllPastes() {
        List<PasteEntity> listPastes = pasteRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);
        return responsePasteMapper.toInfoPasteDtoList(listPastes);
    }

    public void deletePaste(String shortId, String currentUserId) throws IOException {
        PasteEntity paste = pasteRepository.findByShortUrl(shortId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Паста не найдена"));
        if (!paste.getCreatorId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете удалить чужую пасту");
        }
        minioService.deleteContent(shortId);
        pasteRepository.delete(paste);
        openSearchService.deleteById(shortId);
    }

    public void updatePaste(String shortUrl, RequestPasteUpdateDto dto, String currentUsername) throws IOException, MinioException {

        PasteEntity paste = pasteRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new NoSuchElementException("Паста не найдена"));

        boolean isProtected = paste.getPasswordHash() != null && !paste.getPasswordHash().isBlank();

        if (!paste.getCreatorId().equals(currentUsername)) {
            throw new AccessDeniedException("Вы не являетесь автором этой пасты!");
        }

        paste.setSyntax(dto.syntax());
        paste.setExpirationTime(LocalDateTime.now());
        pasteRepository.save(paste);

        minioService.saveContent(dto.content(), paste.getMinioKey());
        if (!isProtected) {
            try {
                PasteIndexDto updatedIndexDto = new PasteIndexDto(
                        shortUrl,
                        shortUrl,
                        paste.getFirstName() + " " + paste.getLastName(),
                        dto.content(),
                        paste.getCreatedAt().toString()
                );

                openSearchService.createIndexDirect(updatedIndexDto);
            } catch (Exception e) {
                log.error("Ошибка обновления индекса OpenSearch для пасты {}", shortUrl, e);
            }
        }
    }

    public ResponsePasteDto unlockAndGetPaste(String shortUrl, String rawPassword) {
        // 1. Ищем пасту в базе данных
        PasteEntity pasteEntity = pasteRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new EntityNotFoundException("Паста с url " + shortUrl + " не найдена"));

        // 2. Проверяем, защищена ли она вообще паролем
        if (pasteEntity.getPasswordHash() == null || pasteEntity.getPasswordHash().isBlank()) {
            throw new IllegalStateException("Эта паста не защищена паролем!");
        }

        // 3. Сверяем сырой пароль от фронтенда с хэшем из базы данных
        // Важно: используем именно .matches(), а не .equals()
        if (!passwordEncoder.matches(rawPassword, pasteEntity.getPasswordHash())) {
            throw new org.springframework.security.access.AccessDeniedException("Неверный пароль!");
        }

        // 4. Если пароль верный — только теперь скачиваем секретный текст из MinIO
        String content = minioService.getPasteContent(pasteEntity.getMinioKey());

        // 6. Маппим и возвращаем полноценный DTO с контентом
        // Явно передаем false в поле isPasswordProtected, так как паста успешно разблокирована
        return new ResponsePasteDto(
                pasteEntity.getShortUrl(), // или title
                content,
                pasteEntity.getFirstName() + " " + pasteEntity.getLastName(), // или твой метод mapAuthor
                pasteEntity.getCreatedAt(),
                pasteEntity.getSyntax(),
                "0 B",    // Твой метод подсчета размера или строка вида "1.2 KB"
                false                      // 🔥 Сигнал для Vue: "паста открыта, убирай замочек"
        );
    }

    private PasteEntity mapToEntity(ModerationPasteMessageDto dto) {
        PasteEntity entity = new PasteEntity();

        entity.setId(dto.id());
        entity.setShortUrl(dto.shortUrl());
        entity.setCreatorId(dto.creatorId());
        entity.setMinioKey(createNameFile(dto.shortUrl(), dto.syntax()));
        entity.setFirstName(dto.firstName());
        entity.setLastName(dto.lastName());
        entity.setSyntax(dto.syntax());
        entity.setExpirationTime(dto.expirationTime());
        entity.setBurnAfterRead(dto.burnAfterRead());
        entity.setStatus(dto.status());
        entity.setVisibility(dto.visibility());
        entity.setPasswordHash(dto.password());

        return entity;
    }


    private String createNameFile(String shortUrl, String syntax) {
        return shortUrl + LanguageExtension.getExtensionByValue(syntax);
    }
}
