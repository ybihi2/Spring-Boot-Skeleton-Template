package com.jydoc.deliverable4.Service;

import com.jydoc.deliverable4.DTO.UserDTO;
import com.jydoc.deliverable4.security.Exceptions.EmailExistsException;
import com.jydoc.deliverable4.security.Exceptions.UsernameExistsException;
import com.jydoc.deliverable4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserValidationHelper {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void validateUserRegistration(UserDTO userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }

        String username = userDto.getUsername().trim();
        if (existsByUsername(username)) {
            throw new UsernameExistsException(username);
        }

        String email = userDto.getEmail().trim().toLowerCase();
        if (existsByEmail(email)) {
            throw new EmailExistsException(email);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}