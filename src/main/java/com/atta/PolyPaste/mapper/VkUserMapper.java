package com.atta.PolyPaste.mapper;

import com.atta.PolyPaste.entitys.VkUserEntity;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.net.URI;

@Mapper(componentModel = "spring")
public interface VkUserMapper {

    @Mapping(target = "vkId", source = "id")
    @Mapping(target = "avatarUrl", source = "photo50")
    VkUserEntity toEntity(GetResponse vkUser);

    default String mapUriToString(URI value) {
        return value != null ? value.toString() : null;
    }
}