package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.ReminderDto;
import com.adro.tms_backend.dto.ReminderRequest;
import com.adro.tms_backend.entity.Reminder;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReminderMapper {

    @Mapping(target = "taskId", source = "task.id")
    ReminderDto toDto(Reminder reminder);

    List<ReminderDto> toDtoList(List<Reminder> reminders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(ReminderRequest request, @MappingTarget Reminder reminder);
}
