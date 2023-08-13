package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll(long[] ids, int from, int size) {
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        return userRepository.findAllByIdIn(ids, pageWithSomeElements).stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAll(int from, int size) {
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        return userRepository.findAll(pageWithSomeElements).stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto save(NewUserRequest newUserRequest) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(newUserRequest)));
    }

    @Override
    public void delete(long userId) {
        userRepository.delete(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId))));
    }
}
