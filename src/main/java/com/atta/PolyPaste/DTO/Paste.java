package com.atta.PolyPaste.DTO;

import java.util.UUID;

public record Paste(
        UUID id,
        String content
) {
}
