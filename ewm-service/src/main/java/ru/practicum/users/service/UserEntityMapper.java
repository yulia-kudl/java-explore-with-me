package ru.practicum.users.service;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.entity.UserEntity;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserEntityMapper {
    UserEntity toEntity(UserDto requestDto);

    UserDto toUserDto(UserEntity entity);
}