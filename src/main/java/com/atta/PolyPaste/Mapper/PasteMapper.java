package com.atta.PolyPaste.Mapper;

import com.atta.PolyPaste.DTO.Paste;
import com.atta.PolyPaste.Entitys.PasteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PasteMapper {
    @Mapping(source = "content", target = "content")
    Paste toPaste(PasteEntity pasteEntity);
}
