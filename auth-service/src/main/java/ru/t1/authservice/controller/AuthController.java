package ru.t1.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.t1.authservice.dto.request.EmailRequestDto;
import ru.t1.authservice.dto.request.LoginRequestDto;
import ru.t1.authservice.dto.request.RegistryRequestDto;
import ru.t1.authservice.dto.response.JwsResponseDto;
import ru.t1.authservice.security.JwtService;
import ru.t1.authservice.security.TokenSecurityService;
import ru.t1.authservice.service.AuthService;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenSecurityService tokenSecurityService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody
                                         @Valid
                                         RegistryRequestDto dto) {
        try {
            System.out.println("try to register " + dto);
            authService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwsResponseDto> login(@RequestBody @Valid LoginRequestDto dto,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        String clientIp = jwtService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        JwsResponseDto authResponse = authService.login(dto, clientIp, userAgent);
        response.setHeader("X-Access-Token-Fingerprint",
                tokenSecurityService.createTokenFingerprint(authResponse.getAccessToken(), userAgent, clientIp));
        response.setHeader("X-Refresh-Token-Fingerprint",
                tokenSecurityService.createTokenFingerprint(authResponse.getRefreshToken(), userAgent, clientIp));

        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication,
                                       HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String refreshTokenHeader = request.getHeader("X-Refresh-Token");
            String tokenFingerprint = request.getHeader("X-Token-Fingerprint");
            String clientIp = jwtService.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            String accessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }

            if (accessToken != null && tokenFingerprint != null) {
                boolean validFingerprint = tokenSecurityService.verifyTokenFingerprint(
                        accessToken, userAgent, clientIp, tokenFingerprint);

                if (!validFingerprint) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .build();
                }
            }

            authService.logout(authentication.getName(), accessToken, refreshTokenHeader);

            return ResponseEntity.status(HttpStatus.OK).build();

        } catch (Exception e) {
            System.err.println("Ошибка при logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/give/premium")
    public ResponseEntity<Void> givePremium(@RequestBody
                                            @Valid
                                            EmailRequestDto dto) {
        authService.givePremium(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/premium")
    public ResponseEntity<String> checkPremium(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(authentication.getName() + " you have premium");
    }

    @PostMapping("/admin/revoke-tokens")
    public ResponseEntity<Void> revokeUserTokens(@RequestBody @Valid EmailRequestDto dto,
                                                 @RequestParam String reason) {
        authService.revokeAllUserTokens(dto.getEmail(), reason);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
