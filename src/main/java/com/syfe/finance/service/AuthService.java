package com.syfe.finance.service;

import com.syfe.finance.dto.AuthDtos;
import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.User;
import com.syfe.finance.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthDtos.RegisterResponse register(AuthDtos.RegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User(
                null,
                username,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.phoneNumber().trim()
        );
        userRepository.save(user);
        return new AuthDtos.RegisterResponse("User registered successfully", user.getId());
    }
}
