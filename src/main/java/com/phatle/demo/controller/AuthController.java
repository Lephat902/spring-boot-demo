package com.phatle.demo.controller;

import java.util.Arrays;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.LoginDTO;
import com.phatle.demo.entity.User;
import com.phatle.demo.security.JwtTokenVo;
import com.phatle.demo.security.SecurityUtils;
import com.phatle.demo.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService service;

    @PostMapping("/login")
    public void login(@Valid @RequestBody LoginDTO loginDTO) {
        User user = service.login(loginDTO);

        JwtTokenVo jwtTokenVo = JwtTokenVo.builder()
                .id(user.getId())
                .roles(Arrays.asList(user.getUserRole()))
                .build();

        SecurityUtils.setJwtToClient(jwtTokenVo);
    }

    @PostMapping("/signup")
    public User save(@Valid @RequestBody AddUserDTO addUserDTO) {
        return service.save(addUserDTO);
    }
}
