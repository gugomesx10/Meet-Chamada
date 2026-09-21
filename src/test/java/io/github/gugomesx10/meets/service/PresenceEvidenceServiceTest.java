package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.*;
import io.github.gugomesx10.meets.entity.enums.*;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
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
class PresenceEvidenceServiceTest {

    @Autowired
    private PresenceEvidenceService presenceEvidenceService;

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
    private SessionBlockRepository sessionBlockRepository;

    @Autowired
    private PresenceEvidenceRepository presenceEvidenceRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private Institution institution;
    private Course course;
    private ClassSession classSession;
    private User instructor;
    private User student;
    private User admin;
    private User outsider;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName(
                "Escola da Nuvem"
        );

        institution =
                institutionRepository.save(
                        institution
                );

        instructor = createUser(
                "Professora",
                "professora@teste.com"
        );

        student = createUser(
                "Gustavo",
                "gustavo@teste.com"
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
                instructor,
                InstitutionRole.TEACHER
        );

        createInstitutionMembership(
                student,
                InstitutionRole.STUDENT
        );

        createInstitutionMembership(
                admin,
                InstitutionRole.ADMIN
        );

        course = new Course();

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
                courseRepository.save(
                        course
                );

        createCourseMembership(
                instructor,
                CourseRole.INSTRUCTOR
        );

        createCourseMembership(
                student,
                CourseRole.STUDENT
        );

        classSession =
                new ClassSession();

        classSession.setCourse(course);
        classSession.setTitle(
                "Aula AWS"
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
                ClassSessionStatus.IN_PROGRESS
        );

        classSession =
                classSessionRepository.save(
                        classSession
                );
    }

    @Test
    void instrutorDeveRegistrarConfirmacaoManual() {

        PresenceEvidence evidence =
                presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                instructor,
                                "Aluno participou da atividade."
                        );

        assertNotNull(
                evidence.getId()
        );

        assertEquals(
                student.getId(),
                evidence.getStudent().getId()
        );

        assertEquals(
                PresenceEvidenceType.TEACHER_CONFIRMATION,
                evidence.getType()
        );

        assertEquals(
                EvidenceSource.TEACHER,
                evidence.getSource()
        );

        assertNotNull(
                evidence.getOccurredAt()
        );
    }

    @Test
    void administradorDaInstituicaoDeveRegistrarConfirmacaoManual() {

        PresenceEvidence evidence =
                presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                admin,
                                "Presença confirmada administrativamente."
                        );

        assertNotNull(
                evidence.getId()
        );

        assertEquals(
                PresenceEvidenceType.TEACHER_CONFIRMATION,
                evidence.getType()
        );

        assertEquals(
                EvidenceSource.TEACHER,
                evidence.getSource()
        );
    }

    @Test
    void alunoNaoDeveRegistrarConfirmacaoManual() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                student,
                                "Tentativa inválida."
                        )
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveRegistrarConfirmacaoManual() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                outsider,
                                "Tentativa inválida."
                        )
        );
    }

    @Test
    void usuarioAlvoPrecisaSerAlunoDoCurso() {

        assertThrows(
                BusinessRuleException.class,
                () -> presenceEvidenceService
                        .registerTeacherConfirmation(
                                instructor.getId(),
                                classSession.getId(),
                                null,
                                instructor,
                                "Instrutor não pode ser tratado como aluno."
                        )
        );
    }

    @Test
    void blocoDevePertencerAMesmaSessao() {

        ClassSession anotherSession =
                new ClassSession();

        anotherSession.setCourse(course);

        anotherSession.setTitle(
                "Outra aula AWS"
        );

        anotherSession.setSessionDate(
                LocalDate.now().plusDays(1)
        );

        anotherSession.setStartTime(
                LocalTime.of(9, 0)
        );

        anotherSession.setEndTime(
                LocalTime.of(12, 0)
        );

        anotherSession.setStatus(
                ClassSessionStatus.SCHEDULED
        );

        anotherSession =
                classSessionRepository.save(
                        anotherSession
                );

        SessionBlock block =
                new SessionBlock();

        block.setClassSession(
                anotherSession
        );

        block.setInstructor(
                instructor
        );

        block.setTitle(
                "Fundamentos de Cloud"
        );

        block.setDescription(
                "Bloco pertencente a outra sessão."
        );

        block.setType(
                SessionBlockType.TECHNICAL
        );

        block.setStartTime(
                LocalTime.of(9, 0)
        );

        block.setEndTime(
                LocalTime.of(10, 0)
        );

        block =
                sessionBlockRepository.save(
                        block
                );

        SessionBlock savedBlock =
                block;

        assertThrows(
                BusinessRuleException.class,
                () -> presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                savedBlock.getId(),
                                instructor,
                                "Tentativa com bloco incorreto."
                        )
        );
    }

    @Test
    void deveAssociarEvidenciaAoBlocoInformado() {

        SessionBlock block =
                new SessionBlock();

        block.setClassSession(
                classSession
        );

        block.setInstructor(
                instructor
        );

        block.setTitle(
                "Treinamento AWS"
        );

        block.setDescription(
                "Bloco técnico da aula."
        );

        block.setType(
                SessionBlockType.TECHNICAL
        );

        block.setStartTime(
                LocalTime.of(10, 30)
        );

        block.setEndTime(
                LocalTime.of(12, 0)
        );

        block =
                sessionBlockRepository.save(
                        block
                );

        PresenceEvidence evidence =
                presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                block.getId(),
                                instructor,
                                "Aluno participou do bloco técnico."
                        );

        assertNotNull(
                evidence.getSessionBlock()
        );

        assertEquals(
                block.getId(),
                evidence.getSessionBlock().getId()
        );
    }

    @Test
    void deveRegistrarAuditoriaDaConfirmacaoManual() {

        PresenceEvidence evidence =
                presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                instructor,
                                "Participação confirmada."
                        );

        var events =
                auditEventRepository
                        .findAllByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                                "PresenceEvidence",
                                evidence.getId()
                        );

        assertEquals(
                1,
                events.size()
        );

        AuditEvent event =
                events.getFirst();

        assertEquals(
                "TEACHER_CONFIRMATION_REGISTERED",
                event.getAction()
        );

        assertEquals(
                instructor.getId(),
                event.getActor().getId()
        );

        assertEquals(
                "PresenceEvidence",
                event.getEntityType()
        );

        assertEquals(
                evidence.getId(),
                event.getEntityId()
        );
    }

    @Test
    void evidenciaRegistradaDeveSerPersistida() {

        PresenceEvidence evidence =
                presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                instructor,
                                "Aluno respondeu durante a aula."
                        );

        PresenceEvidence saved =
                presenceEvidenceRepository
                        .findById(
                                evidence.getId()
                        )
                        .orElseThrow();

        assertEquals(
                PresenceEvidenceType.TEACHER_CONFIRMATION,
                saved.getType()
        );

        assertEquals(
                EvidenceSource.TEACHER,
                saved.getSource()
        );

        assertEquals(
                "Aluno respondeu durante a aula.",
                saved.getDetails()
        );
    }

    @Test
    void alunoDeveConsultarPropriasEvidencias() {

        PresenceEvidence evidence =
                presenceEvidenceService
                        .registerTeacherConfirmation(
                                student.getId(),
                                classSession.getId(),
                                null,
                                instructor,
                                "Participação confirmada."
                        );

        var evidences =
                presenceEvidenceService
                        .findByStudentAndSession(
                                student.getId(),
                                classSession.getId(),
                                student
                        );

        assertEquals(
                1,
                evidences.size()
        );

        assertEquals(
                evidence.getId(),
                evidences.getFirst().getId()
        );
    }

    @Test
    void instrutorDeveListarEvidenciasDaSessao() {

        presenceEvidenceService
                .registerTeacherConfirmation(
                        student.getId(),
                        classSession.getId(),
                        null,
                        instructor,
                        "Participação confirmada."
                );

        var evidences =
                presenceEvidenceService.findBySession(
                        classSession.getId(),
                        instructor
                );

        assertEquals(
                1,
                evidences.size()
        );
    }

    @Test
    void alunoNaoDeveListarEvidenciasDaSessaoInteira() {

        presenceEvidenceService
                .registerTeacherConfirmation(
                        student.getId(),
                        classSession.getId(),
                        null,
                        instructor,
                        "Participação confirmada."
                );

        assertThrows(
                ForbiddenOperationException.class,
                () -> presenceEvidenceService.findBySession(
                        classSession.getId(),
                        student
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveConsultarEvidenciasDoAluno() {

        presenceEvidenceService
                .registerTeacherConfirmation(
                        student.getId(),
                        classSession.getId(),
                        null,
                        instructor,
                        "Participação confirmada."
                );

        assertThrows(
                ForbiddenOperationException.class,
                () -> presenceEvidenceService.findByStudentAndSession(
                        student.getId(),
                        classSession.getId(),
                        outsider
                )
        );
    }

    private User createUser(
            String name,
            String email
    ) {

        User user =
                new User();

        user.setName(name);
        user.setEmail(email);

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