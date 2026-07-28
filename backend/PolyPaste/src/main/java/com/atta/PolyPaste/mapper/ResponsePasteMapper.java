package com.atta.PolyPaste.mapper;

import com.atta.PolyPaste.dto.InfoPasteDto;
import com.atta.PolyPaste.dto.ResponsePasteDto;
import com.atta.PolyPaste.entitys.PasteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface ResponsePasteMapper {

    @Mapping(target = "title", source = "entity.shortUrl")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "language", source = "entity.syntax")
    @Mapping(target = "author", expression = "java(mapAuthor(entity))")
    @Mapping(target = "date", source = "entity.createdAt")
    @Mapping(target = "size", source = "content", qualifiedByName = "calculateSize")
    ResponsePasteDto toResponseDto(PasteEntity entity, String content);

    List<InfoPasteDto> toInfoPasteDtoList(List<PasteEntity> entities);

    @Mapping(target = "shortId", source = "shortUrl")
    @Mapping(target = "title", source = "shortUrl")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "expiresAt", source = "expirationTime")
    @Mapping(target = "author", expression = "java(mapAuthor(entity))")
    InfoPasteDto toInfoPasteDto(PasteEntity entity);

    default String mapAuthor(PasteEntity entity) {
        if (entity == null) return "Anonymous";
        String first = entity.getFirstName() != null ? entity.getFirstName() : "";
        String last = entity.getLastName() != null ? entity.getLastName() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Anonymous" : full;
    }

    @Named("calculateSize")
    default String calculateSize(String content) {
        if (content == null) return "0 B";
        long bytes = content.length();
        if (bytes < 1024) return bytes + " B";
        return String.format(Locale.ENGLISH, "%.2f KB", bytes / 1024.0);
    }
}