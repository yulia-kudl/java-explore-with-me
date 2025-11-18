package ru.practicum.users.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ErrorHandler.EntityNotFoundException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.entity.UserEntity;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserEntityMapper mapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllById(ids)
                    .stream()
                    .map(mapper::toUserDto)
                    .collect(Collectors.toList());
        }
        Pageable pageable = PageRequest.of(from / size, size);
        return userRepository.findAll(pageable)
                .stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto addUser(NewUserRequest request) {
        UserEntity userEntity = userRepository.save(mapper.toEntity(request));
        return mapper.toUserDto(userEntity);
    }

    @Override
    public void deleteUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(userId, "User");
        }
        userRepository.deleteById(userId);
    }
}
