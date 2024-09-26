package com.phatle.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.LoginDTO;
import com.phatle.demo.dto.UserDTO;
import com.phatle.demo.entity.User;
import com.phatle.demo.mapper.EntityDTOMapper;
import com.phatle.demo.repository.UserRepository;
import com.phatle.demo.security.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final EntityDTOMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Environment environment;

    public List<UserDTO> findAll() {
        return mapper.toDTOs(repository.findAll());
    }

    public UserDTO findById(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id));
        return mapper.toDTO(user);
    }

    @Transactional
    public User save(AddUserDTO addUserDTO) {
        repository.findOneByUsername(addUserDTO.getUsername()).ifPresent(existingUser -> {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists: " + addUserDTO.getUsername());
        });

        User userToSave = mapper.toEntity(addUserDTO);
        userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));
        return repository.save(userToSave);
    }

    public User saveOAuthUser(User user) {
        return repository.save(user);
    }

    public void login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    public String processGrantCode(String authCode) {
        // Exchange the authorization code for an access token
        var accessTokenResponse = getOauthAccessTokenGoogle(authCode);

        String accessToken = accessTokenResponse.getAccess_token();
        var userProfile = getProfileDetailsGoogle(accessToken);

        var email = userProfile.getEmail();
        var existUser = repository.findOneByEmail(email).get();
        if (existUser == null) {
            var user = new User();
            user.setEmail(email);
            existUser = saveOAuthUser(user);
        }

        var jwtToken = SecurityUtils.buildJwtTokenFromUser(existUser);
        SecurityUtils.setJwtToClient(jwtToken);

        // Return a success message or the profile details as needed
        return "OK, you win";
    }

    private GoogleOAuth2TokenResponse getOauthAccessTokenGoogle(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", environment.getProperty("oauth.google.redirect.uri"));
        params.add("client_id", environment.getProperty("oauth.google.client-id"));
        params.add("client_secret", environment.getProperty("oauth.google.client-secret"));
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile");
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
        params.add("scope", "openid");
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

        String url = "https://oauth2.googleapis.com/token";
        String response = restTemplate.postForObject(url, requestEntity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, GoogleOAuth2TokenResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Some shit went wrong");
        }
    }

    private GoogleOAuth2UserInfo getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), GoogleOAuth2UserInfo.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Some shit went wrong");
        }
    }
}
