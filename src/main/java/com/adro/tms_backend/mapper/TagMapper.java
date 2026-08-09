package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.TagDto;
import com.adro.tms_backend.dto.TagRequest;
import com.adro.tms_backend.entity.Tag;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "userId", source = "user.id")
    TagDto toDto(Tag tag);

    List<TagDto> toDtoList(List<Tag> tags);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(TagRequest request, @MappingTarget Tag tag);
}
