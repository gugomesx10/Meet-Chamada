package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Transactional
    public User create(
            String name,
            String email,
            String externalId
    ) {

        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(
                    "O nome do usuário é obrigatório."
            );
        }

        if (email == null || email.isBlank()) {
            throw new BusinessRuleException(
                    "O e-mail do usuário é obrigatório."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException(
                    "Já existe um usuário com este e-mail."
            );
        }

        User user = new User();

        user.setName(name.trim());
        user.setEmail(normalizedEmail);

        if (externalId != null
                && !externalId.isBlank()) {

            user.setExternalId(
                    externalId.trim()
            );
        }

        return userRepository.save(user);
    }
    @Transactional(readOnly = true)
    public User findById(UUID userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }
    @Transactional(readOnly = true)
    public User findByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new BusinessRuleException(
                    "O e-mail é obrigatório."
            );
        }

        return userRepository
                .findByEmail(
                        email.trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }
}