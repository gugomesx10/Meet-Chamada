package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private InstitutionRepository institutionRepository;

    private Institution institution;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName("Escola da Nuvem");

        institution =
                institutionRepository.save(institution);
    }

    @Test
    void deveCriarCursoComoPlanned() {

        Course course = createCourse();

        assertNotNull(course.getId());

        assertEquals(
                CourseStatus.PLANNED,
                course.getStatus()
        );
    }

    @Test
    void deveAtivarCursoPlanejado() {

        Course course = createCourse();

        Course activated =
                courseService.activate(
                        course.getId()
                );

        assertEquals(
                CourseStatus.ACTIVE,
                activated.getStatus()
        );
    }

    @Test
    void deveConcluirCursoAtivo() {

        Course course = createCourse();

        courseService.activate(
                course.getId()
        );

        Course completed =
                courseService.complete(
                        course.getId()
                );

        assertEquals(
                CourseStatus.COMPLETED,
                completed.getStatus()
        );
    }

    @Test
    void naoDeveConcluirCursoPlanejado() {

        Course course = createCourse();

        assertThrows(
                ConflictException.class,
                () -> courseService.complete(
                        course.getId()
                )
        );
    }

    @Test
    void deveCancelarCursoPlanejado() {

        Course course = createCourse();

        Course cancelled =
                courseService.cancel(
                        course.getId()
                );

        assertEquals(
                CourseStatus.CANCELLED,
                cancelled.getStatus()
        );
    }

    @Test
    void cursoConcluidoNaoPodeSerCancelado() {

        Course course = createCourse();

        courseService.activate(course.getId());
        courseService.complete(course.getId());

        assertThrows(
                ConflictException.class,
                () -> courseService.cancel(
                        course.getId()
                )
        );
    }

    @Test
    void dataFinalNaoPodeSerAnteriorAInicial() {

        assertThrows(
                BusinessRuleException.class,
                () -> courseService.create(
                        institution.getId(),
                        "AWS re/Start",
                        "Treinamento AWS",
                        LocalDate.now(),
                        LocalDate.now().minusDays(1)
                )
        );
    }

    @Test
    void nomeDoCursoDeveSerObrigatorio() {

        assertThrows(
                BusinessRuleException.class,
                () -> courseService.create(
                        institution.getId(),
                        "   ",
                        "Treinamento AWS",
                        LocalDate.now(),
                        LocalDate.now().plusMonths(3)
                )
        );
    }

    @Test
    void deveListarCursosDaInstituicao() {

        Course course = createCourse();

        var courses =
                courseService.findByInstitution(
                        institution.getId()
                );

        assertEquals(1, courses.size());

        assertEquals(
                course.getId(),
                courses.getFirst().getId()
        );
    }

    private Course createCourse() {

        return courseService.create(
                institution.getId(),
                "AWS re/Start",
                "Treinamento em computação em nuvem",
                LocalDate.now(),
                LocalDate.now().plusMonths(3)
        );
    }
}