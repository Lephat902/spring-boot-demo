package com.phatle.demo.security;

import java.util.Arrays;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.phatle.demo.entity.User;
import com.phatle.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationManager implements AuthenticationManager {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var principal = (String) authentication.getPrincipal();
        var password = (String) authentication.getCredentials();

        User user;
        if (principal.contains("@")) {
            user = userRepository.findOneByEmail(principal).orElseThrow(
                    () -> new BadCredentialsException("1000"));
        } else {
            user = userRepository.findOneByUsername(principal).orElseThrow(
                    () -> new BadCredentialsException("1000"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("1000");
        }

        var userRole = user.getUserRole();
        var jwtToken = SecurityUtils.buildJwtTokenFromUser(user);

        return new UsernamePasswordAuthenticationToken(
                jwtToken,
                null,
                Arrays.asList(new SimpleGrantedAuthority(userRole.toString())));
    }
}