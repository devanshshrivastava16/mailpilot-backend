package com.mailpilot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sender_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SenderAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "gmail_address", nullable = false, length = 320)
    private String gmailAddress;

    @Column(name = "encrypted_app_password", nullable = false, columnDefinition = "TEXT")
    private String encryptedAppPassword;

    @Column(length = 100)
    private String label;

    @Column(name = "is_active")
    private boolean active = false;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public void setActive(boolean active) {
        this.active = active;
    }
}