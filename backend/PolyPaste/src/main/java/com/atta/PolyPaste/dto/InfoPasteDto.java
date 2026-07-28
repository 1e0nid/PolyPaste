package com.atta.PolyPaste.dto;

import java.time.LocalDateTime;

public record InfoPasteDto(
        String shortId,
        String title,
        LocalDateTime createdAt,
        String expiresAt,
        String author
) {}