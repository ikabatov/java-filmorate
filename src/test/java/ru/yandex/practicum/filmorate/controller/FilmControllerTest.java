package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    private Film.FilmBuilder validFilm() {
        return Film.builder()
                .name("Матрица")
                .description("Описание фильма")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100);
    }

    @Test
    void shouldRejectFilmWithEmptyName() {
        Film film = validFilm().name("").build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));

        assertEquals("Название не может быть пустым", e.getMessage());
    }

    @Test
    void shouldRejectFilmWithDescriptionLongerThan200() {
        Film film = validFilm().description("a".repeat(201)).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));

        assertEquals("Длина описания не может превышать 200 символов", e.getMessage());
    }

    @Test
    void shouldAcceptFilmWithDescriptionOf200() {
        Film film = validFilm().description("a".repeat(200)).build();

        assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    void shouldRejectFilmReleasedBeforeCinemaBirthday() {
        Film film = validFilm().releaseDate(LocalDate.of(1895, 12, 27)).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));

        assertEquals("Дата релиза - не раньше 28 декабря 1895 года", e.getMessage());
    }

    @Test
    void shouldAcceptFilmReleasedOnCinemaBirthday() {
        Film film = validFilm().releaseDate(LocalDate.of(1895, 12, 28)).build();

        assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    void shouldRejectFilmWithZeroDuration() {
        Film film = validFilm().duration(0).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));

        assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
    }

    @Test
    void shouldAcceptFilmWithPositiveDuration() {
        Film film = validFilm().duration(1).build();

        assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    void shouldAssignIdWhenFilmAdded() {
        Film added = filmController.addFilm(validFilm().build());

        assertNotNull(added.getId());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        Film film = validFilm().build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(film));

        assertEquals("ID не может быть пустым", e.getMessage());
    }

    @Test
    void shouldRejectUpdateOfUnknownFilm() {
        filmController.addFilm(validFilm().build());
        Film unknown = validFilm().id(999L).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(unknown));

        assertEquals("Фильм с таким ID не найден", e.getMessage());
    }
}
