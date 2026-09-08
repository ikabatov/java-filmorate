package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    private User.UserBuilder validUser() {
        return User.builder()
                .email("vasya@mail.ru")
                .login("vasya")
                .name("Василий")
                .birthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    void shouldRejectUserWithEmptyEmail() {
        User user = validUser().email("").build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Электронная почта не может быть пустой", e.getMessage());
    }

    @Test
    void shouldRejectUserWithEmailWithoutAtSign() {
        User user = validUser().email("mail.ru").build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Электронная почта должна содержать символ '@'", e.getMessage());
    }

    @Test
    void shouldRejectUserWithEmptyLogin() {
        User user = validUser().login("").build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Логин не может быть пустым", e.getMessage());
    }

    @Test
    void shouldRejectUserWithLoginContainingSpaces() {
        User user = validUser().login("vasya petrov").build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Логин не может содержать пробелы", e.getMessage());
    }

    @Test
    void shouldRejectUserWithBirthdayInFuture() {
        User user = validUser().birthday(LocalDate.now().plusDays(1)).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Дата рождения не может быть в будущем", e.getMessage());
    }

    @Test
    void shouldAcceptUserBornToday() {
        User user = validUser().birthday(LocalDate.now()).build();

        assertDoesNotThrow(() -> userController.createUser(user));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = validUser().name("").build();

        User created = userController.createUser(user);

        assertEquals("vasya", created.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = validUser().name(null).build();

        User created = userController.createUser(user);

        assertEquals("vasya", created.getName());
    }

    @Test
    void shouldRejectUpdateOfUnknownUser() {
        userController.createUser(validUser().build());
        User unknown = validUser().id(999L).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> userController.updateUser(unknown));

        assertEquals("Пользователя с таким ID не существует", e.getMessage());
    }
}
