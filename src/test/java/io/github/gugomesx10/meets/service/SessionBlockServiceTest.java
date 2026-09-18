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
    private CourseRepository courseRepository;

    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    private User instructor;
    private User student;
    private User outsider;
    private Course course;
    private ClassSession classSession;

    @BeforeEach
    void setUp() {

        Institution institution =
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

        course = courseRepository.save(course);

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
                        LocalTime.of(10, 20)
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
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void usuarioSemVinculoNaoDeveCriarBloco() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> sessionBlockService.create(
                        classSession.getId(),
                        outsider.getId(),
                        "Bloco inválido",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
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
                        LocalTime.of(10, 0)
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
                        LocalTime.of(12, 30)
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
                        LocalTime.of(10, 0)
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
                LocalTime.of(10, 20)
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
                        LocalTime.of(11, 0)
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
                LocalTime.of(10, 0)
        );

        SessionBlock second =
                sessionBlockService.create(
                        classSession.getId(),
                        instructor.getId(),
                        "Segundo bloco",
                        null,
                        SessionBlockType.TECHNICAL,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                );

        assertNotNull(second.getId());
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
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void deveRetornarBlocosOrdenadosPorHorario() {

        sessionBlockService.create(
                classSession.getId(),
                instructor.getId(),
                "Segundo bloco",
                null,
                SessionBlockType.TECHNICAL,
                LocalTime.of(10, 30),
                LocalTime.of(12, 0)
        );

        sessionBlockService.create(
                classSession.getId(),
                instructor.getId(),
                "Primeiro bloco",
                null,
                SessionBlockType.THEORETICAL,
                LocalTime.of(9, 0),
                LocalTime.of(10, 20)
        );

        var blocks =
                sessionBlockService.findBySession(
                        classSession.getId()
                );

        assertEquals(2, blocks.size());

        assertEquals(
                "Primeiro bloco",
                blocks.get(0).getTitle()
        );

        assertEquals(
                "Segundo bloco",
                blocks.get(1).getTitle()
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