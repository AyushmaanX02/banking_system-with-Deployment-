package com.banking.service;

import com.banking.dto.request.LoginRequest;
import com.banking.dto.request.RegisterRequest;
import com.banking.dto.response.AuthResponse;
import com.banking.entity.User;
import com.banking.enums.Role;
import com.banking.exception.UserAlreadyExistsException;
import com.banking.repository.UserRepository;
import com.banking.security.JwtUtil;
import com.banking.security.UserDetailsServiceImpl.BankUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Audit Log
        auditLogService.log(
                savedUser.getEmail(),
                "REGISTER",
                "User",
                savedUser.getId().toString(),
                "User registered successfully as CUSTOMER"
        );

        // Generate JWT token
        BankUserDetails userDetails = new BankUserDetails(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .fullName(savedUser.getFullName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        // Audit Log
        auditLogService.log(
                user.getEmail(),
                "LOGIN",
                "User",
                user.getId().toString(),
                "User logged in successfully"
        );

        // Generate JWT token
        BankUserDetails userDetails = new BankUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }
}
