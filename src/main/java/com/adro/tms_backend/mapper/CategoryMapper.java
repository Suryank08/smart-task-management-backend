package com.adro.tms_backend.mapper;

import com.adro.tms_backend.dto.CategoryDto;
import com.adro.tms_backend.dto.CategoryRequest;
import com.adro.tms_backend.entity.Category;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "userId", source = "user.id")
    CategoryDto toDto(Category category);

    List<CategoryDto> toDtoList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
