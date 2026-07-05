package com.mailpilot.repository;

import com.mailpilot.entity.SenderAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SenderAccountRepository extends JpaRepository<SenderAccount, Long> {

    List<SenderAccount> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SenderAccount> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT sa FROM SenderAccount sa WHERE sa.user.id = :userId AND sa.active = true")
    Optional<SenderAccount> findActiveByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);
}