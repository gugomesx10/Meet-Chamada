package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionMembershipRepository institutionMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    private Institution institution;
    private User admin;
    private User student;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName("Escola da Nuvem");

        institution =
                institutionRepository.save(
                        institution
                );

        admin = new User();
        admin.setName("Administrador");
        admin.setEmail("admin@teste.com");

        admin =
                userRepository.save(
                        admin
                );

        student = new User();
        student.setName("Gustavo");
        student.setEmail("gustavo@teste.com");

        student =
                userRepository.save(
                        student
                );

        createInstitutionMembership(
                admin,
                InstitutionRole.ADMIN
        );

        createInstitutionMembership(
                student,
                InstitutionRole.STUDENT
        );
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
                        course.getId(),
                        admin
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
                course.getId(),
                admin
        );

        Course completed =
                courseService.complete(
                        course.getId(),
                        admin
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
                        course.getId(),
                        admin
                )
        );
    }

    @Test
    void deveCancelarCursoPlanejado() {

        Course course = createCourse();

        Course cancelled =
                courseService.cancel(
                        course.getId(),
                        admin
                );

        assertEquals(
                CourseStatus.CANCELLED,
                cancelled.getStatus()
        );
    }

    @Test
    void cursoConcluidoNaoPodeSerCancelado() {

        Course course = createCourse();

        courseService.activate(
                course.getId(),
                admin
        );

        courseService.complete(
                course.getId(),
                admin
        );

        assertThrows(
                ConflictException.class,
                () -> courseService.cancel(
                        course.getId(),
                        admin
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
                        LocalDate.now().minusDays(1),
                        admin
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
                        LocalDate.now().plusMonths(3),
                        admin
                )
        );
    }

    @Test
    void deveListarCursosDaInstituicaoQuandoUsuarioForAdmin() {

        Course course = createCourse();

        var courses =
                courseService.findByInstitution(
                        institution.getId(),
                        admin
                );

        assertEquals(
                1,
                courses.size()
        );

        assertEquals(
                course.getId(),
                courses.getFirst().getId()
        );
    }

    @Test
    void alunoNaoDeveCriarCurso() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> courseService.create(
                        institution.getId(),
                        "Curso indevido",
                        "Tentativa sem permissão",
                        LocalDate.now(),
                        LocalDate.now().plusMonths(1),
                        student
                )
        );
    }

    @Test
    void alunoNaoDeveAtivarCurso() {

        Course course = createCourse();

        assertThrows(
                ForbiddenOperationException.class,
                () -> courseService.activate(
                        course.getId(),
                        student
                )
        );
    }

    private Course createCourse() {

        return courseService.create(
                institution.getId(),
                "AWS re/Start",
                "Treinamento em computação em nuvem",
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                admin
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
}