package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseRepository;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
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
    private CourseRepository courseRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    private Course course;

    @BeforeEach
    void setUp() {

        Institution institution = new Institution();
        institution.setName("Escola da Nuvem");

        institution =
                institutionRepository.save(institution);

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
        course.setStatus(CourseStatus.ACTIVE);

        course = courseRepository.save(course);
    }

    @Test
    void deveCriarAulaComoScheduled() {

        ClassSession session =
                classSessionService.create(
                        course.getId(),
                        "Treinamento AWS",
                        LocalDate.now(),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
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
                        LocalTime.of(9, 0)
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
                        LocalTime.of(12, 0)
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
                        LocalTime.of(12, 0)
                )
        );
    }

    @Test
    void deveIniciarAulaAgendada() {

        ClassSession session = createSession();

        ClassSession started =
                classSessionService.start(
                        session.getId()
                );

        assertEquals(
                ClassSessionStatus.IN_PROGRESS,
                started.getStatus()
        );
    }

    @Test
    void deveConcluirAulaEmAndamento() {

        ClassSession session = createSession();

        classSessionService.start(
                session.getId()
        );

        ClassSession completed =
                classSessionService.complete(
                        session.getId()
                );

        assertEquals(
                ClassSessionStatus.COMPLETED,
                completed.getStatus()
        );
    }

    @Test
    void naoDeveConcluirAulaQueNaoEstaEmAndamento() {

        ClassSession session = createSession();

        assertThrows(
                ConflictException.class,
                () -> classSessionService.complete(
                        session.getId()
                )
        );
    }

    @Test
    void deveCancelarAulaAgendada() {

        ClassSession session = createSession();

        ClassSession cancelled =
                classSessionService.cancel(
                        session.getId()
                );

        assertEquals(
                ClassSessionStatus.CANCELLED,
                cancelled.getStatus()
        );
    }

    @Test
    void aulaConcluidaNaoPodeSerCancelada() {

        ClassSession session = createSession();

        classSessionService.start(
                session.getId()
        );

        classSessionService.complete(
                session.getId()
        );

        assertThrows(
                ConflictException.class,
                () -> classSessionService.cancel(
                        session.getId()
                )
        );
    }

    @Test
    void cursoCanceladoNaoDeveAceitarNovaAula() {

        course.setStatus(CourseStatus.CANCELLED);
        courseRepository.save(course);

        assertThrows(
                ConflictException.class,
                () -> classSessionService.create(
                        course.getId(),
                        "Nova aula",
                        LocalDate.now(),
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
                )
        );
    }

    private ClassSession createSession() {

        return classSessionService.create(
                course.getId(),
                "Aula AWS",
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );
    }
}