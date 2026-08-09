package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.AttachmentDto;
import com.adro.tms_backend.entity.Attachment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    AttachmentDto toDto(Attachment attachment);

    List<AttachmentDto> toDtoList(List<Attachment> attachments);
}
