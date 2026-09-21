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
    private InstitutionMembershipRepository institutionMembershipRepository;

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

    private Institution institution;
    private User instructor;
    private User student;
    private User admin;
    private User outsider;
    private Course course;
    private ClassSession classSession;

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

        course =
                new Course();

        course.setInstitution(
                institution
        );

        course.setName(
                "AWS re/Start"
        );

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

        classSession.setCourse(
                course
        );

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
    void deveAbrirCheckInQuandoUsuarioForInstrutor() {

        CheckInWindow window =
                openCheckInAsInstructor();

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
    void administradorDevePoderAbrirCheckIn() {

        CheckInWindow window =
                checkInService.openCheckIn(
                        classSession.getId(),
                        null,
                        admin,
                        Duration.ofMinutes(3)
                );

        assertNotNull(
                window.getId()
        );

        assertEquals(
                admin.getId(),
                window.getOpenedBy().getId()
        );
    }

    @Test
    void deveRegistrarRespostaEGerarEvidencia() {

        CheckInWindow window =
                openCheckInAsInstructor();

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
                openCheckInAsInstructor();

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

    @Test
    void instrutorNaoDeveResponderCheckInComoAluno() {

        CheckInWindow window =
                openCheckInAsInstructor();

        assertThrows(
                ForbiddenOperationException.class,
                () -> checkInService.respond(
                        window.getId(),
                        instructor
                )
        );
    }

    @Test
    void alunoDeveListarCheckInsDaPropriaAula() {

        CheckInWindow window =
                openCheckInAsInstructor();

        var windows =
                checkInService.findBySession(
                        classSession.getId(),
                        student
                );

        assertEquals(
                1,
                windows.size()
        );

        assertEquals(
                window.getId(),
                windows.getFirst().getId()
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveListarCheckInsDaAula() {

        openCheckInAsInstructor();

        assertThrows(
                ForbiddenOperationException.class,
                () -> checkInService.findBySession(
                        classSession.getId(),
                        outsider
                )
        );
    }

    @Test
    void instrutorDeveListarRespostasDoCheckIn() {

        CheckInWindow window =
                openCheckInAsInstructor();

        CheckInResponse response =
                checkInService.respond(
                        window.getId(),
                        student
                );

        var responses =
                checkInService.findResponses(
                        window.getId(),
                        instructor
                );

        assertEquals(
                1,
                responses.size()
        );

        assertEquals(
                response.getId(),
                responses.getFirst().getId()
        );
    }

    @Test
    void administradorDeveListarRespostasDoCheckIn() {

        CheckInWindow window =
                openCheckInAsInstructor();

        checkInService.respond(
                window.getId(),
                student
        );

        var responses =
                checkInService.findResponses(
                        window.getId(),
                        admin
                );

        assertEquals(
                1,
                responses.size()
        );
    }

    @Test
    void alunoNaoDeveListarRespostasDeTodos() {

        CheckInWindow window =
                openCheckInAsInstructor();

        checkInService.respond(
                window.getId(),
                student
        );

        assertThrows(
                ForbiddenOperationException.class,
                () -> checkInService.findResponses(
                        window.getId(),
                        student
                )
        );
    }

    @Test
    void administradorDevePoderFecharCheckIn() {

        CheckInWindow window =
                openCheckInAsInstructor();

        CheckInWindow closed =
                checkInService.closeCheckIn(
                        window.getId(),
                        admin
                );

        assertEquals(
                CheckInStatus.CLOSED,
                closed.getStatus()
        );
    }

    @Test
    void instrutorDevePoderCancelarCheckIn() {

        CheckInWindow window =
                openCheckInAsInstructor();

        CheckInWindow cancelled =
                checkInService.cancelCheckIn(
                        window.getId(),
                        instructor
                );

        assertEquals(
                CheckInStatus.CANCELLED,
                cancelled.getStatus()
        );
    }

    private CheckInWindow openCheckInAsInstructor() {

        return checkInService.openCheckIn(
                classSession.getId(),
                null,
                instructor,
                Duration.ofMinutes(3)
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