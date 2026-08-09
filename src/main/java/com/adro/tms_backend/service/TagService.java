package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.TagDto;
import com.adro.tms_backend.dto.TagRequest;
import com.adro.tms_backend.entity.Tag;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.DuplicateResourceException;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.TagMapper;
import com.adro.tms_backend.repository.TagRepository;
import com.adro.tms_backend.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    @Transactional
    public TagDto create(UUID userId, TagRequest request) {
        User user = userRepository
                .findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        if (tagRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new DuplicateResourceException("Tag already exists: " + request.name());
        }

        Tag tag = Tag.builder()
                .user(user)
                .name(request.name())
                .color(request.color())
                .build();

        return tagMapper.toDto(tagRepository.save(tag));
    }

    @Transactional(readOnly = true)
    public List<TagDto> listByUser(UUID userId) {
        requireActiveUser(userId);
        return tagMapper.toDtoList(tagRepository.findByUserIdOrderByNameAsc(userId));
    }

    @Transactional(readOnly = true)
    public TagDto findById(UUID id, UUID userId) {
        return tagMapper.toDto(getOwnedTag(id, userId));
    }

    @Transactional
    public TagDto update(UUID id, UUID userId, TagRequest request) {
        Tag tag = getOwnedTag(id, userId);
        tagMapper.updateEntityFromRequest(request, tag);
        return tagMapper.toDto(tagRepository.save(tag));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        tagRepository.delete(getOwnedTag(id, userId));
    }

    private Tag getOwnedTag(UUID id, UUID userId) {
        requireActiveUser(userId);
        return tagRepository.findByIdAndUserId(id, userId).orElseThrow(() -> ResourceNotFoundException.of("Tag", id));
    }

    private void requireActiveUser(UUID userId) {
        if (!userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE)) {
            throw ResourceNotFoundException.of("User", userId);
        }
    }
}
