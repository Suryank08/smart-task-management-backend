package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.CategoryDto;
import com.adro.tms_backend.dto.CategoryRequest;
import com.adro.tms_backend.entity.Category;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.DuplicateResourceException;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.CategoryMapper;
import com.adro.tms_backend.repository.CategoryRepository;
import com.adro.tms_backend.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDto create(UUID userId, CategoryRequest request) {
        User user = userRepository
                .findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new DuplicateResourceException("Category already exists: " + request.name());
        }

        Category category = Category.builder()
                .user(user)
                .name(request.name())
                .icon(request.icon())
                .color(request.color())
                .build();

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> listByUser(UUID userId) {
        requireActiveUser(userId);
        return categoryMapper.toDtoList(categoryRepository.findByUserIdOrderByNameAsc(userId));
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(UUID id, UUID userId) {
        return categoryMapper.toDto(getOwnedCategory(id, userId));
    }

    @Transactional
    public CategoryDto update(UUID id, UUID userId, CategoryRequest request) {
        Category category = getOwnedCategory(id, userId);
        categoryMapper.updateEntityFromRequest(request, category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        categoryRepository.delete(getOwnedCategory(id, userId));
    }

    private Category getOwnedCategory(UUID id, UUID userId) {
        requireActiveUser(userId);
        return categoryRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
    }

    private void requireActiveUser(UUID userId) {
        if (!userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE)) {
            throw ResourceNotFoundException.of("User", userId);
        }
    }
}
