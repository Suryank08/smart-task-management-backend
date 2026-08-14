package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.AiPlan;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPlanRepository extends JpaRepository<AiPlan, UUID> {

    Optional<AiPlan> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
