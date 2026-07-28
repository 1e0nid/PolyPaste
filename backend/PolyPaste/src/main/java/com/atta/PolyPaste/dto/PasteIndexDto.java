package com.atta.PolyPaste.dto;

import java.time.LocalDateTime;

public record PasteIndexDto(
        String id,
        String title,
        String author,
        String content,
        String createdAt
) {}