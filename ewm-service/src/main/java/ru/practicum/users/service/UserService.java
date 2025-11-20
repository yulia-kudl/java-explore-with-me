package ru.practicum.users.service;

import org.springframework.stereotype.Service;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

@Service
public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto addUser(NewUserRequest request);

    void deleteUser(Long userId);
}
