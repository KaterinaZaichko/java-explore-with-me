package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NameUniquenessViolationException;
import ru.practicum.exception.UserNotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll(long[] ids, int from, int size) {
        List<UserDto> users = new ArrayList<>();
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        for (User user : userRepository.findAllByIdIn(ids, pageWithSomeElements)) {
            users.add(UserMapper.toUserDto(user));
        }
        return users;
    }

    @Override
    public List<UserDto> getAll(int from, int size) {
        List<UserDto> users = new ArrayList<>();
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        for (User user : userRepository.findAll(pageWithSomeElements)) {
            users.add(UserMapper.toUserDto(user));
        }
        return users;
    }

    @Override
    public UserDto save(NewUserRequest newUserRequest) {
        if (userRepository.findByName(newUserRequest.getName()) != null) {
            throw new NameUniquenessViolationException(
                    String.format("Name %s already exists", newUserRequest.getName()));
        }
        User user = UserMapper.toUser(newUserRequest);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(long userId) {
        userRepository.delete(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId))));
    }
}
