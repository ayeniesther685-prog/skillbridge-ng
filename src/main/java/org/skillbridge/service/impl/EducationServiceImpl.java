package org.skillbridge.service.impl;

import org.skillbridge.dto.request.EducationRequest;
import org.skillbridge.dto.response.EducationResponse;
import org.skillbridge.entity.Education;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.EducationMapper;
import org.skillbridge.repository.EducationRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EducationServiceImpl {

    private final EducationRepository repo;
    private final UserRepository userRepository;
    private final EducationMapper mapper;
    private final CurrentUser currentUser;

    public EducationServiceImpl(EducationRepository repo, UserRepository userRepository,
                                EducationMapper mapper, CurrentUser currentUser) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<EducationResponse> listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return repo.findByUserIdOrderByStartDateDesc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public EducationResponse create(EducationRequest req) {
        User u = currentUser.require();
        Education e = new Education();
        e.setUser(u);
        apply(e, req);
        return mapper.toResponse(repo.save(e));
    }

    @Transactional
    public EducationResponse update(Long id, EducationRequest req) {
        User u = currentUser.require();
        Education e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found with id " + id));
        if (!e.getUser().getId().equals(u.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your record");
        }
        apply(e, req);
        return mapper.toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        User u = currentUser.require();
        Education e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found with id " + id));
        if (!e.getUser().getId().equals(u.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your record");
        }
        repo.delete(e);
    }

    private void apply(Education e, EducationRequest r) {
        e.setInstitution(r.institution());
        e.setDegree(r.degree());
        e.setFieldOfStudy(r.fieldOfStudy());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setDescription(r.description());
    }
}