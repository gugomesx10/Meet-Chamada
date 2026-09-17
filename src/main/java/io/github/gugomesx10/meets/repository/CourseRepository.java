package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findAllByInstitutionId(UUID institutionId);
}