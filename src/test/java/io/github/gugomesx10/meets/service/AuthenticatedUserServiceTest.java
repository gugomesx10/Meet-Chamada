package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.TestcontainersConfiguration;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthenticatedUserServiceTest {

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private UserRepository userRepository;

    private User oauthUser;
    private User devUser;

    @BeforeEach
    void setUp() {

        oauthUser = new User();
        oauthUser.setName("Usuário OAuth");
        oauthUser.setEmail("oauth@teste.com");
        oauthUser.setExternalId("google-sub-123");

        oauthUser =
                userRepository.save(
                        oauthUser
                );

        devUser = new User();
        devUser.setName("Usuário Dev");
        devUser.setEmail("dev@teste.com");

        devUser =
                userRepository.save(
                        devUser
                );
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void deveIdentificarUsuarioAutenticadoPorOidc() {

        OidcUser oidcUser =
                createOidcUser(
                        "google-sub-123",
                        "oauth@teste.com"
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        oidcUser,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );

        User currentUser =
                authenticatedUserService.getCurrentUser();

        assertEquals(
                oauthUser.getId(),
                currentUser.getId()
        );

        assertEquals(
                "google-sub-123",
                currentUser.getExternalId()
        );
    }

    @Test
    void deveIdentificarUsuarioDevPeloEmailDoBasicAuth() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "DEV@TESTE.COM",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_DEV"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );

        User currentUser =
                authenticatedUserService.getCurrentUser();

        assertEquals(
                devUser.getId(),
                currentUser.getId()
        );

        assertEquals(
                "dev@teste.com",
                currentUser.getEmail()
        );
    }

    @Test
    void usuarioNaoAutenticadoDeveGerarErro() {

        SecurityContextHolder.clearContext();

        assertThrows(
                ForbiddenOperationException.class,
                () -> authenticatedUserService.getCurrentUser()
        );
    }

    @Test
    void usuarioDevInexistenteDeveGerarErro() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "inexistente@teste.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_DEV"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> authenticatedUserService.getCurrentUser()
        );
    }

    private OidcUser createOidcUser(
            String subject,
            String email
    ) {

        Instant now =
                Instant.now();

        OidcIdToken idToken =
                new OidcIdToken(
                        "token-value",
                        now,
                        now.plusSeconds(300),
                        Map.of(
                                "sub", subject,
                                "email", email
                        )
                );

        return new DefaultOidcUser(
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_USER"
                        )
                ),
                idToken,
                "sub"
        );
    }
}