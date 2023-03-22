package com.bulbul.authservice.repository;

import com.bulbul.authservice.entity.ERole;
import com.bulbul.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(ERole roleUser);
}
