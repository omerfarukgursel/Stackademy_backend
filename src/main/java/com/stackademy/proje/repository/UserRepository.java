package com.stackademy.proje.repository;

import com.stackademy.proje.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // --- YENİ EKLENEN KRİTİK METOD ---
    // "Kullanıcı adı BU olsun VEYA E-postası BU olsun" diyoruz.
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRole(String role);

    // Sıralama için - puana göre azalan sıralama
    List<User> findByRoleOrderByTotalScoreDesc(String role);
}