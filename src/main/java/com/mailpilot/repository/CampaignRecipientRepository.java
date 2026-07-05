package com.mailpilot.repository;

import com.mailpilot.entity.CampaignRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRecipientRepository extends JpaRepository<CampaignRecipient, Long> {

    List<CampaignRecipient> findByCampaignIdOrderByRecipientName(Long campaignId);

    long countByCampaignIdAndStatus(Long campaignId, String status);
}