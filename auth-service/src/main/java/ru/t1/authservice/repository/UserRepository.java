package ru.t1.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.authservice.model.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
