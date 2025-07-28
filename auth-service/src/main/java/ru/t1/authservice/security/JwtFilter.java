package ru.t1.authservice.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenSecurityService tokenSecurityService;

    @Value("${security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${security.rate-limit.max-requests:100}")
    private int maxRequestsPerMinute;

    @Override
    @SneakyThrows
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {

        String clientIp = jwtService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        if (rateLimitEnabled && !tokenSecurityService.checkRateLimit(clientIp, maxRequestsPerMinute, Duration.ofMinutes(1))) {
            response.setStatus(429);
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String fingerprintHeader = request.getHeader("X-Token-Fingerprint");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String encryptedToken = authHeader.substring(7);

            String tokenHash = tokenSecurityService.createTokenHash(encryptedToken);
            if (tokenSecurityService.isTokenBlacklisted(tokenHash)) {
                log.warn("Попытка использования токена из черного списка с IP: {}", clientIp);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                return;
            }

            if (tokenSecurityService.isTokenCompromised(tokenHash)) {
                log.warn("Попытка использования скомпрометированного токена с IP: {}", clientIp);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is compromised");
                return;
            }

            if (!tokenSecurityService.verifyTokenFingerprint(encryptedToken, userAgent, clientIp, fingerprintHeader)) {
                log.warn("Неверный отпечаток токена с IP: {}, User-Agent: {}", clientIp, userAgent);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token fingerprint");
                return;
            }

            final String username = jwtService.extractUsername(encryptedToken);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(encryptedToken, userDetails.getUsername())) {
                    List<String> roles = jwtService.extractRoles(encryptedToken);
                    Collection<? extends GrantedAuthority> authorities;

                    if (roles != null && !roles.isEmpty()) {
                        authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .map(GrantedAuthority.class::cast)
                                .toList();
                    } else {
                        authorities = userDetails.getAuthorities();
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Успешная аутентификация пользователя: {} с IP: {}", username, clientIp);
                } else {
                    log.warn("Недействительный токен для пользователя: {} с IP: {}", username, clientIp);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Ошибка обработки JWT токена с IP: {}, ошибка: {}", clientIp, e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        } catch (Exception e) {
            log.error("Неожиданная ошибка в JWT фильтре", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        }
    }
}