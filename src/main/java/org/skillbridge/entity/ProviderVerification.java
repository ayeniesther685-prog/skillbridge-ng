package org.skillbridge.entity;

import jakarta.persistence.*;
import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.VerificationStatus;

import java.time.Instant;

@Entity
@Table(name = "provider_verifications")
public class ProviderVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private ProviderType providerType;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "cac_registration_number", length = 50)
    private String cacRegistrationNumber;

    @Column(name = "official_email", nullable = false, length = 180)
    private String officialEmail;

    @Column(name = "official_phone", nullable = false, length = 30)
    private String officialPhone;

    @Column(name = "physical_address", nullable = false, length = 500)
    private String physicalAddress;

    @Column(name = "website", length = 300)
    private String website;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "authorized_rep_name", nullable = false, length = 200)
    private String authorizedRepName;

    @Column(name = "authorized_rep_role", nullable = false, length = 100)
    private String authorizedRepRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private VerificationStatus status = VerificationStatus.UNVERIFIED;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_admin_id")
    private User verifiedByAdmin;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "admin_notes", length = 2000)
    private String adminNotes;

    @Column(name = "last_recheck_at")
    private Instant lastRecheckAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ProviderType getProviderType() { return providerType; }
    public void setProviderType(ProviderType providerType) { this.providerType = providerType; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getCacRegistrationNumber() { return cacRegistrationNumber; }
    public void setCacRegistrationNumber(String cacRegistrationNumber) { this.cacRegistrationNumber = cacRegistrationNumber; }

    public String getOfficialEmail() { return officialEmail; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }

    public String getOfficialPhone() { return officialPhone; }
    public void setOfficialPhone(String officialPhone) { this.officialPhone = officialPhone; }

    public String getPhysicalAddress() { return physicalAddress; }
    public void setPhysicalAddress(String physicalAddress) { this.physicalAddress = physicalAddress; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getAuthorizedRepName() { return authorizedRepName; }
    public void setAuthorizedRepName(String authorizedRepName) { this.authorizedRepName = authorizedRepName; }

    public String getAuthorizedRepRole() { return authorizedRepRole; }
    public void setAuthorizedRepRole(String authorizedRepRole) { this.authorizedRepRole = authorizedRepRole; }

    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public User getVerifiedByAdmin() { return verifiedByAdmin; }
    public void setVerifiedByAdmin(User verifiedByAdmin) { this.verifiedByAdmin = verifiedByAdmin; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public Instant getLastRecheckAt() { return lastRecheckAt; }
    public void setLastRecheckAt(Instant lastRecheckAt) { this.lastRecheckAt = lastRecheckAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}