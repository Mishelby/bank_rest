package com.example.bankcards.security;

import com.example.bankcards.dto.SignupResponseDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.LoginRequestDto;
import com.example.bankcards.entity.dto.LoginResponseDto;
import com.example.bankcards.exception.AuthException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.bankcards.entity.enums.Role.USER;

/**
 * Сервис для аутентификации и регистрации пользователей.
 *
 * <p>Содержит логику входа (login) и создания новых аккаунтов (signup).
 * Использует {@link AuthenticationManager} для проверки учетных данных,
 * {@link UserRepository} для доступа к данным пользователей,
 * {@link AuthUtil} для генерации JWT-токенов,
 * и {@link PasswordEncoder} для безопасного хеширования паролей.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Выполняет аутентификацию пользователя и возвращает JWT-токен.
     *
     * <p>Процесс включает:
     * <ul>
     *     <li>Проверку учетных данных через {@link AuthenticationManager}</li>
     *     <li>Проверку, что аккаунт активен (enabled)</li>
     *     <li>Генерацию access-токена через {@link AuthUtil}</li>
     * </ul>
     * </p>
     *
     * @param loginRequestDto DTO с именем пользователя и паролем
     * @return {@link LoginResponseDto} с JWT-токеном и идентификатором пользователя
     * @throws AuthException если учетные данные неверные или аккаунт недоступен
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) throws AuthException {
        var manager = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.username(), loginRequestDto.password())
        );
        var user = (UserEntity) manager.getPrincipal();

        if (!user.isEnabled()) {
            throw new AuthException("Аккаунт недоступен!", String.valueOf(HttpStatus.UNAUTHORIZED));
        }

        String token = authUtil.generateAccessToken(user);

        return LoginResponseDto.builder()
                .jwt(token)
                .userId(user.getId())
                .build();
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * <p>Процесс включает:
     * <ul>
     *     <li>Проверку уникальности имени пользователя</li>
     *     <li>Хеширование пароля через {@link PasswordEncoder}</li>
     *     <li>Сохранение нового пользователя в БД с ролью {@code USER}</li>
     * </ul>
     * </p>
     *
     * @param loginRequestDto DTO с именем пользователя и паролем
     * @return {@link SignupResponseDto} с информацией о созданном пользователе
     * @throws IllegalArgumentException если имя пользователя уже занято
     */
    public SignupResponseDto signup(LoginRequestDto loginRequestDto) throws IllegalArgumentException {
        if (userRepository.existsByUsername(loginRequestDto.username())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        var encode = passwordEncoder.encode(loginRequestDto.password());

        UserEntity saved = userRepository.save(
                UserEntity.builder()
                        .username(loginRequestDto.username())
                        .password(encode)
                        .role(USER)
                        .enabled(true)
                        .build()
        );

        return SignupResponseDto.builder()
                .userId(saved.getId())
                .username(saved.getUsername())
                .build();
    }
}
