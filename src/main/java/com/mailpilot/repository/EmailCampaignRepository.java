package com.mailpilot.repository;

import com.mailpilot.entity.EmailCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {

    List<EmailCampaign> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<EmailCampaign> findByIdAndUserId(Long id, Long userId);
}