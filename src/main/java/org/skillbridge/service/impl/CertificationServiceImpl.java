package org.skillbridge.service.impl;

import org.skillbridge.dto.request.CertificationRequest;
import org.skillbridge.dto.response.CertificationResponse;
import org.skillbridge.entity.Certification;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.CertificationMapper;
import org.skillbridge.repository.CertificationRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CertificationServiceImpl {

    private final CertificationRepository repo;
    private final UserRepository userRepository;
    private final CertificationMapper mapper;
    private final CurrentUser currentUser;

    public CertificationServiceImpl(CertificationRepository repo, UserRepository userRepository,
                                    CertificationMapper mapper, CurrentUser currentUser) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<CertificationResponse> listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return repo.findByUserIdOrderByIssueDateDesc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CertificationResponse create(CertificationRequest req) {
        User u = currentUser.require();
        Certification c = new Certification();
        c.setUser(u);
        apply(c, req);
        return mapper.toResponse(repo.save(c));
    }

    @Transactional
    public CertificationResponse update(Long id, CertificationRequest req) {
        User u = currentUser.require();
        Certification c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id " + id));
        if (!c.getUser().getId().equals(u.getId())) throw new AccessDeniedException("Not your record");
        apply(c, req);
        return mapper.toResponse(repo.save(c));
    }

    @Transactional
    public void delete(Long id) {
        User u = currentUser.require();
        Certification c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id " + id));
        if (!c.getUser().getId().equals(u.getId())) throw new AccessDeniedException("Not your record");
        repo.delete(c);
    }

    private void apply(Certification c, CertificationRequest r) {
        c.setName(r.name());
        c.setIssuingOrganization(r.issuingOrganization());
        c.setIssueDate(r.issueDate());
        c.setExpiryDate(r.expiryDate());
        c.setCredentialUrl(r.credentialUrl());
    }
}