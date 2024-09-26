package com.phatle.demo.dto;

import java.util.Date;
import java.util.UUID;

import com.phatle.demo.entity.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private UserRole userRole;
    private String picture;
    private String name;
    private String city;
    private Date birthDate;
    private boolean isPasswordSet;
}
