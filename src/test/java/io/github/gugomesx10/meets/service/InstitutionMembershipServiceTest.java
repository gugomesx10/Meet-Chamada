package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class InstitutionMembershipServiceTest {

    @Autowired
    private InstitutionMembershipService institutionMembershipService;

    @Autowired
    private InstitutionMembershipRepository institutionMembershipRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

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

        InstitutionMembership adminMembership =
                new InstitutionMembership();

        adminMembership.setInstitution(
                institution
        );

        adminMembership.setUser(
                admin
        );

        adminMembership.setRole(
                InstitutionRole.ADMIN
        );

        institutionMembershipRepository.save(
                adminMembership
        );
    }

    @Test
    void deveCriarVinculoInstitucional() {

        InstitutionMembership membership =
                institutionMembershipService.create(
                        institution.getId(),
                        student.getId(),
                        InstitutionRole.STUDENT,
                        admin
                );

        assertNotNull(
                membership.getId()
        );

        assertEquals(
                InstitutionRole.STUDENT,
                membership.getRole()
        );

        assertEquals(
                student.getId(),
                membership.getUser().getId()
        );

        assertEquals(
                institution.getId(),
                membership.getInstitution().getId()
        );
    }

    @Test
    void naoDevePermitirVinculoDuplicado() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT,
                admin
        );

        assertThrows(
                ConflictException.class,
                () -> institutionMembershipService.create(
                        institution.getId(),
                        student.getId(),
                        InstitutionRole.STUDENT,
                        admin
                )
        );
    }

    @Test
    void papelInstitucionalDeveSerObrigatorio() {

        assertThrows(
                BusinessRuleException.class,
                () -> institutionMembershipService.create(
                        institution.getId(),
                        student.getId(),
                        null,
                        admin
                )
        );
    }

    @Test
    void deveEncontrarVinculoPorInstituicaoEUsuario() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT,
                admin
        );

        InstitutionMembership membership =
                institutionMembershipService.find(
                        institution.getId(),
                        student.getId(),
                        admin
                );

        assertEquals(
                InstitutionRole.STUDENT,
                membership.getRole()
        );
    }

    @Test
    void usuarioDevePoderConsultarProprioVinculo() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT,
                admin
        );

        InstitutionMembership membership =
                institutionMembershipService.find(
                        institution.getId(),
                        student.getId(),
                        student
                );

        assertEquals(
                student.getId(),
                membership.getUser().getId()
        );

        assertEquals(
                InstitutionRole.STUDENT,
                membership.getRole()
        );
    }

    @Test
    void deveListarMembrosDaInstituicaoQuandoUsuarioForAdmin() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT,
                admin
        );

        var memberships =
                institutionMembershipService
                        .findByInstitution(
                                institution.getId(),
                                admin
                        );

        assertEquals(
                2,
                memberships.size()
        );
    }

    @Test
    void alunoNaoDeveAdicionarMembroNaInstituicao() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT,
                admin
        );

        User anotherUser = new User();
        anotherUser.setName("Outro aluno");
        anotherUser.setEmail("outro@teste.com");

        anotherUser =
                userRepository.save(
                        anotherUser
                );

        User savedAnotherUser = anotherUser;

        assertThrows(
                ForbiddenOperationException.class,
                () -> institutionMembershipService.create(
                        institution.getId(),
                        savedAnotherUser.getId(),
                        InstitutionRole.STUDENT,
                        student
                )
        );
    }

    @Test
    void alunoNaoDeveListarTodosOsMembrosDaInstituicao() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT,
                admin
        );

        assertThrows(
                ForbiddenOperationException.class,
                () -> institutionMembershipService.findByInstitution(
                        institution.getId(),
                        student
                )
        );
    }

    @Test
    void vinculoInexistenteDeveGerarErro() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> institutionMembershipService.find(
                        institution.getId(),
                        student.getId(),
                        admin
                )
        );
    }
}