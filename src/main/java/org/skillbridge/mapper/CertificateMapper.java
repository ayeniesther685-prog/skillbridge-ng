package org.skillbridge.mapper;

import org.skillbridge.dto.response.CertificateResponse;
import org.skillbridge.dto.response.CertificateVerificationResponse;
import org.skillbridge.entity.Certificate;
import org.skillbridge.entity.StudentRecord;
import org.skillbridge.entity.User;
import org.skillbridge.enums.CertificateStatus;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {

    public CertificateResponse toResponse(Certificate c) {
        StudentRecord sr = c.getStudentRecord();
        User student = sr.getUser();
        String studentName = (student.getFirstName() + " " + student.getLastName()).trim();

        return new CertificateResponse(
                c.getId(),
                c.getCertificateNumber(),
                c.getCourse().getId(),
                c.getCourse().getTitle(),
                sr.getStudentId(),
                studentName,
                student.getEmail(),
                c.getFinalScore(),
                c.getStatus().name(),
                c.getIssuedAt(),
                "/api/certificates/verify/" + c.getCertificateNumber()
        );
    }

    public CertificateVerificationResponse toVerificationResponse(Certificate c) {
        StudentRecord sr = c.getStudentRecord();
        User student = sr.getUser();
        String studentName = (student.getFirstName() + " " + student.getLastName()).trim();
        boolean revoked = c.getStatus() == CertificateStatus.REVOKED;

        return new CertificateVerificationResponse(
                !revoked,
                c.getCertificateNumber(),
                studentName,
                sr.getStudentId(),
                c.getCourse().getTitle(),
                c.getFinalScore(),
                c.getIssuedAt(),
                revoked ? "Certificate has been revoked" : null
        );
    }
}