package com.example.bankcards.security;

import com.example.bankcards.dto.SignupResponseDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.LoginRequestDto;
import com.example.bankcards.entity.dto.LoginResponseDto;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.bankcards.entity.enums.Role.USER;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        var manager = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.username(), loginRequestDto.password())
        );
        var user = (UserEntity) manager.getPrincipal();
        String token = authUtil.generateAccessToken(user);

        return LoginResponseDto.builder()
                .jwt(token)
                .userId(user.getId())
                .build();
    }

    public SignupResponseDto signup(LoginRequestDto loginRequestDto) {
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
