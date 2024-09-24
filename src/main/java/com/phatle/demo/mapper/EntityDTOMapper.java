package com.phatle.demo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.phatle.demo.dto.AddUserDTO;
import com.phatle.demo.dto.UserDTO;
import com.phatle.demo.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntityDTOMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(AddUserDTO dto);

    UserDTO toDTO(User user);
    List<UserDTO> toDTOs(List<User> users);
}