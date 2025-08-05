package com.example.producer.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.common_lib.model.exception.DuplicateEmailException;
import com.example.producer.model.User;
import com.example.producer.repo.UserRepository;
import com.example.producer.service.nosql.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser_Success() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser(user);

        // Assert
        assertNotNull(createdUser.getCreatedAt(), "La date de création doit être définie");
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testuser", createdUser.getUsername());

        // Vérifie que save() a été appelé exactement une fois
        verify(userRepository, times(1)).save(createdUser);
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        // Arrange
        User user = new User();
        user.setEmail("existing@example.com");
        user.setUsername("existinguser");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class, () -> {
            userService.createUser(user);
        });

        assertEquals("L'email existe déjà : existing@example.com", ex.getMessage());

        // Vérifie que save() n'a jamais été appelé
        verify(userRepository, never()).save(any());
    }

}

