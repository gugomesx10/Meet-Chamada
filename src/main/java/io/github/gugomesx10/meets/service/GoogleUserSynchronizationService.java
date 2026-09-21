package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GoogleUserSynchronizationService {

    private final UserRepository userRepository;

    @Transactional
    public User synchronize(OidcUser oidcUser) {

        String externalId = oidcUser.getSubject();

        String email = oidcUser.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String name = oidcUser.getFullName();

        return userRepository.findByExternalId(externalId)
                .map(user -> updateExistingUser(user, name, email))
                .orElseGet(() -> linkOrCreateUser(
                        externalId,
                        name,
                        email
                ));
    }

    private User updateExistingUser(
            User user,
            String name,
            String email
    ) {

        userRepository.findByEmail(email)
                .filter(found -> !found.getId().equals(user.getId()))
                .ifPresent(found -> {
                    throw new ConflictException(
                            "O e-mail informado pelo Google já está vinculado a outro usuário."
                    );
                });

        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }

    private User linkOrCreateUser(
            String externalId,
            String name,
            String email
    ) {

        return userRepository.findByEmail(email)
                .map(user -> linkExistingUser(
                        user,
                        externalId,
                        name
                ))
                .orElseGet(() -> createUser(
                        externalId,
                        name,
                        email
                ));
    }

    private User linkExistingUser(
            User user,
            String externalId,
            String name
    ) {

        if (user.getExternalId() != null
                && !user.getExternalId().isBlank()
                && !user.getExternalId().equals(externalId)) {

            throw new ConflictException(
                    "Este e-mail já está vinculado a outra conta externa."
            );
        }

        user.setExternalId(externalId);
        user.setName(name);

        return userRepository.save(user);
    }

    private User createUser(
            String externalId,
            String name,
            String email
    ) {

        User user = new User();

        user.setExternalId(externalId);
        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }
}