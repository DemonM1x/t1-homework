package ru.t1.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.authservice.model.Role;
import ru.t1.authservice.model.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    RoleEntity findByRole(Role role);
}
