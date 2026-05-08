package com.atta.PolyPaste.mapper;

import com.atta.PolyPaste.dto.Paste;
import com.atta.PolyPaste.entitys.PasteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PasteMapper {
    @Mapping(source = "content", target = "content")
    Paste toPaste(PasteEntity pasteEntity);
}
