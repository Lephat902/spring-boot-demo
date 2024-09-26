package com.phatle.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.phatle.demo.entity.User;
import com.phatle.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (DefaultOidcUser) auth.getPrincipal();
        var email = principal.getEmail();

        var user = userRepository.findOneByEmail(email)
                .orElseGet(() -> {
                    // Create new user
                    var newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(principal.getFullName());
                    newUser.setPicture(principal.getPicture());

                    return userRepository.save(newUser);
                });

        var jwtToken = SecurityUtils.buildJwtTokenFromUser(user);
        SecurityUtils.setJwtToClient(jwtToken);
    }
}
