package ru.t1.authservice.dto.response;

import lombok.Builder;
import lombok.Data;
import ru.t1.authservice.model.TokenType;

@Data
@Builder
public class JwsResponseDto {
    private String accessToken;
    private String refreshToken;
    private TokenType tokenType;
}
