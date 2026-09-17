package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.CheckInResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInResponseRepository extends JpaRepository<CheckInResponse, UUID> {
    Optional<CheckInResponse> findByCheckInWindowIdAndStudentId(
            UUID checkInWindowId,
            UUID studentId
    );
    List<CheckInResponse> findAllByCheckInWindowId(UUID checkInWindowId);
    List<CheckInResponse> findAllByStudentId(UUID studentId);
    boolean existsByCheckInWindowIdAndStudentId(
            UUID checkInWindowId,
            UUID studentId
    );
}