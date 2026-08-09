package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.SubtaskDto;
import com.adro.tms_backend.dto.SubtaskRequest;
import com.adro.tms_backend.entity.Subtask;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubtaskMapper {

    @Mapping(target = "taskId", source = "task.id")
    SubtaskDto toDto(Subtask subtask);

    List<SubtaskDto> toDtoList(List<Subtask> subtasks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(SubtaskRequest request, @MappingTarget Subtask subtask);
}
