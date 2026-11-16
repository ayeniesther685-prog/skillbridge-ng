package org.skillbridge.service;

import org.skillbridge.dto.request.ProviderVerificationSubmitRequest;
import org.skillbridge.dto.response.ProviderVerificationEventResponse;
import org.skillbridge.dto.response.ProviderVerificationResponse;
import org.skillbridge.dto.response.ProviderVerificationStatusResponse;
import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProviderVerificationService {

    ProviderVerificationResponse getMine();

    ProviderVerificationStatusResponse getStatusForUser(Long userId);

    ProviderVerificationResponse submit(ProviderVerificationSubmitRequest req);

    Page<ProviderVerificationResponse> adminList(VerificationStatus status, ProviderType type, Pageable pageable);

    ProviderVerificationResponse adminGet(Long id);

    List<ProviderVerificationEventResponse> adminEvents(Long id);

    ProviderVerificationResponse adminApprove(Long id, String reason);

    ProviderVerificationResponse adminReject(Long id, String reason);

    ProviderVerificationResponse adminSuspend(Long id, String reason);

    ProviderVerificationResponse adminRevoke(Long id, String reason);

    boolean isVerified(Long userId);
}