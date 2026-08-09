package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.AiSuggestion;
import com.adro.tms_backend.entity.enums.SuggestionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, UUID> {

    List<AiSuggestion> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    List<AiSuggestion> findByTaskIdAndStatus(UUID taskId, SuggestionStatus status);

    Optional<AiSuggestion> findByIdAndTaskId(UUID id, UUID taskId);
}
