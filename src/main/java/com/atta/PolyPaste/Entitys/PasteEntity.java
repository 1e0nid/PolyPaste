package com.atta.PolyPaste.entitys;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pastes")
@Data
public class PasteEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Ограничение длины 8 символов для Base62
    @Column(name = "short_url", unique = true, nullable = false, length = 8)
    private String shortUrl;

    // Используем TEXT для контента до 1 МБ
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // Срок хранения (TTL)
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}