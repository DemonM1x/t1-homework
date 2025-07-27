package ru.t1.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.t1.authservice.model.AccessTokenEntity;
import ru.t1.authservice.model.RefreshTokenEntity;
import ru.t1.authservice.repository.AccessTokenRepository;
import ru.t1.authservice.repository.RefreshTokenRepository;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final PrivateKey privateKey = JwtUtils.getPrivateKey("private.pem");
    private final PublicKey publicKey = JwtUtils.getPublicKey("public.pem");
    @Value("${security.jwt.access_toke_expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${security.jwt.refresh_toke_expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private AccessTokenRepository accessTokenRepository;
    private RefreshTokenRepository refreshTokenRepository;

    public JwtService() throws Exception {
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .claim("type", "access")
                .signWith(privateKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .claim("type", "refresh")
                .signWith(privateKey)
                .compact();
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

    private boolean isRefreshTokenWithdrown(String refreshToken, String userName) {
        RefreshTokenEntity token =  this.refreshTokenRepository.findById(userName).orElse(null);
        return token == null || !token.getRefreshToken().equals(refreshToken);
    }

    private boolean isAccessTokenWithdrown(String accessToken, String userName) {
        AccessTokenEntity token =  this.accessTokenRepository.findById(userName).orElse(null);
        return token == null || !token.getAccessToken().equals(accessToken);
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
