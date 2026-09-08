package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.debug("Запрос на добавление фильма: {}", film);
        validateFilm(film);
        film.setId(getNextId());

        films.put(film.getId(), film);
        log.info("Добавили фильм id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Запрос на обновление фильма: {}", film);
        if (film.getId() == null) {
            log.warn("Отклонено: пустое ID");
            throw new ValidationException("ID не может быть пустым");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Отклонено: фильм c id={} не найден", film.getId());
            throw new ValidationException("Фильм с таким ID не найден");
        }
        validateFilm(film);

        films.replace(film.getId(), film);
        log.info("Фильм обновлен id={}, name={}", film.getId(), film.getName());
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Отклонено: название пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Отклонено: длина описания: {}, превышает 200 символов", film.getDescription().length());
            throw new ValidationException("Длина описания не может превышать 200 символов");
        }
        if (film.getReleaseDate() != null && CINEMA_BIRTHDAY.isAfter(film.getReleaseDate())) {
            log.warn("Отклонено: дата релиза: releaseDate={}, раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            log.warn("Отклонено: продолжительность фильма: duration={}, не положительное число", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
