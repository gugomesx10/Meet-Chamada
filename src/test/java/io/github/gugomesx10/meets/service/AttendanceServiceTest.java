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
    private PresenceEvidenceService presenceEvidenceService;

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
    private AttendanceDecisionRepository attendanceDecisionRepository;

    private User student;
    private User instructor;
    private Course course;
    private ClassSession classSession;

    @BeforeEach
    void setUp() {

        Institution institution = new Institution();
        institution.setName("Escola da Nuvem");
        institution = institutionRepository.save(institution);

        student = new User();
        student.setName("Gustavo");
        student.setEmail("gustavo@teste.com");
        student = userRepository.save(student);

        instructor = new User();
        instructor.setName("Professora");
        instructor.setEmail("professora@teste.com");
        instructor = userRepository.save(instructor);

        course = new Course();
        course.setInstitution(institution);
        course.setName("AWS re/Start");
        course.setDescription("Treinamento em computação em nuvem");
        course.setStartDate(LocalDate.now());
        course.setEndDate(LocalDate.now().plusMonths(3));
        course.setStatus(CourseStatus.ACTIVE);
        course = courseRepository.save(course);

        CourseMembership studentMembership = new CourseMembership();
        studentMembership.setCourse(course);
        studentMembership.setUser(student);
        studentMembership.setRole(CourseRole.STUDENT);
        courseMembershipRepository.save(studentMembership);

        CourseMembership instructorMembership = new CourseMembership();
        instructorMembership.setCourse(course);
        instructorMembership.setUser(instructor);
        instructorMembership.setRole(CourseRole.INSTRUCTOR);
        courseMembershipRepository.save(instructorMembership);

        classSession = new ClassSession();
        classSession.setCourse(course);
        classSession.setTitle("Treinamento AWS");
        classSession.setSessionDate(LocalDate.now());
        classSession.setStartTime(LocalTime.of(9, 0));
        classSession.setEndTime(LocalTime.of(12, 0));
        classSession.setStatus(ClassSessionStatus.COMPLETED);
        classSession = classSessionRepository.save(classSession);
    }

    @Test
    void aulaEmAndamentoDeveManterPresencaPendente() {

        classSession.setStatus(ClassSessionStatus.IN_PROGRESS);
        classSessionRepository.save(classSession);

        AttendanceDecision decision = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                AttendanceStatus.PENDING,
                decision.getStatus()
        );

        assertEquals(
                AttendanceDecisionSource.SYSTEM,
                decision.getDecisionSource()
        );

        assertNull(decision.getDecidedAt());
    }

    @Test
    void nenhumaEvidenciaDeveResultarEmAusencia() {

        AttendanceDecision decision = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                AttendanceStatus.ABSENT,
                decision.getStatus()
        );

        assertEquals(
                AttendanceDecisionSource.SYSTEM,
                decision.getDecisionSource()
        );

        assertNotNull(decision.getDecidedAt());
    }

    @Test
    void somenteConexaoNoMeetDeveExigirRevisao() {

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.MEETING_SESSION,
                EvidenceSource.GOOGLE_MEET,
                null,
                "Aluno esteve conectado à reunião."
        );

        AttendanceDecision decision = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                AttendanceStatus.REVIEW_REQUIRED,
                decision.getStatus()
        );
    }

    @Test
    void meetMaisCheckInDeveConfirmarPresenca() {

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.MEETING_SESSION,
                EvidenceSource.GOOGLE_MEET,
                null,
                "Participação registrada pelo Google Meet."
        );

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL,
                null,
                "Check-in realizado durante a aula."
        );

        AttendanceDecision decision = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                decision.getStatus()
        );
    }

    @Test
    void duasEvidenciasAtivasSemMeetDevemConfirmarPresenca() {

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL,
                null,
                "Check-in realizado."
        );

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.POLL_RESPONSE,
                EvidenceSource.INTERNAL,
                null,
                "Aluno respondeu à enquete da aula."
        );

        AttendanceDecision decision = attendanceService.evaluate(
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

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.TEACHER_CONFIRMATION,
                EvidenceSource.TEACHER,
                null,
                "Professora confirmou que o aluno acompanhou a aula."
        );

        AttendanceDecision decision = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                decision.getStatus()
        );
    }

    @Test
    void umaUnicaEvidenciaAtivaDeveExigirRevisao() {

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL,
                null,
                "Aluno realizou apenas um check-in."
        );

        AttendanceDecision decision = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                AttendanceStatus.REVIEW_REQUIRED,
                decision.getStatus()
        );
    }

    @Test
    void decisaoManualNaoDeveSerSobrescritaPeloSistema() {

        AttendanceDecision manualDecision = new AttendanceDecision();

        manualDecision.setStudent(student);
        manualDecision.setClassSession(classSession);
        manualDecision.setStatus(AttendanceStatus.JUSTIFIED);
        manualDecision.setDecisionSource(
                AttendanceDecisionSource.TEACHER
        );
        manualDecision.setDecidedBy(instructor);
        manualDecision.setReason(
                "Ausência justificada manualmente pela professora."
        );

        manualDecision = attendanceDecisionRepository.save(
                manualDecision
        );

        presenceEvidenceService.register(
                student,
                classSession,
                null,
                PresenceEvidenceType.MEETING_SESSION,
                EvidenceSource.GOOGLE_MEET,
                null,
                "Evidência adicionada posteriormente."
        );

        AttendanceDecision evaluated = attendanceService.evaluate(
                student.getId(),
                classSession.getId()
        );

        assertEquals(
                manualDecision.getId(),
                evaluated.getId()
        );

        assertEquals(
                AttendanceStatus.JUSTIFIED,
                evaluated.getStatus()
        );

        assertEquals(
                AttendanceDecisionSource.TEACHER,
                evaluated.getDecisionSource()
        );

        assertEquals(
                instructor.getId(),
                evaluated.getDecidedBy().getId()
        );
    }
}