package ru.t1.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.authservice.model.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
    TokenEntity findByToken(String token);
}
