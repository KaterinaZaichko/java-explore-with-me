package ru.practicum.service;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(long[] ids, int from, int size);

    List<UserDto> getAll(int from, int size);

    UserDto save(NewUserRequest newUserRequest);

    void delete(long userId);
}
