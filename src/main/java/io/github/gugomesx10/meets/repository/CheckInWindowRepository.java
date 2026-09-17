package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.CheckInWindow;
import io.github.gugomesx10.meets.entity.enums.CheckInStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CheckInWindowRepository extends JpaRepository<CheckInWindow, UUID> {
    List<CheckInWindow> findAllByClassSessionId(UUID classSessionId);
    List<CheckInWindow> findAllByClassSessionIdAndStatus(
            UUID classSessionId,
            CheckInStatus status
    );
    List<CheckInWindow> findAllBySessionBlockId(UUID sessionBlockId);
}