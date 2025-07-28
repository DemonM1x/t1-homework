package ru.t1.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
public class LoginRequestDto {
    @Size(min = 4,
    max = 255)
    @NotBlank
    private String email;
    @Size(min = 6,
    max = 255)
    @NotBlank
    private String password;
}
