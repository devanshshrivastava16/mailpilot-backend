package com.mailpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignPreviewItem {

    private int serialNumber;
    private String recipientName;
    private String recipientEmail;
    private String companyName;
    private String previewHtml;
}