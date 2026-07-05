package com.mailpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDetailResponse extends CampaignResponse {

    private List<RecipientDetailResponse> recipients;

    public static CampaignDetailResponse from(CampaignResponse base, List<RecipientDetailResponse> recipients) {
        CampaignDetailResponse detail = new CampaignDetailResponse();
        detail.setId(base.getId());
        detail.setSenderEmail(base.getSenderEmail());
        detail.setSubject(base.getSubject());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setTotalRecipients(base.getTotalRecipients());
        detail.setSuccessfulSends(base.getSuccessfulSends());
        detail.setFailedSends(base.getFailedSends());
        detail.setRecipients(recipients);
        return detail;
    }
}