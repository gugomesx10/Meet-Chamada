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
class AttendanceReviewServiceTest {

    @Autowired
    private AttendanceReviewService attendanceReviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionMembershipRepository institutionMembershipRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Autowired
    private AttendanceDecisionRepository attendanceDecisionRepository;

    @Autowired
    private AttendanceReviewRepository attendanceReviewRepository;

    private Institution institution;
    private Course course;
    private ClassSession classSession;

    private User student;
    private User instructor;
    private User admin;
    private User outsider;

    private AttendanceDecision decision;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName("Escola da Nuvem");
        institution = institutionRepository.save(institution);

        student = createUser(
                "Gustavo",
                "gustavo@teste.com"
        );

        instructor = createUser(
                "Professora",
                "professora@teste.com"
        );

        admin = createUser(
                "Administrador",
                "admin@teste.com"
        );

        outsider = createUser(
                "Usuário externo",
                "externo@teste.com"
        );

        course = new Course();
        course.setInstitution(institution);
        course.setName("AWS re/Start");
        course.setDescription("Treinamento em computação em nuvem");
        course.setStartDate(LocalDate.now());
        course.setEndDate(LocalDate.now().plusMonths(3));
        course.setStatus(CourseStatus.ACTIVE);
        course = courseRepository.save(course);

        CourseMembership studentMembership =
                new CourseMembership();

        studentMembership.setCourse(course);
        studentMembership.setUser(student);
        studentMembership.setRole(CourseRole.STUDENT);

        courseMembershipRepository.save(studentMembership);

        CourseMembership instructorMembership =
                new CourseMembership();

        instructorMembership.setCourse(course);
        instructorMembership.setUser(instructor);
        instructorMembership.setRole(CourseRole.INSTRUCTOR);

        courseMembershipRepository.save(instructorMembership);

        InstitutionMembership adminMembership =
                new InstitutionMembership();

        adminMembership.setInstitution(institution);
        adminMembership.setUser(admin);
        adminMembership.setRole(InstitutionRole.ADMIN);

        institutionMembershipRepository.save(adminMembership);

        classSession = new ClassSession();
        classSession.setCourse(course);
        classSession.setTitle("Treinamento AWS");
        classSession.setSessionDate(LocalDate.now());
        classSession.setStartTime(LocalTime.of(9, 0));
        classSession.setEndTime(LocalTime.of(12, 0));
        classSession.setStatus(ClassSessionStatus.COMPLETED);

        classSession = classSessionRepository.save(classSession);

        decision = new AttendanceDecision();
        decision.setStudent(student);
        decision.setClassSession(classSession);
        decision.setStatus(AttendanceStatus.REVIEW_REQUIRED);
        decision.setDecisionSource(
                AttendanceDecisionSource.SYSTEM
        );

        decision = attendanceDecisionRepository.save(decision);
    }

    @Test
    void instrutorDevePoderConfirmarPresenca() {

        AttendanceReview review =
                attendanceReviewService.review(
                        decision.getId(),
                        instructor.getId(),
                        AttendanceStatus.CONFIRMED,
                        "Aluno acompanhou a aula e participou da atividade."
                );

        assertNotNull(review.getId());

        assertEquals(
                AttendanceStatus.REVIEW_REQUIRED,
                review.getPreviousStatus()
        );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                review.getNewStatus()
        );

        AttendanceDecision updated =
                attendanceDecisionRepository
                        .findById(decision.getId())
                        .orElseThrow();

        assertEquals(
                AttendanceStatus.CONFIRMED,
                updated.getStatus()
        );

        assertEquals(
                AttendanceDecisionSource.TEACHER,
                updated.getDecisionSource()
        );

        assertEquals(
                instructor.getId(),
                updated.getDecidedBy().getId()
        );
    }

    @Test
    void administradorDaInstituicaoDevePoderRevisarPresenca() {

        AttendanceReview review =
                attendanceReviewService.review(
                        decision.getId(),
                        admin.getId(),
                        AttendanceStatus.JUSTIFIED,
                        "Ausência justificada administrativamente."
                );

        assertNotNull(review.getId());

        AttendanceDecision updated =
                attendanceDecisionRepository
                        .findById(decision.getId())
                        .orElseThrow();

        assertEquals(
                AttendanceStatus.JUSTIFIED,
                updated.getStatus()
        );

        assertEquals(
                AttendanceDecisionSource.ADMIN,
                updated.getDecisionSource()
        );

        assertEquals(
                admin.getId(),
                updated.getDecidedBy().getId()
        );
    }

    @Test
    void alunoNaoDevePoderRevisarPresenca() {

        assertThrows(
                IllegalStateException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        student.getId(),
                        AttendanceStatus.CONFIRMED,
                        "Tentativa inválida."
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDevePoderRevisarPresenca() {

        assertThrows(
                IllegalStateException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        outsider.getId(),
                        AttendanceStatus.CONFIRMED,
                        "Tentativa inválida."
                )
        );
    }

    @Test
    void motivoDaRevisaoDeveSerObrigatorio() {

        assertThrows(
                IllegalArgumentException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        instructor.getId(),
                        AttendanceStatus.CONFIRMED,
                        "   "
                )
        );
    }

    @Test
    void naoDevePermitirMesmoStatusNaRevisao() {

        attendanceReviewService.review(
                decision.getId(),
                instructor.getId(),
                AttendanceStatus.CONFIRMED,
                "Presença confirmada pela professora."
        );

        assertThrows(
                IllegalStateException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        instructor.getId(),
                        AttendanceStatus.CONFIRMED,
                        "Tentativa de aplicar novamente o mesmo status."
                )
        );
    }

    @Test
    void revisaoManualNaoPodeResultarEmPending() {

        assertThrows(
                IllegalArgumentException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        instructor.getId(),
                        AttendanceStatus.PENDING,
                        "Status inválido para revisão manual."
                )
        );
    }

    @Test
    void devePreservarHistoricoDeRevisoes() {

        attendanceReviewService.review(
                decision.getId(),
                instructor.getId(),
                AttendanceStatus.CONFIRMED,
                "Presença confirmada pela professora."
        );

        AttendanceReview secondReview =
                attendanceReviewService.review(
                        decision.getId(),
                        admin.getId(),
                        AttendanceStatus.JUSTIFIED,
                        "Status alterado após análise administrativa."
                );

        var history =
                attendanceReviewRepository
                        .findAllByAttendanceDecisionIdOrderByReviewedAtAsc(
                                decision.getId()
                        );

        assertEquals(2, history.size());

        assertEquals(
                AttendanceStatus.REVIEW_REQUIRED,
                history.get(0).getPreviousStatus()
        );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                history.get(0).getNewStatus()
        );

        assertEquals(
                AttendanceStatus.CONFIRMED,
                secondReview.getPreviousStatus()
        );

        assertEquals(
                AttendanceStatus.JUSTIFIED,
                secondReview.getNewStatus()
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