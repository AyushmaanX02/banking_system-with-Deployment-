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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("9876543210");

        savedUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("register: success → returns token, email, role, fullName")
    void register_success() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(BankUserDetails.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getFullName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
        verify(auditLogService).log(eq("test@example.com"), eq("REGISTER"), any(), any(), any());
    }

    @Test
    @DisplayName("register: duplicate email → throws UserAlreadyExistsException")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: valid credentials → returns JWT token")
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken(any(BankUserDetails.class))).thenReturn("login-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("login-jwt-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(auditLogService).log(eq("test@example.com"), eq("LOGIN"), any(), any(), any());
    }

    @Test
    @DisplayName("login: wrong credentials → throws BadCredentialsException")
    void login_wrongPassword_throws() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
