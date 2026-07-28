package com.atta.PolyPaste.entitys;

import com.atta.PolyPaste.enums.Visibility;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pastes")
@Data
public class PasteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "short_url", unique = true, nullable = false, length = 8)
    private String shortUrl;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "minio_key")
    private String minioKey;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_Name")
    private String lastName;

    @Column(name = "syntax")
    private String syntax;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "burn_after_read")
    private boolean burnAfterRead = false;

    @Column(name = "status")
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PUBLIC;
}