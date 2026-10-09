package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.user.Role;
import com.banditdev.actioncenter.model.user.User;
import com.banditdev.actioncenter.model.user.dto.UserRequest;
import com.banditdev.actioncenter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Test
    void createUser_duplicateUsername_rejectedAndNotSaved() {
        when(userRepository.existsByUsername("anna")).thenReturn(true);

        var ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(new UserRequest("anna", "secret", Role.EMPLOYEE)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_correctCredentials_returnsUser() {
        User anna = new User("anna", "secret", Role.EMPLOYEE);
        when(userRepository.findByUsername("anna")).thenReturn(anna);

        assertSame(anna, userService.login("anna", "secret"));
    }

    @Test
    void login_wrongPassword_rejected() {
        when(userRepository.findByUsername("anna")).thenReturn(new User("anna", "secret", Role.EMPLOYEE));

        assertThrows(IllegalArgumentException.class, () -> userService.login("anna", "wrong"));
    }
}
