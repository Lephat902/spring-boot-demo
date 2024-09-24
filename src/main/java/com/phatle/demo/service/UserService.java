package com.phatle.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.UserDTO;
import com.phatle.demo.entity.User;
import com.phatle.demo.mapper.EntityDTOMapper;
import com.phatle.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private EntityDTOMapper mapper;

    public List<UserDTO> findAll() {
        return mapper.toDTOs(repository.findAll());
    }

    @Transactional
    public User save(AddUserDTO addUserDTO) {
        if (repository.findOneByUsername(addUserDTO.getUsername()) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists: " + addUserDTO.getUsername());
        }

        User userToSave = mapper.toEntity(addUserDTO);
        return repository.save(userToSave);
    }
}
