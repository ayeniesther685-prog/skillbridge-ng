package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.skillbridge.dto.response.AdminTreasuryResponse;
import org.skillbridge.dto.response.CommissionRecordResponse;
import org.skillbridge.entity.PlatformCommission;
import org.skillbridge.entity.User;
import org.skillbridge.entity.Wallet;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.PlatformCommissionRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.repository.WalletRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/treasury")
@Tag(name = "Admin Treasury")
public class AdminTreasuryController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PlatformCommissionRepository commissionRepository;

    public AdminTreasuryController(UserRepository userRepository,
                                   WalletRepository walletRepository,
                                   PlatformCommissionRepository commissionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.commissionRepository = commissionRepository;
    }

    @GetMapping
    public ResponseEntity<AdminTreasuryResponse> summary() {
        User treasury = userRepository.findByEmailIgnoreCase("treasury@skillbridge.ng")
                .orElseThrow(() -> new ResourceNotFoundException("Treasury not found"));

        Wallet wallet = walletRepository.findByUserId(treasury.getId()).orElse(null);
        int balancePoints = wallet != null ? wallet.getBalancePoints() : 0;

        BigDecimal conversionRate = new BigDecimal("10.00");
        BigDecimal balanceNaira = conversionRate.multiply(BigDecimal.valueOf(balancePoints));

        long totalRecorded = commissionRepository.countByStatus("RECORDED");

        List<CommissionRecordResponse> recent = commissionRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(new AdminTreasuryResponse(
                treasury.getId(),
                balancePoints,
                balanceNaira,
                conversionRate,
                totalRecorded,
                recent
        ));
    }

    private CommissionRecordResponse toResponse(PlatformCommission c) {
        User freelancer = c.getFreelancer();
        String name = (freelancer.getFirstName() + " " + freelancer.getLastName()).trim();
        return new CommissionRecordResponse(
                c.getId(),
                c.getContract().getId(),
                c.getContract().getTitle(),
                c.getAmountPoints(),
                BigDecimal.TEN.multiply(BigDecimal.valueOf(c.getAmountPoints())),
                c.getFeePercent(),
                freelancer.getId(),
                name,
                c.getStatus(),
                c.getCreatedAt()
        );
    }
}