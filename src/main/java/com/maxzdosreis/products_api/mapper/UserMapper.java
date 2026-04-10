package com.maxzdosreis.products_api.mapper;

import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Os demais campos (id, email, enabled) têm o mesmo nome dos dois lados
    @Mapping(source = "username", target = "userName")
    @Mapping(source = "fullName", target = "fullName")
    UserResponseDTO toDto(User user);
}
