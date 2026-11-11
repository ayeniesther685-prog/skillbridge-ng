package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.skillbridge.dto.response.CertificateResponse;
import org.skillbridge.dto.response.CertificateVerificationResponse;
import org.skillbridge.service.impl.CertificateServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Certificates")
public class CertificateController {

    private final CertificateServiceImpl service;

    public CertificateController(CertificateServiceImpl service) { this.service = service; }

    @PostMapping("/academy/courses/{courseId}/certificate")
    public ResponseEntity<CertificateResponse> issue(@PathVariable Long courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.issueCertificate(courseId));
    }

    @GetMapping("/academy/me/certificates")
    public ResponseEntity<List<CertificateResponse>> mine() {
        return ResponseEntity.ok(service.listMyCertificates());
    }

    @GetMapping("/certificates/verify/{certificateNumber}")
    public ResponseEntity<CertificateVerificationResponse> verify(@PathVariable String certificateNumber) {
        return ResponseEntity.ok(service.verify(certificateNumber));
    }
}