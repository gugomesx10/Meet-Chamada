package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.*;
import io.github.gugomesx10.meets.entity.enums.*;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CheckInServiceTest {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Autowired
    private CheckInResponseRepository checkInResponseRepository;

    @Autowired
    private PresenceEvidenceRepository presenceEvidenceRepository;

    private User instructor;
    private User student;
    private Course course;
    private ClassSession classSession;

    @BeforeEach
    void setUp() {

        Institution institution = new Institution();
        institution.setName("Escola da Nuvem");

        institution =
                institutionRepository.save(
                        institution
                );

        instructor = new User();
        instructor.setName("Professora");
        instructor.setEmail("professora@teste.com");

        instructor =
                userRepository.save(
                        instructor
                );

        student = new User();
        student.setName("Gustavo");
        student.setEmail("gustavo@teste.com");

        student =
                userRepository.save(
                        student
                );

        course = new Course();

        course.setInstitution(institution);
        course.setName("AWS re/Start");
        course.setDescription("Treinamento AWS");
        course.setStartDate(LocalDate.now());
        course.setEndDate(
                LocalDate.now().plusMonths(3)
        );
        course.setStatus(
                CourseStatus.ACTIVE
        );

        course =
                courseRepository.save(
                        course
                );

        CourseMembership instructorMembership =
                new CourseMembership();

        instructorMembership.setCourse(course);
        instructorMembership.setUser(instructor);
        instructorMembership.setRole(
                CourseRole.INSTRUCTOR
        );

        courseMembershipRepository.save(
                instructorMembership
        );

        CourseMembership studentMembership =
                new CourseMembership();

        studentMembership.setCourse(course);
        studentMembership.setUser(student);
        studentMembership.setRole(
                CourseRole.STUDENT
        );

        courseMembershipRepository.save(
                studentMembership
        );

        classSession = new ClassSession();

        classSession.setCourse(course);
        classSession.setTitle("Aula AWS");
        classSession.setSessionDate(LocalDate.now());
        classSession.setStartTime(
                LocalTime.of(9, 0)
        );
        classSession.setEndTime(
                LocalTime.of(12, 0)
        );
        classSession.setStatus(
                ClassSessionStatus.IN_PROGRESS
        );

        classSession =
                classSessionRepository.save(
                        classSession
                );
    }

    @Test
    void deveAbrirCheckInQuandoUsuarioForInstrutor() {

        CheckInWindow window =
                checkInService.openCheckIn(
                        classSession.getId(),
                        null,
                        instructor,
                        Duration.ofMinutes(3)
                );

        assertNotNull(
                window.getId()
        );

        assertEquals(
                CheckInStatus.OPEN,
                window.getStatus()
        );

        assertEquals(
                instructor.getId(),
                window.getOpenedBy().getId()
        );

        assertEquals(
                classSession.getId(),
                window.getClassSession().getId()
        );
    }

    @Test
    void deveRegistrarRespostaEGerarEvidencia() {

        CheckInWindow window =
                checkInService.openCheckIn(
                        classSession.getId(),
                        null,
                        instructor,
                        Duration.ofMinutes(3)
                );

        CheckInResponse response =
                checkInService.respond(
                        window.getId(),
                        student
                );

        assertNotNull(
                response.getId()
        );

        assertTrue(
                response.isValid()
        );

        assertTrue(
                checkInResponseRepository
                        .existsByCheckInWindowIdAndStudentId(
                                window.getId(),
                                student.getId()
                        )
        );

        var evidences =
                presenceEvidenceRepository
                        .findAllByStudentIdAndClassSessionId(
                                student.getId(),
                                classSession.getId()
                        );

        assertEquals(
                1,
                evidences.size()
        );

        assertEquals(
                PresenceEvidenceType.CHECK_IN,
                evidences.getFirst().getType()
        );

        assertEquals(
                EvidenceSource.INTERNAL,
                evidences.getFirst().getSource()
        );
    }

    @Test
    void naoDevePermitirResponderDuasVezes() {

        CheckInWindow window =
                checkInService.openCheckIn(
                        classSession.getId(),
                        null,
                        instructor,
                        Duration.ofMinutes(3)
                );

        checkInService.respond(
                window.getId(),
                student
        );

        assertThrows(
                ConflictException.class,
                () -> checkInService.respond(
                        window.getId(),
                        student
                )
        );
    }

    @Test
    void alunoNaoDevePoderAbrirCheckIn() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> checkInService.openCheckIn(
                        classSession.getId(),
                        null,
                        student,
                        Duration.ofMinutes(3)
                )
        );
    }
}