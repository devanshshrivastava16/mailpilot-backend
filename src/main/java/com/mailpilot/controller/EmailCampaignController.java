package com.mailpilot.controller;

import com.mailpilot.dto.*;
import com.mailpilot.repository.UserRepository;
import com.mailpilot.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class EmailCampaignController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
    }

    @PostMapping("/preview")
    public ResponseEntity<List<CampaignPreviewItem>> preview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EmailCampaignRequest request) {
        return ResponseEntity.ok(emailService.generatePreview(getUserId(userDetails), request));
    }

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CampaignResponse> send(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") @Valid EmailCampaignRequest request,
            @RequestParam(required = false) MultipartFile resume) {
        return ResponseEntity.ok(emailService.sendCampaign(getUserId(userDetails), request, resume));
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getCampaigns(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(emailService.getCampaigns(getUserId(userDetails)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignDetailResponse> getCampaignDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(emailService.getCampaignDetail(getUserId(userDetails), id));
    }
}