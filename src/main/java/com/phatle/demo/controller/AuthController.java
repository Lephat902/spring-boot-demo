package com.phatle.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.LoginDTO;
import com.phatle.demo.dto.UserDTO;
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

    @PostMapping("/signin")
    public void login(@Valid @RequestBody LoginDTO loginDTO) {
        service.login(loginDTO);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var jwtToken = (JwtTokenVo) auth.getPrincipal();

        SecurityUtils.setJwtToClient(jwtToken);
    }

    @PostMapping("/signup")
    public User save(@Valid @RequestBody AddUserDTO addUserDTO) {
        return service.save(addUserDTO);
    }

    @GetMapping("/self")
    public UserDTO findMe(@AuthenticationPrincipal JwtTokenVo currentUser) {
        return service.findById(currentUser.getId());
    }
}
