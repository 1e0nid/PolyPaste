package com.atta.PolyPaste.entitys;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "pastes")
@Data
public class PasteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url", unique = true, nullable = false, length = 8)
    private String shortUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "creator_id")
    private String creatorId;

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
}