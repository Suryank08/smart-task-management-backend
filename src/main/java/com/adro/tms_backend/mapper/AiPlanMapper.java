package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.AiPlanDto;
import com.adro.tms_backend.entity.AiPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AiPlanMapper {

    @Mapping(target = "userId", source = "user.id")
    AiPlanDto toDto(AiPlan aiPlan);
}
