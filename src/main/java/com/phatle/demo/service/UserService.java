package com.phatle.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.LoginDTO;
import com.phatle.demo.dto.UserDTO;
import com.phatle.demo.entity.User;
import com.phatle.demo.entity.UserRole;
import com.phatle.demo.mapper.EntityDTOMapper;
import com.phatle.demo.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final EntityDTOMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public List<UserDTO> findAll() {
        return mapper.toDTOs(repository.findAll());
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

    public User login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            var userID = (UUID) authentication.getPrincipal();
            // Currently, user has only one role
            var userRole = (SimpleGrantedAuthority) authentication.getAuthorities().toArray()[0];

            User user = new User();
            user.setId(userID);
            user.setUserRole(UserRole.valueOf(userRole.toString()));

            return user;
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }
}
