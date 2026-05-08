package com.atta.PolyPaste.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResultDTO {
    private String id;
    private String content;
    private String status;
}