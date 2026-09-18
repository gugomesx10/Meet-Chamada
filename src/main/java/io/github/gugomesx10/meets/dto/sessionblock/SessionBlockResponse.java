package io.github.gugomesx10.meets.dto.sessionblock;

import io.github.gugomesx10.meets.entity.SessionBlock;
import io.github.gugomesx10.meets.entity.enums.SessionBlockType;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record SessionBlockResponse(
        UUID id,
        UUID classSessionId,
        UUID instructorId,
        String title,
        String description,
        SessionBlockType type,
        LocalTime startTime,
        LocalTime endTime,
        Instant createdAt,
        Instant updatedAt

) {

    public static SessionBlockResponse from(
            SessionBlock block
    ) {

        return new SessionBlockResponse(
                block.getId(),
                block.getClassSession().getId(),
                block.getInstructor().getId(),
                block.getTitle(),
                block.getDescription(),
                block.getType(),
                block.getStartTime(),
                block.getEndTime(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}