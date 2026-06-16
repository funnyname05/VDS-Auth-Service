package com.valledelsol.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);  // ← User, no UserDetails
    boolean existsByEmail(String email);
}