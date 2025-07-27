package ru.t1.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.t1.authservice.model.RefreshTokenEntity;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String > {
}
