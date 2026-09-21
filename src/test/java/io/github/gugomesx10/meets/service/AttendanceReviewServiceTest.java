package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.*;
import io.github.gugomesx10.meets.entity.enums.*;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
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

    private Institution institution;
    private Course course;
    private ClassSession classSession;
    private User student;
    private User anotherStudent;
    private User instructor;
    private User admin;
    private User outsider;
    private AttendanceDecision decision;

    @BeforeEach
    void setUp() {

        institution =
                new Institution();

        institution.setName(
                "Escola da Nuvem"
        );

        institution =
                institutionRepository.save(
                        institution
                );

        student = createUser(
                "Gustavo",
                "gustavo@teste.com"
        );

        anotherStudent = createUser(
                "Outro aluno",
                "outro@teste.com"
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

        createInstitutionMembership(
                student,
                InstitutionRole.STUDENT
        );

        createInstitutionMembership(
                anotherStudent,
                InstitutionRole.STUDENT
        );

        createInstitutionMembership(
                instructor,
                InstitutionRole.TEACHER
        );

        createInstitutionMembership(
                admin,
                InstitutionRole.ADMIN
        );

        course =
                new Course();

        course.setInstitution(
                institution
        );

        course.setName(
                "AWS re/Start"
        );

        course.setDescription(
                "Treinamento em computação em nuvem"
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
                courseRepository.save(
                        course
                );

        createCourseMembership(
                student,
                CourseRole.STUDENT
        );

        createCourseMembership(
                anotherStudent,
                CourseRole.STUDENT
        );

        createCourseMembership(
                instructor,
                CourseRole.INSTRUCTOR
        );

        classSession =
                new ClassSession();

        classSession.setCourse(
                course
        );

        classSession.setTitle(
                "Treinamento AWS"
        );

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

        decision =
                createDecision(
                        student
                );
    }

    @Test
    void instrutorDevePoderConfirmarPresenca() {

        AttendanceReview review =
                attendanceReviewService.review(
                        decision.getId(),
                        instructor,
                        AttendanceStatus.CONFIRMED,
                        "Aluno acompanhou a aula e participou da atividade."
                );

        assertNotNull(
                review.getId()
        );

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
                        .findById(
                                decision.getId()
                        )
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
                        admin,
                        AttendanceStatus.JUSTIFIED,
                        "Ausência justificada administrativamente."
                );

        assertNotNull(
                review.getId()
        );

        AttendanceDecision updated =
                attendanceDecisionRepository
                        .findById(
                                decision.getId()
                        )
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
                ForbiddenOperationException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        student,
                        AttendanceStatus.CONFIRMED,
                        "Tentativa inválida."
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDevePoderRevisarPresenca() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        outsider,
                        AttendanceStatus.CONFIRMED,
                        "Tentativa inválida."
                )
        );
    }

    @Test
    void motivoDaRevisaoDeveSerObrigatorio() {

        assertThrows(
                BusinessRuleException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        instructor,
                        AttendanceStatus.CONFIRMED,
                        "   "
                )
        );
    }

    @Test
    void naoDevePermitirMesmoStatusNaRevisao() {

        attendanceReviewService.review(
                decision.getId(),
                instructor,
                AttendanceStatus.CONFIRMED,
                "Presença confirmada pela professora."
        );

        assertThrows(
                ConflictException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        instructor,
                        AttendanceStatus.CONFIRMED,
                        "Tentativa de aplicar novamente o mesmo status."
                )
        );
    }

    @Test
    void revisaoManualNaoPodeResultarEmPending() {

        assertThrows(
                BusinessRuleException.class,
                () -> attendanceReviewService.review(
                        decision.getId(),
                        instructor,
                        AttendanceStatus.PENDING,
                        "Status inválido para revisão manual."
                )
        );
    }

    @Test
    void devePreservarHistoricoDeRevisoes() {

        attendanceReviewService.review(
                decision.getId(),
                instructor,
                AttendanceStatus.CONFIRMED,
                "Presença confirmada pela professora."
        );

        AttendanceReview secondReview =
                attendanceReviewService.review(
                        decision.getId(),
                        admin,
                        AttendanceStatus.JUSTIFIED,
                        "Status alterado após análise administrativa."
                );

        var history =
                attendanceReviewService.findHistory(
                        decision.getId(),
                        instructor
                );

        assertEquals(
                2,
                history.size()
        );

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

    @Test
    void alunoDeveConsultarProprioHistorico() {

        AttendanceReview review =
                attendanceReviewService.review(
                        decision.getId(),
                        instructor,
                        AttendanceStatus.CONFIRMED,
                        "Presença confirmada."
                );

        var history =
                attendanceReviewService.findHistory(
                        decision.getId(),
                        student
                );

        assertEquals(
                1,
                history.size()
        );

        assertEquals(
                review.getId(),
                history.getFirst().getId()
        );
    }

    @Test
    void instrutorDeveConsultarHistoricoDoAluno() {

        attendanceReviewService.review(
                decision.getId(),
                instructor,
                AttendanceStatus.CONFIRMED,
                "Presença confirmada."
        );

        var history =
                attendanceReviewService.findHistory(
                        decision.getId(),
                        instructor
                );

        assertEquals(
                1,
                history.size()
        );
    }

    @Test
    void administradorDeveConsultarHistoricoDoAluno() {

        attendanceReviewService.review(
                decision.getId(),
                instructor,
                AttendanceStatus.CONFIRMED,
                "Presença confirmada."
        );

        var history =
                attendanceReviewService.findHistory(
                        decision.getId(),
                        admin
                );

        assertEquals(
                1,
                history.size()
        );
    }

    @Test
    void outroAlunoNaoDeveConsultarHistorico() {

        attendanceReviewService.review(
                decision.getId(),
                instructor,
                AttendanceStatus.CONFIRMED,
                "Presença confirmada."
        );

        assertThrows(
                ForbiddenOperationException.class,
                () -> attendanceReviewService.findHistory(
                        decision.getId(),
                        anotherStudent
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveConsultarHistorico() {

        attendanceReviewService.review(
                decision.getId(),
                instructor,
                AttendanceStatus.CONFIRMED,
                "Presença confirmada."
        );

        assertThrows(
                ForbiddenOperationException.class,
                () -> attendanceReviewService.findHistory(
                        decision.getId(),
                        outsider
                )
        );
    }

    private AttendanceDecision createDecision(
            User targetStudent
    ) {

        AttendanceDecision attendanceDecision =
                new AttendanceDecision();

        attendanceDecision.setStudent(
                targetStudent
        );

        attendanceDecision.setClassSession(
                classSession
        );

        attendanceDecision.setStatus(
                AttendanceStatus.REVIEW_REQUIRED
        );

        attendanceDecision.setDecisionSource(
                AttendanceDecisionSource.SYSTEM
        );

        return attendanceDecisionRepository.save(
                attendanceDecision
        );
    }

    private User createUser(
            String name,
            String email
    ) {

        User user =
                new User();

        user.setName(
                name
        );

        user.setEmail(
                email
        );

        return userRepository.save(
                user
        );
    }

    private InstitutionMembership createInstitutionMembership(
            User user,
            InstitutionRole role
    ) {

        InstitutionMembership membership =
                new InstitutionMembership();

        membership.setInstitution(
                institution
        );

        membership.setUser(
                user
        );

        membership.setRole(
                role
        );

        return institutionMembershipRepository.save(
                membership
        );
    }

    private CourseMembership createCourseMembership(
            User user,
            CourseRole role
    ) {

        CourseMembership membership =
                new CourseMembership();

        membership.setCourse(
                course
        );

        membership.setUser(
                user
        );

        membership.setRole(
                role
        );

        return courseMembershipRepository.save(
                membership
        );
    }
}