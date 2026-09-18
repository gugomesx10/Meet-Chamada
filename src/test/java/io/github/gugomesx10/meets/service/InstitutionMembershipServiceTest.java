package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
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
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRepository userRepository;

    private Institution institution;
    private User student;

    @BeforeEach
    void setUp() {

        institution = new Institution();
        institution.setName("Escola da Nuvem");
        institution =
                institutionRepository.save(institution);

        student = new User();
        student.setName("Gustavo");
        student.setEmail("gustavo@teste.com");
        student =
                userRepository.save(student);
    }

    @Test
    void deveCriarVinculoInstitucional() {

        InstitutionMembership membership =
                institutionMembershipService.create(
                        institution.getId(),
                        student.getId(),
                        InstitutionRole.STUDENT
                );

        assertNotNull(membership.getId());

        assertEquals(
                InstitutionRole.STUDENT,
                membership.getRole()
        );
    }

    @Test
    void naoDevePermitirVinculoDuplicado() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT
        );

        assertThrows(
                ConflictException.class,
                () -> institutionMembershipService.create(
                        institution.getId(),
                        student.getId(),
                        InstitutionRole.STUDENT
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
                        null
                )
        );
    }

    @Test
    void deveEncontrarVinculoPorInstituicaoEUsuario() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT
        );

        InstitutionMembership membership =
                institutionMembershipService.find(
                        institution.getId(),
                        student.getId()
                );

        assertEquals(
                InstitutionRole.STUDENT,
                membership.getRole()
        );
    }

    @Test
    void deveListarMembrosDaInstituicao() {

        institutionMembershipService.create(
                institution.getId(),
                student.getId(),
                InstitutionRole.STUDENT
        );

        var memberships =
                institutionMembershipService
                        .findByInstitution(
                                institution.getId()
                        );

        assertEquals(1, memberships.size());
    }

    @Test
    void vinculoInexistenteDeveGerarErro() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> institutionMembershipService.find(
                        institution.getId(),
                        student.getId()
                )
        );
    }
}