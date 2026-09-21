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
class SessionBlockServiceTest {

    @Autowired
    private SessionBlockService sessionBlockService;

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

    private Institution institution;
    private User instructor;
    private User student;
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
                "Treinamento AWS"
        );
        course.setStartDate(LocalDate.now());
        course.setEndDate(
                LocalDate.now().plusMonths(3)
        );
        course.setStatus(
                CourseStatus.ACTIVE
        );

        course =
                courseRepository.save(course);

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
                "Manhã AWS re/Start"
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
                ClassSessionStatus.SCHEDULED
        );

        classSession =
                classSessionRepository.save(
                        classSession
                );
    }

    @Test
    void deveCriarBlocoParaInstrutorDoCurso() {

        SessionBlock block =
                sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Desenvolvimento profissional",
                        "Currículo e empregabilidade",
                        SessionBlockType.THEORETICAL,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 20),
                        instructor
                );

        assertNotNull(block.getId());

        assertEquals(
                instructor.getId(),
                block.getInstructor().getId()
        );

        assertEquals(
                classSession.getId(),
                block.getClassSession().getId()
        );
    }

    @Test
    void alunoNaoDevePoderSerInstrutorDoBloco() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        student.getId(),
                        "Bloco inválido",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        instructor
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveSerInstrutorDoBloco() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        outsider.getId(),
                        "Bloco inválido",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        instructor
                )
        );
    }

    @Test
    void alunoNaoDeveCriarBlocoMesmoComInstrutorValido() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Tentativa inválida",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        student
                )
        );
    }

    @Test
    void blocoNaoPodeComecarAntesDaAula() {

        assertThrows(
                BusinessRuleException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Bloco inválido",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(8, 30),
                        LocalTime.of(10, 0),
                        instructor
                )
        );
    }

    @Test
    void blocoNaoPodeTerminarDepoisDaAula() {

        assertThrows(
                BusinessRuleException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Bloco inválido",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 30),
                        instructor
                )
        );
    }

    @Test
    void horarioInicialDoBlocoDeveSerAnteriorAoFinal() {

        assertThrows(
                BusinessRuleException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Bloco inválido",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(10, 0),
                        LocalTime.of(10, 0),
                        instructor
                )
        );
    }

    @Test
    void naoDevePermitirSobreposicaoDeBlocos() {

        sessionBlockService.create(
                classSession.getId(),
                instructor.getId(),
                "Desenvolvimento profissional",
                null,
                SessionBlockType.THEORETICAL,
                LocalTime.of(9, 0),
                LocalTime.of(10, 20),
                instructor
        );

        assertThrows(
                ConflictException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Treinamento AWS",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        instructor
                )
        );
    }

    @Test
    void devePermitirBlocosAdjacentes() {

        sessionBlockService.create(
                classSession.getId(),
                instructor.getId(),
                "Primeiro bloco",
                null,
                SessionBlockType.THEORETICAL,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                instructor
        );

        SessionBlock second =
                sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Segundo bloco",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        instructor
                );

        assertNotNull(
                second.getId()
        );
    }

    @Test
    void aulaConcluidaNaoDeveAceitarNovoBloco() {

        classSession.setStatus(
                ClassSessionStatus.COMPLETED
        );

        classSessionRepository.save(
                classSession
        );

        assertThrows(
                ConflictException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Novo bloco",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        instructor
                )
        );
    }

    @Test
    void alunoDeveConsultarBlocosDoCurso() {

        sessionBlockService.create(
                classSession.getId(),
                instructor.getId(),
                "Segundo bloco",
                null,
                SessionBlockType.TECHNICAL,
                LocalTime.of(10, 30),
                LocalTime.of(12, 0),
                instructor
        );

        sessionBlockService.create(
                classSession.getId(),
                instructor.getId(),
                "Primeiro bloco",
                null,
                SessionBlockType.THEORETICAL,
                LocalTime.of(9, 0),
                LocalTime.of(10, 20),
                instructor
        );

        var blocks =
                sessionBlockService.findBySession(
                        classSession.getId(),
                        student
                );

        assertEquals(
                2,
                blocks.size()
        );

        assertEquals(
                "Primeiro bloco",
                blocks.get(0).getTitle()
        );

        assertEquals(
                "Segundo bloco",
                blocks.get(1).getTitle()
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveConsultarBlocos() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> sessionBlockService.findBySession(
                        classSession.getId(),
                        outsider
                )
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