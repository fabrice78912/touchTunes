package com.example.producer.mapper;

import com.example.producer.model.User;
import com.example.producer.model.dto.UserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequest dto);
    UserRequest toDto(User entity);
}
