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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CourseMembershipServiceTest {

    @Autowired
    private CourseMembershipService courseMembershipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionMembershipRepository institutionMembershipRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Institution institution;
    private Course course;
    private User student;
    private User teacher;
    private User admin;
    private User outsider;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName("Escola da Nuvem");

        institution =
                institutionRepository.save(
                        institution
                );

        student = createUser(
                "Gustavo",
                "gustavo@teste.com"
        );

        teacher = createUser(
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
                teacher,
                InstitutionRole.TEACHER
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
                courseRepository.save(course);
    }

    @Test
    void alunoDaInstituicaoDeveSerMatriculadoComoStudent() {

        CourseMembership membership =
                courseMembershipService.create(
                        course.getId(),
                        student.getId(),
                        CourseRole.STUDENT
                );

        assertNotNull(membership.getId());

        assertEquals(
                CourseRole.STUDENT,
                membership.getRole()
        );
    }

    @Test
    void professorDaInstituicaoDeveSerInstrutorDoCurso() {

        CourseMembership membership =
                courseMembershipService.create(
                        course.getId(),
                        teacher.getId(),
                        CourseRole.INSTRUCTOR
                );

        assertEquals(
                CourseRole.INSTRUCTOR,
                membership.getRole()
        );
    }

    @Test
    void administradorPodeSerInstrutorDoCurso() {

        CourseMembership membership =
                courseMembershipService.create(
                        course.getId(),
                        admin.getId(),
                        CourseRole.INSTRUCTOR
                );

        assertEquals(
                CourseRole.INSTRUCTOR,
                membership.getRole()
        );
    }

    @Test
    void alunoDaInstituicaoNaoPodeSerInstrutor() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> courseMembershipService.create(
                        course.getId(),
                        student.getId(),
                        CourseRole.INSTRUCTOR
                )
        );
    }

    @Test
    void professorNaoPodeSerMatriculadoComoAluno() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> courseMembershipService.create(
                        course.getId(),
                        teacher.getId(),
                        CourseRole.STUDENT
                )
        );
    }

    @Test
    void usuarioSemVinculoInstitucionalNaoPodeEntrarNoCurso() {

        assertThrows(
                ForbiddenOperationException.class,
                () -> courseMembershipService.create(
                        course.getId(),
                        outsider.getId(),
                        CourseRole.STUDENT
                )
        );
    }

    @Test
    void naoDevePermitirVinculoDuplicadoNoCurso() {

        courseMembershipService.create(
                course.getId(),
                student.getId(),
                CourseRole.STUDENT
        );

        assertThrows(
                ConflictException.class,
                () -> courseMembershipService.create(
                        course.getId(),
                        student.getId(),
                        CourseRole.STUDENT
                )
        );
    }

    @Test
    void deveEncontrarVinculoDoUsuarioNoCurso() {

        courseMembershipService.create(
                course.getId(),
                student.getId(),
                CourseRole.STUDENT
        );

        CourseMembership membership =
                courseMembershipService.find(
                        course.getId(),
                        student.getId()
                );

        assertEquals(
                CourseRole.STUDENT,
                membership.getRole()
        );
    }

    @Test
    void deveListarMembrosDoCurso() {

        courseMembershipService.create(
                course.getId(),
                student.getId(),
                CourseRole.STUDENT
        );

        courseMembershipService.create(
                course.getId(),
                teacher.getId(),
                CourseRole.INSTRUCTOR
        );

        var memberships =
                courseMembershipService
                        .findByCourse(
                                course.getId()
                        );

        assertEquals(
                2,
                memberships.size()
        );
    }

    @Test
    void papelNoCursoDeveSerObrigatorio() {

        assertThrows(
                BusinessRuleException.class,
                () -> courseMembershipService.create(
                        course.getId(),
                        student.getId(),
                        null
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

        return userRepository.save(user);
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