package com.atta.PolyPaste.dto;

import com.atta.PolyPaste.enums.Visibility;

import java.time.LocalDateTime;

public record PasteMessageDto (
    Long id,
    String shortUrl,
    String content,
    String creatorId,
    String firstName,
    String lastName,
    String syntax,
    LocalDateTime expirationTime,
    boolean burnAfterRead,
    Visibility visibility,
    String password
) {}
