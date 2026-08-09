package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.AiSuggestionDto;
import com.adro.tms_backend.entity.AiSuggestion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AiSuggestionMapper {

    @Mapping(target = "taskId", source = "task.id")
    AiSuggestionDto toDto(AiSuggestion suggestion);

    List<AiSuggestionDto> toDtoList(List<AiSuggestion> suggestions);
}
