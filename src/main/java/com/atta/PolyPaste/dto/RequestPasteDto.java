package com.atta.PolyPaste.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание новой пасты")
public record RequestPasteDto(
        @Schema(description = "Содержимое текста", example = "print('Hello World')")
        String content,

        @Schema(description = "Язык подсветки синтаксиса", example = "python")
        String syntax,

        @Schema(description = "Дата и время удаления", example = "2026-05-10T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expirationTime,

        @Schema(description = "Удалить ли после первого прочтения")
        boolean burnAfterRead
) {}