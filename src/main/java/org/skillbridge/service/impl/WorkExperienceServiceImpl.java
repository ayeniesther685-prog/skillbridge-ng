package org.skillbridge.service.impl;

import org.skillbridge.dto.request.WorkExperienceRequest;
import org.skillbridge.dto.response.WorkExperienceResponse;
import org.skillbridge.entity.User;
import org.skillbridge.entity.WorkExperience;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.WorkExperienceMapper;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.repository.WorkExperienceRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkExperienceServiceImpl {

    private final WorkExperienceRepository repo;
    private final UserRepository userRepository;
    private final WorkExperienceMapper mapper;
    private final CurrentUser currentUser;

    public WorkExperienceServiceImpl(WorkExperienceRepository repo, UserRepository userRepository,
                                     WorkExperienceMapper mapper, CurrentUser currentUser) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<WorkExperienceResponse> listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return repo.findByUserIdOrderByStartDateDesc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public WorkExperienceResponse create(WorkExperienceRequest req) {
        User u = currentUser.require();
        WorkExperience w = new WorkExperience();
        w.setUser(u);
        apply(w, req);
        return mapper.toResponse(repo.save(w));
    }

    @Transactional
    public WorkExperienceResponse update(Long id, WorkExperienceRequest req) {
        User u = currentUser.require();
        WorkExperience w = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id " + id));
        if (!w.getUser().getId().equals(u.getId())) throw new AccessDeniedException("Not your record");
        apply(w, req);
        return mapper.toResponse(repo.save(w));
    }

    @Transactional
    public void delete(Long id) {
        User u = currentUser.require();
        WorkExperience w = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id " + id));
        if (!w.getUser().getId().equals(u.getId())) throw new AccessDeniedException("Not your record");
        repo.delete(w);
    }

    private void apply(WorkExperience w, WorkExperienceRequest r) {
        w.setCompany(r.company());
        w.setPosition(r.position());
        w.setDescription(r.description());
        w.setStartDate(r.startDate());
        w.setEndDate(r.endDate());
        w.setCurrent(Boolean.TRUE.equals(r.current()));
    }
}