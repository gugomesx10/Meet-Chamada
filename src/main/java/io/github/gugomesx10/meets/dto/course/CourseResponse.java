package io.github.gugomesx10.meets.dto.course;

import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        UUID institutionId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status,
        Instant createdAt,
        Instant updatedAt

) {

    public static CourseResponse from(Course course) {

        return new CourseResponse(
                course.getId(),
                course.getInstitution().getId(),
                course.getName(),
                course.getDescription(),
                course.getStartDate(),
                course.getEndDate(),
                course.getStatus(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}