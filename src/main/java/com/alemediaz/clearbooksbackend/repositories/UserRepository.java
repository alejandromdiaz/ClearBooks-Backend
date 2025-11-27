package com.alemediaz.clearbooksbackend.repositories;

import com.alemediaz.clearbooksbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByVatNumber(String vatNumber);
    Optional<User> findByEmail(String email);
    boolean existsByVatNumber(String vatNumber);
    boolean existsByEmail(String email);
}
