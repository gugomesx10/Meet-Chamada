package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
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
    @Transactional(readOnly = true)
    public User findById(
            UUID userId
    ) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }
    @Transactional(readOnly = true)
    public User findByEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

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