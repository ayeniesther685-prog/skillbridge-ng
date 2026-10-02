package org.skillbridge.mapper;

import org.skillbridge.dto.response.CertificationResponse;
import org.skillbridge.entity.Certification;
import org.springframework.stereotype.Component;

@Component
public class CertificationMapper {
    public CertificationResponse toResponse(Certification c) {
        return new CertificationResponse(
                c.getId(), c.getUser().getId(), c.getName(), c.getIssuingOrganization(),
                c.getIssueDate(), c.getExpiryDate(), c.getCredentialUrl());
    }
}