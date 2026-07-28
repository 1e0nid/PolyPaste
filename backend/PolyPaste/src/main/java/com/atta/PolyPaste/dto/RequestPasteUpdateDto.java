package com.atta.PolyPaste.dto;

public record RequestPasteUpdateDto(
    String title,
    String content,
    String syntax
) {}
