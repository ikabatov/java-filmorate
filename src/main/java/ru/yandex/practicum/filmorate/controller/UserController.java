package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Запрос на создание пользователя: {}", user);
        validateUser(user);
        updateUserName(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создали пользователя id={}, name={}", user.getId(), user.getName());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Запрос на обновление пользователя: {}", user);
        if (user.getId() == null) {
            log.warn("Отклонено: id пустое");
            throw new ValidationException("ID не может быть пустым");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("Отклонено: пользователя с id={}, не существует", user.getId());
            throw new ValidationException("Пользователя с таким ID не существует");
        }
        validateUser(user);
        updateUserName(user);
        users.replace(user.getId(), user);
        log.info("Обновили пользователя id={}, name={}", user.getId(), user.getName());
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Отклонено: электронная почта пустая");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Отклонено: электронная почта: email={}, должна содержать символ @", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Отклонено: логин пустой");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Отклонено: login={} не может содержать пробелы", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Отклонено: дата рождения: birthday={}, не может быть в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void updateUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
