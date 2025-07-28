package ru.t1.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import ru.t1.authservice.model.AccessTokenEntity;
import ru.t1.authservice.model.RefreshTokenEntity;
import ru.t1.authservice.model.UserEntity;
import ru.t1.authservice.repository.AccessTokenRepository;
import ru.t1.authservice.repository.RefreshTokenRepository;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final PrivateKey privateKey = JwtUtils.getPrivateKey("private.pem");
    private final PublicKey publicKey = JwtUtils.getPublicKey("public.pem");
    @Value("${security.jwt.access_toke_expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${security.jwt.refresh_toke_expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(AccessTokenRepository accessTokenRepository, RefreshTokenRepository refreshTokenRepository) throws Exception {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(UserEntity user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .claim("type", "access")
                .claim("roles", roles)
                .signWith(privateKey)
                .compact();

        AccessTokenEntity accessTokenEntity = AccessTokenEntity.builder()
                .id(user.getUsername())
                .accessToken(new ArrayList<>())
                .build();
        accessTokenEntity.getAccessToken().add(accessToken);
        accessTokenRepository.save(accessTokenEntity);
        return accessToken;
    }

    public String generateRefreshToken(UserEntity user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String refreshToken = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .claim("type", "refresh")
                .claim("roles", roles)
                .signWith(privateKey)
                .compact();

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(user.getUsername())
                .orElse(RefreshTokenEntity.builder()
                        .id(user.getUsername())
                        .refreshToken(refreshToken)
                        .build());
        refreshTokenRepository.save(refreshTokenEntity);
        return refreshToken;
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        Claims claims = extractClaims(token);
        String username = claims.getSubject();
        return username.equals(expectedUsername) && !isTokenExpired(token) && !isAccessTokenWithdrown(token, username);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        return claims.get("roles", List.class);
    }

    private boolean isAccessTokenWithdrown(String accessToken, String userName) {
        AccessTokenEntity tokenEntity = this.accessTokenRepository.findById(userName).orElse(null);
        return tokenEntity == null || !tokenEntity.getAccessToken().contains(accessToken);
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public void deleteAllTokens(String username) {
        accessTokenRepository.deleteById(username);
        refreshTokenRepository.deleteById(username);
    }
}
