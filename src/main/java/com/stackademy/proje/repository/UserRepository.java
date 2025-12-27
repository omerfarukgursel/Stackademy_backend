package com.stackademy.proje.repository;

import com.stackademy.proje.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    // Sıralama için - puana göre azalan sıralama
    List<User> findByRoleOrderByTotalScoreDesc(String role);
}