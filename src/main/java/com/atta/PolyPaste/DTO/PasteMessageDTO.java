package com.atta.PolyPaste.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasteMessageDTO {
    private String id;
    private String content;
}