package com.phatle.demo.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.phatle.demo.entity.UserRole;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
    private final Environment env;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
            LazySecurityContextProviderFilter lazySecurityContextProviderFilter) throws Exception {

        http
                .httpBasic(basic -> basic.disable())
                .sessionManagement(se -> se.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(a -> {
                    a.requestMatchers(HttpMethod.GET, "/users/**").hasAuthority(UserRole.ADMIN.toString());
                    a.requestMatchers(HttpMethod.GET, "/self").authenticated();
                    a.anyRequest().permitAll();
                })
                .oauth2Login(oauth2Login -> oauth2Login
                        .successHandler(customOAuth2AuthenticationSuccessHandler))
                .addFilterAfter(lazySecurityContextProviderFilter, SessionManagementFilter.class)
                // WITHOUT the following block:
                // 1) Any unauthorized requests will be redirected to OAuth2 login page
                // 2) Status code will then be 302 instead of 401
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new CustomAuthenticationEntryPoint(),
                                new AntPathRequestMatcher("/**")));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(env.getProperty("HOST")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
