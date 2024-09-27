package com.phatle.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.phatle.demo.entity.User;
import com.phatle.demo.entity.UserRole;
import com.phatle.demo.repository.UserRepository;

public class CustomAuthenticationManagerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomAuthenticationManager customAuthenticationManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAuthenticateWithEmail() {
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setUserRole(UserRole.USER);

        when(userRepository.findOneByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        Authentication result = customAuthenticationManager.authenticate(authentication);

        assertNotNull(result);
        assertEquals(user.getUserRole().toString(), result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    public void testAuthenticateWithUsername() {
        String username = "testuser";
        String password = "password";
        User user = new User();
        user.setUsername(username);
        user.setPassword("hashedPassword");
        user.setUserRole(UserRole.ADMIN);

        when(userRepository.findOneByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        Authentication result = customAuthenticationManager.authenticate(authentication);

        assertNotNull(result);
        assertEquals(user.getUserRole().toString(), result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    public void testAuthenticateWithInvalidCredentials() {
        String username = "testuser";
        String password = "wrongpassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword("hashedPassword");

        when(userRepository.findOneByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        assertThrows(BadCredentialsException.class, () -> {
            customAuthenticationManager.authenticate(authentication);
        });
    }

    @Test
    public void testAuthenticateWithNonExistentUser() {
        String username = "nonexistentuser";
        String password = "password";

        when(userRepository.findOneByUsername(username)).thenReturn(Optional.empty());

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        assertThrows(BadCredentialsException.class, () -> {
            customAuthenticationManager.authenticate(authentication);
        });
    }
}