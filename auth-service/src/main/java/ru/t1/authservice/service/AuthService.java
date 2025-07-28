package ru.t1.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.authservice.dto.request.EmailRequestDto;
import ru.t1.authservice.dto.request.LoginRequestDto;
import ru.t1.authservice.dto.request.RegistryRequestDto;
import ru.t1.authservice.dto.response.JwsResponseDto;
import ru.t1.authservice.model.Role;
import ru.t1.authservice.model.RoleEntity;
import ru.t1.authservice.model.TokenType;
import ru.t1.authservice.model.UserEntity;
import ru.t1.authservice.repository.AccessTokenRepository;
import ru.t1.authservice.repository.RefreshTokenRepository;
import ru.t1.authservice.repository.UserRepository;
import ru.t1.authservice.security.JwtService;
import ru.t1.authservice.validation.AuthValidator;
import ru.t1.authservice.validation.EmailValidator;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void register(RegistryRequestDto dto) {
        if (!AuthValidator.authValidation(dto.getUsername(), dto.getPassword())){
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!EmailValidator.isValid(dto.getEmail())){
            throw new IllegalArgumentException("Invalid email");
        }
        checkUserEmail(dto.getEmail());
        checkUsername(dto.getUsername());
        RoleEntity userRole;
        if (dto.getEmail().contains("admin")){
            userRole = RoleEntity.builder()
                    .role(Role.ADMIN)
                    .build();
        }
        else {
            userRole = RoleEntity.builder()
                    .role(Role.GUEST)
                    .build();
        }
        List<RoleEntity> roles = new ArrayList<>();
        roles.add(userRole);
        UserEntity user =  buildUserEntity(dto);
        user.setRoles(roles);
        userRepository.save(user);
        log.debug("Created user {}", user);

    }

    public JwsResponseDto login(LoginRequestDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );
        UserEntity user = (UserEntity) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return JwsResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TokenType.BEARER)
                .build();
    }

    public void logout(String username) {
        jwtService.deleteAllTokens(username);
    }

    public void givePremium(EmailRequestDto request){
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));
        RoleEntity role = RoleEntity.builder()
                .role(Role.PREMIUM_USER)
                .build();
        if (!user.getRoles().contains(role)){
            user.getRoles().add(role);
            userRepository.save(user);
        }

    }

    private void checkUserEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private void checkUsername(String username) {
        if (userRepository.existsByUsernameIgnoreCase(username)){
            throw new IllegalArgumentException("Username already exists");
        }
    }

    private UserEntity buildUserEntity(RegistryRequestDto request) {
        return UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
    }

}
