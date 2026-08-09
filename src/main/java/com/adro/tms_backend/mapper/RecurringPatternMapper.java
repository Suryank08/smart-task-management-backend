package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.RecurringPatternDto;
import com.adro.tms_backend.dto.RecurringPatternRequest;
import com.adro.tms_backend.entity.RecurringPattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RecurringPatternMapper {

    @Mapping(target = "taskId", source = "task.id")
    RecurringPatternDto toDto(RecurringPattern pattern);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(RecurringPatternRequest request, @MappingTarget RecurringPattern pattern);
}
