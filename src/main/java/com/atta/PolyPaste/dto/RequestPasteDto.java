package com.atta.PolyPaste.dto;

import com.atta.PolyPaste.enums.Visibility;

import java.time.LocalDateTime;

public record RequestPasteDto(
        String content,
        String syntax,
        LocalDateTime expirationTime,
        boolean burnAfterRead,
        Visibility visibility,
        String password
) {}