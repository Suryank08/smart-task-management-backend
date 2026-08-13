package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {}

    /**
     * Builds a single query combining every filter that was actually supplied (all others are
     * left out of the WHERE clause entirely), so status/priority/archived/dueDate/search can all
     * be applied together instead of the caller having to pick just one.
     */
    public static Specification<Task> filter(
            UUID userId,
            TaskStatus status,
            TaskPriority priority,
            Boolean pinned,
            Instant dueFrom,
            Instant dueTo,
            String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (pinned != null) {
                predicates.add(cb.equal(root.get("pinned"), pinned));
            }
            if (dueFrom != null && dueTo != null) {
                predicates.add(cb.between(root.get("dueDate"), dueFrom, dueTo));
            }
            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate titleContains = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate descriptionContains = cb.like(cb.lower(root.get("description")), likePattern);
                // Trigram similarity as a typo-tolerant fallback — plain ILIKE substring matching
                // above is the primary behavior, since similarity() alone routinely scores well
                // below its own 0.3 threshold for genuine (but short) substring matches.
                Predicate titleSimilar = cb.greaterThan(
                        cb.function("similarity", Float.class, root.get("title"), cb.literal(search)), 0.3f);
                predicates.add(cb.or(titleContains, descriptionContains, titleSimilar));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
