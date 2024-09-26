package com.phatle.demo.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.phatle.demo.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenVo {
    private UUID id;
    private List<UserRole> roles;

    @JsonIgnore
    public List<GrantedAuthority> getAuthorities() {
        if (roles == null)
            return new ArrayList<>();
        return roles.stream().map(s -> (GrantedAuthority) () -> s.toString()).toList();
    }
}
