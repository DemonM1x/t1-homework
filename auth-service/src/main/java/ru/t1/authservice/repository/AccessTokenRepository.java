package ru.t1.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.t1.authservice.model.AccessTokenEntity;

public interface AccessTokenRepository extends CrudRepository<AccessTokenEntity, String> {
}
