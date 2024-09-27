package com.phatle.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.LoginDTO;
import com.phatle.demo.dto.UserDTO;
import com.phatle.demo.entity.User;
import com.phatle.demo.mapper.EntityDTOMapper;
import com.phatle.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private EntityDTOMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_UserExists() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        UserDTO userDTO = new UserDTO();

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.findById(userId);

        assertNotNull(result);
        assertEquals(userDTO, result);
    }

    @Test
    void findById_UserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.findById(userId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found with id: " + userId, exception.getReason());
    }

    @Test
    @Transactional
    void save_UsernameExists() {
        AddUserDTO addUserDTO = AddUserDTO.builder()
                .username("existingUser")
                .build();

        when(repository.findOneByUsername(addUserDTO.getUsername())).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.save(addUserDTO);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username already exists: " + addUserDTO.getUsername(), exception.getReason());
    }

    @Test
    @Transactional
    void save_UsernameDoesNotExist() {
        AddUserDTO addUserDTO = AddUserDTO.builder()
                .username("newUser")
                .password("password")
                .build();

        User user = new User();
        user.setUsername(addUserDTO.getUsername());
        user.setPassword(addUserDTO.getPassword());

        when(repository.findOneByUsername(addUserDTO.getUsername())).thenReturn(Optional.empty());
        when(mapper.toEntity(addUserDTO)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(repository.save(user)).thenReturn(user);

        User result = userService.save(addUserDTO);

        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void login_CorrectPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("user");
        loginDTO.setPassword("correctPassword");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        userService.login(loginDTO);

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void login_WrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("user");
        loginDTO.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {
                });

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.login(loginDTO);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }
}
