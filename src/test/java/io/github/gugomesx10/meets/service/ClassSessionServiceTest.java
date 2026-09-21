package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.CourseRepository;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
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
class ClassSessionServiceTest {

    @Autowired
    private ClassSessionService classSessionService;

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
    private UserRepository userRepository;

    private Institution institution;
    private Course course;
    private User instructor;
    private User student;
    private User outsider;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName("Escola da Nuvem");

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

        course = new Course();

        course.setInstitution(institution);
        course.setName("AWS re/Start");
        course.setDescription(
                "Treinamento em computação em nuvem"
        );
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

        createCourseMembership(
                instructor,
                CourseRole.INSTRUCTOR
        );

        createCourseMembership(
                student,
                CourseRole.STUDENT
        );
    }

    @Test
    void deveCriarAulaComoScheduled() {

        ClassSession session =
                classSessionService.create(
                        course.getId(),
                        "Treinamento AWS",
                        LocalDate.now(),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0),
                        instructor
                );

        assertNotNull(session.getId());

        assertEquals(
                ClassSessionStatus.SCHEDULED,
                session.getStatus()
        );

        assertEquals(
                course.getId(),
                session.getCourse().getId()
        );

        assertEquals(
                "Treinamento AWS",
                session.getTitle()
        );
    }

    @Test
    void horarioInicialDeveSerAnteriorAoFinal() {

        assertThrows(
                BusinessRuleException.class,
                () -> classSessionService.create(
                        course.getId(),
                        "Aula inválida",
                        LocalDate.now(),
                        LocalTime.of(12, 0),
                        LocalTime.of(9, 0),
                        instructor
                )
        );
    }

    @Test
    void aulaNaoPodeOcorrerAntesDoInicioDoCurso() {

        assertThrows(
                BusinessRuleException.class,
                () -> classSessionService.create(
                        course.getId(),
                        "Aula antecipada",
                        course.getStartDate().minusDays(1),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0),
                        instructor
                )
        );
    }

    @Test
    void aulaNaoPodeOcorrerDepoisDoFimDoCurso() {

        assertThrows(
                BusinessRuleException.class,
                () -> classSessionService.create(
                        course.getId(),
                        "Aula fora do período",
                        course.getEndDate().plusDays(1),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0),
                        instructor
                )
        );
    }

    @Test
    void deveIniciarAulaAgendada() {

        ClassSession session =
                createSession();

        ClassSession started =
                classSessionService.start(
                        session.getId(),
                        instructor
                );

        assertEquals(
                ClassSessionStatus.IN_PROGRESS,
                started.getStatus()
        );
    }

    @Test
    void deveConcluirAulaEmAndamento() {

        ClassSession session =
                createSession();

        classSessionService.start(
                session.getId(),
                instructor
        );

        ClassSession completed =
                classSessionService.complete(
                        session.getId(),
                        instructor
                );

        assertEquals(
                ClassSessionStatus.COMPLETED,
                completed.getStatus()
        );
    }

    @Test
    void naoDeveConcluirAulaQueNaoEstaEmAndamento() {

        ClassSession session =
                createSession();

        assertThrows(
                ConflictException.class,
                () -> classSessionService.complete(
                        session.getId(),
                        instructor
                )
        );
    }

    @Test
    void deveCancelarAulaAgendada() {

        ClassSession session =
                createSession();

        ClassSession cancelled =
                classSessionService.cancel(
                        session.getId(),
                        instructor
                );

        assertEquals(
                ClassSessionStatus.CANCELLED,
                cancelled.getStatus()
        );
    }

    @Test
    void aulaConcluidaNaoPodeSerCancelada() {

        ClassSession session =
                createSession();

        classSessionService.start(
                session.getId(),
                instructor
        );

        classSessionService.complete(
                session.getId(),
                instructor
        );

        assertThrows(
                ConflictException.class,
                () -> classSessionService.cancel(
                        session.getId(),
                        instructor
                )
        );
    }

    @Test
    void cursoCanceladoNaoDeveAceitarNovaAula() {

        course.setStatus(
                CourseStatus.CANCELLED
        );

        courseRepository.save(
                course
        );

        assertThrows(
                ConflictException.class,
                () -> classSessionService.create(
                        course.getId(),
                        "Nova aula",
                        LocalDate.now(),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0),
                        instructor
                )
        );
    }

    @Test
    void alunoDevePoderConsultarAulaDoCurso() {

        ClassSession session =
                createSession();

        ClassSession found =
                classSessionService.findById(
                        session.getId(),
                        student
                );

        assertEquals(
                session.getId(),
                found.getId()
        );
    }

    @Test
    void alunoNaoDeveCriarAula() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> classSessionService.create(
                        course.getId(),
                        "Aula indevida",
                        LocalDate.now(),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0),
                        student
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveConsultarAula() {

        ClassSession session =
                createSession();

        assertThrows(
                ForbiddenOperationException.class,
                () -> classSessionService.findById(
                        session.getId(),
                        outsider
                )
        );
    }

    private ClassSession createSession() {

        return classSessionService.create(
                course.getId(),
                "Aula AWS",
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                instructor
        );
    }

    private User createUser(
            String name,
            String email
    ) {

        User user = new User();

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

        membership.setInstitution(institution);
        membership.setUser(user);
        membership.setRole(role);

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

        membership.setCourse(course);
        membership.setUser(user);
        membership.setRole(role);

        return courseMembershipRepository.save(
                membership
        );
    }
}