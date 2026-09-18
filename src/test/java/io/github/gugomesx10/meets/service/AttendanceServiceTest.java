package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.*;
import io.github.gugomesx10.meets.entity.enums.*;
import io.github.gugomesx10.meets.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

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
    private PresenceEvidenceRepository presenceEvidenceRepository;

    @Autowired
    private AttendanceDecisionRepository attendanceDecisionRepository;

    private User student;
    private User instructor;
    private ClassSession classSession;

    @BeforeEach
    void setUp() {

        Institution institution =
                new Institution();

        institution.setName("Escola da Nuvem");

        institution =
                institutionRepository.save(
                        institution
                );

        student = createUser(
                "Gustavo",
                "gustavo@teste.com"
        );

        instructor = createUser(
                "Professora",
                "professora@teste.com"
        );

        Course course =
                new Course();

        course.setInstitution(institution);
        course.setName("AWS re/Start");
        course.setDescription(
                "Treinamento AWS"
        );
        course.setStartDate(
                LocalDate.now()
        );
        course.setEndDate(
                LocalDate.now().plusMonths(3)
        );
        course.setStatus(
                CourseStatus.ACTIVE
        );

        course =
                courseRepository.save(course);

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

        classSession =
                new ClassSession();

        classSession.setCourse(course);
        classSession.setTitle("Aula AWS");
        classSession.setSessionDate(
                LocalDate.now()
        );
        classSession.setStartTime(
                LocalTime.of(9, 0)
        );
        classSession.setEndTime(
                LocalTime.of(12, 0)
        );
        classSession.setStatus(
                ClassSessionStatus.COMPLETED
        );

        classSession =
                classSessionRepository.save(
                        classSession
                );
    }

    @Test
    void aulaEmAndamentoDeveGerarPending() {

        classSession.setStatus(
                ClassSessionStatus.IN_PROGRESS
        );

        classSessionRepository.save(
                classSession
        );

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.PENDING,
                decision.getStatus()
        );
    }

    @Test
    void semEvidenciaDeveGerarAbsent() {

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.ABSENT,
                decision.getStatus()
        );
    }

    @Test
    void somenteMeetingDeveGerarReviewRequired() {

        createEvidence(
                PresenceEvidenceType.MEETING_SESSION,
                EvidenceSource.GOOGLE_MEET
        );

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.REVIEW_REQUIRED,
                decision.getStatus()
        );
    }

    @Test
    void meetingMaisCheckInDeveConfirmarPresenca() {

        createEvidence(
                PresenceEvidenceType.MEETING_SESSION,
                EvidenceSource.GOOGLE_MEET
        );

        createEvidence(
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL
        );

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                decision.getStatus()
        );
    }

    @Test
    void duasEvidenciasAtivasDevemConfirmarSemMeeting() {

        createEvidence(
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL
        );

        createEvidence(
                PresenceEvidenceType.ACTIVITY_RESPONSE,
                EvidenceSource.INTERNAL
        );

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                decision.getStatus()
        );
    }

    @Test
    void confirmacaoDoProfessorDeveConfirmarPresenca() {

        createEvidence(
                PresenceEvidenceType.TEACHER_CONFIRMATION,
                EvidenceSource.TEACHER
        );

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                decision.getStatus()
        );
    }

    @Test
    void umaUnicaEvidenciaAtivaDeveGerarReviewRequired() {

        createEvidence(
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL
        );

        AttendanceDecision decision =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                AttendanceStatus.REVIEW_REQUIRED,
                decision.getStatus()
        );
    }

    @Test
    void decisaoManualNaoDeveSerSobrescrita() {

        AttendanceDecision manual =
                new AttendanceDecision();

        manual.setStudent(student);
        manual.setClassSession(classSession);
        manual.setStatus(
                AttendanceStatus.JUSTIFIED
        );
        manual.setDecisionSource(
                AttendanceDecisionSource.TEACHER
        );
        manual.setDecidedBy(instructor);
        manual.setDecidedAt(
                Instant.now()
        );
        manual.setReason(
                "Ausência justificada manualmente."
        );

        manual =
                attendanceDecisionRepository.save(
                        manual
                );

        AttendanceDecision result =
                attendanceService.evaluate(
                        student.getId(),
                        classSession.getId()
                );

        assertEquals(
                manual.getId(),
                result.getId()
        );

        assertEquals(
                AttendanceStatus.JUSTIFIED,
                result.getStatus()
        );

        assertEquals(
                AttendanceDecisionSource.TEACHER,
                result.getDecisionSource()
        );
    }

    private PresenceEvidence createEvidence(
            PresenceEvidenceType type,
            EvidenceSource source
    ) {

        PresenceEvidence evidence =
                new PresenceEvidence();

        evidence.setStudent(student);
        evidence.setClassSession(classSession);
        evidence.setType(type);
        evidence.setSource(source);
        evidence.setOccurredAt(
                Instant.now()
        );

        return presenceEvidenceRepository.save(
                evidence
        );
    }

    private User createUser(
            String name,
            String email
    ) {

        User user = new User();

        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }
}