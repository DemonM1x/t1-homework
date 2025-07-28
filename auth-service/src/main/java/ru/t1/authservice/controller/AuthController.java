package ru.t1.authservice.controller;

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
import ru.t1.authservice.service.AuthService;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping( "/register")
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
    public ResponseEntity<JwsResponseDto> login(@RequestBody
                                            @Valid
                                            LoginRequestDto dto) {
        JwsResponseDto response = authService.login(dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/admin/give/premium")
    public ResponseEntity<Void> givePremium(@RequestBody
                                            @Valid
                                            EmailRequestDto dto)  {
        authService.givePremium(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/premium")
    public ResponseEntity<String> checkPremium(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(authentication.getName() + " you have premium");
    }
}
