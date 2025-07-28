package ru.t1.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JweService jweService;

    public JwtService(AccessTokenRepository accessTokenRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      JweService jweService) throws Exception {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jweService = jweService;
    }

    public String generateAccessToken(UserEntity user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String jwtToken = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .claim("type", "access")
                .claim("roles", roles)
                .claim("sessionId", generateSessionId())
                .signWith(privateKey)
                .compact();

        String encryptedToken = jweService.encryptToken(jwtToken);

        AccessTokenEntity accessTokenEntity = AccessTokenEntity.builder()
                .id(user.getUsername())
                .accessToken(new ArrayList<>())
                .build();
        accessTokenEntity.getAccessToken().add(encryptedToken);
        accessTokenRepository.save(accessTokenEntity);

        return encryptedToken;
    }

    public String generateRefreshToken(UserEntity user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String jwtToken = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .claim("type", "refresh")
                .claim("roles", roles)
                .claim("sessionId", generateSessionId())
                .signWith(privateKey)
                .compact();


        String encryptedToken = jweService.encryptToken(jwtToken);

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(user.getUsername())
                .orElse(RefreshTokenEntity.builder()
                        .id(user.getUsername())
                        .refreshToken(encryptedToken)
                        .build());
        refreshTokenEntity.setRefreshToken(encryptedToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return encryptedToken;
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractClaims(jwtToken).getExpiration().before(new Date());
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    public boolean isTokenValid(String encryptedToken, String expectedUsername) {
        try {
            String jwtToken = jweService.decryptToken(encryptedToken);
            Claims claims = extractClaims(jwtToken);
            String username = claims.getSubject();
            return username.equals(expectedUsername) && !isTokenExpired(jwtToken) && !isAccessTokenWithdrown(encryptedToken, username);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String encryptedToken) {
        String jwtToken = jweService.decryptToken(encryptedToken);
        return extractClaims(jwtToken).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String encryptedToken) {
        String jwtToken = jweService.decryptToken(encryptedToken);
        Claims claims = extractClaims(jwtToken);
        return claims.get("roles", List.class);
    }

    private boolean isAccessTokenWithdrown(String accessToken, String userName) {
        AccessTokenEntity tokenEntity = this.accessTokenRepository.findById(userName).orElse(null);
        return tokenEntity == null || !tokenEntity.getAccessToken().contains(accessToken);
    }

    private Claims extractClaims(String jwtToken) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public void deleteAllTokens(String username) {
        accessTokenRepository.deleteById(username);
        refreshTokenRepository.deleteById(username);
    }
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

}
