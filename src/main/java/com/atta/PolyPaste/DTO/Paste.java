package com.atta.PolyPaste.dto;

import java.util.UUID;

public record Paste(
        UUID id,
        String content
) {
}
