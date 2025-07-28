package ru.t1.authservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenSecurityService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${security.token.hmac.secret}")
    private String hmacSecret;

    @Value("${security.token.fingerprint.enabled:true}")
    private boolean fingerprintEnabled;

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String SESSION_PREFIX = "session:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public String createTokenFingerprint(String token, String userAgent, String clientIp) {
        if (!fingerprintEnabled) {
            return null;
        }

        try {
            String fingerprintData = token + userAgent + clientIp + hmacSecret;
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(fingerprintData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Ошибка создания отпечатка токена", e);
            throw new RuntimeException("Ошибка создания отпечатка токена", e);
        }
    }

    public boolean verifyTokenFingerprint(String token, String userAgent, String clientIp, String expectedFingerprint) {
        if (!fingerprintEnabled || expectedFingerprint == null) {
            return true;
        }

        String actualFingerprint = createTokenFingerprint(token, userAgent, clientIp);
        return actualFingerprint.equals(expectedFingerprint);
    }

    public void blacklistToken(String tokenHash, Duration expiration) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + tokenHash;
            redisTemplate.opsForValue().set(key, "blacklisted", expiration.toSeconds(), TimeUnit.SECONDS);
            log.info("Токен добавлен в черный список: {}", tokenHash.substring(0, 8) + "...");
        } catch (Exception e) {
            log.error("Ошибка добавления токена в черный список", e);
        }
    }

    public boolean isTokenBlacklisted(String tokenHash) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + tokenHash;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Ошибка проверки черного списка токенов", e);
            return false;
        }
    }

    public String createTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Ошибка создания хеша токена", e);
            throw new RuntimeException("Ошибка создания хеша токена", e);
        }
    }

    public void createUserSession(String username, String sessionId, String clientIp, String userAgent) {
        try {
            String key = SESSION_PREFIX + username + ":" + sessionId;
            String sessionData = String.format("{\"ip\":\"%s\",\"userAgent\":\"%s\",\"created\":\"%s\"}",
                    clientIp, userAgent, Instant.now().toString());

            redisTemplate.opsForValue().set(key, sessionData, 24, TimeUnit.HOURS);
            log.debug("Создана сессия для пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка создания сессии пользователя", e);
        }
    }

    public boolean checkRateLimit(String clientIp, int maxRequests, Duration timeWindow) {
        try {
            String key = RATE_LIMIT_PREFIX + clientIp;
            String currentCount = redisTemplate.opsForValue().get(key);

            if (currentCount == null) {
                redisTemplate.opsForValue().set(key, "1", timeWindow.toSeconds(), TimeUnit.SECONDS);
                return true;
            }

            int count = Integer.parseInt(currentCount);
            if (count >= maxRequests) {
                log.warn("Превышен лимит запросов для IP: {}", clientIp);
                return false;
            }

            redisTemplate.opsForValue().increment(key);
            return true;

        } catch (Exception e) {
            log.error("Ошибка проверки лимита запросов", e);
            return true;
        }
    }

    public String generateSecureToken(int length) {
        byte[] randomBytes = new byte[length];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public boolean isTokenCompromised(String tokenHash) {
        try {
            String key = "compromised:token:" + tokenHash;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Ошибка проверки компрометации токена", e);
            return false;
        }
    }
}