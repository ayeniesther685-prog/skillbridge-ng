package org.skillbridge.service.impl;

import org.skillbridge.entity.Contract;
import org.skillbridge.entity.PlatformCommission;
import org.skillbridge.entity.PlatformFeeConfig;
import org.skillbridge.entity.User;
import org.skillbridge.enums.WalletTransactionType;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.PlatformCommissionRepository;
import org.skillbridge.repository.PlatformFeeConfigRepository;
import org.skillbridge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class CommissionServiceImpl {

    private final PlatformCommissionRepository commissionRepository;
    private final PlatformFeeConfigRepository feeConfigRepository;
    private final UserRepository userRepository;
    private final WalletServiceImpl walletService;

    public CommissionServiceImpl(PlatformCommissionRepository commissionRepository,
                                 PlatformFeeConfigRepository feeConfigRepository,
                                 UserRepository userRepository,
                                 WalletServiceImpl walletService) {
        this.commissionRepository = commissionRepository;
        this.feeConfigRepository = feeConfigRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @Transactional
    public void recordCommissionForContract(Contract contract) {
        if (commissionRepository.existsByContractId(contract.getId())) {
            return;
        }

        User freelancer = contract.getFreelancer();
        String roleName = determineRole(freelancer);

        PlatformFeeConfig config = feeConfigRepository
                .findByRoleNameIgnoreCaseAndActiveTrue(roleName)
                .orElse(null);

        if (config == null) {
            return;
        }

        int contractAmountPoints = contract.getAmount()
                .divide(BigDecimal.TEN, RoundingMode.DOWN)
                .intValue();

        if (contractAmountPoints <= 0) return;

        BigDecimal commissionAmount = BigDecimal.valueOf(contractAmountPoints)
                .multiply(config.getFeePercent())
                .divide(BigDecimal.valueOf(100), RoundingMode.DOWN);
        int commissionPoints = commissionAmount.intValue();

        if (commissionPoints <= 0) return;

        PlatformCommission commission = new PlatformCommission();
        commission.setContract(contract);
        commission.setAmountPoints(commissionPoints);
        commission.setFeePercent(config.getFeePercent());
        commission.setContractAmount(contractAmountPoints);
        commission.setFreelancer(freelancer);
        commission.setStatus("RECORDED");
        commissionRepository.save(commission);

        User treasury = userRepository.findByEmailIgnoreCase("treasury@skillbridge.ng")
                .orElseThrow(() -> new ResourceNotFoundException("Treasury user not found"));

        walletService.creditPoints(
                treasury.getId(),
                commissionPoints,
                WalletTransactionType.JOB_EARNING,
                "COMMISSION-CONTRACT-" + contract.getId(),
                "Platform fee from contract: " + contract.getTitle()
        );
    }

    private String determineRole(User user) {
        if (user.getRoles().stream().anyMatch(r -> r.getName().name().equals("FREELANCER"))) {
            return "FREELANCER";
        }
        if (user.getRoles().stream().anyMatch(r -> r.getName().name().equals("STUDENT"))) {
            return "STUDENT";
        }
        return "FREELANCER";
    }
}