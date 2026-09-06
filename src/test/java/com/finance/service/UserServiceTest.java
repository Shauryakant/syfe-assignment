package com.finance.service;

import com.finance.dto.RegisterRequest;
import com.finance.dto.RegisterResponse;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.DuplicateResourceException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("user@example.com", "password123", "John Doe", "+1234567890");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User(registerRequest.getUsername(), "encodedPassword", registerRequest.getFullName(), registerRequest.getPhoneNumber());
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = userService.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_NullOrBlankUsername() {
        RegisterRequest req = new RegisterRequest("", "password", "Name", "123");
        assertThrows(BadRequestException.class, () -> userService.registerUser(req));
    }

    @Test
    void testRegisterUser_NullOrBlankPassword() {
        RegisterRequest req = new RegisterRequest("user@example.com", "", "Name", "123");
        assertThrows(BadRequestException.class, () -> userService.registerUser(req));
    }

    @Test
    void testGetUserByUsername_Success() {
        User user = new User("user@example.com", "pass", "John", "123");
        user.setId(1L);
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("user@example.com");

        assertNotNull(result);
        assertEquals("user@example.com", result.getUsername());
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("unknown@example.com"));
    }
}
