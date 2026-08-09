package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.TaskDto;
import com.adro.tms_backend.dto.TaskUpdateRequest;
import com.adro.tms_backend.entity.Tag;
import com.adro.tms_backend.entity.Task;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "tagIds", source = "tags")
    TaskDto toDto(Task task);

    default Set<UUID> mapTagsToIds(Set<Tag> tags) {
        return tags == null ? Set.of() : tags.stream().map(Tag::getId).collect(Collectors.toSet());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "aiMetadata", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(TaskUpdateRequest request, @MappingTarget Task task);
}
