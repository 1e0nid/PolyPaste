package com.atta.PolyPaste.dto;

import java.time.LocalDateTime;

public record ResponsePasteDto(
        String title,
        String content,
        String author,
        LocalDateTime date,
        String language,
        String size,
        boolean isPasswordProtected
) {}