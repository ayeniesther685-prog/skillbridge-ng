package org.skillbridge.service.impl;

import org.skillbridge.dto.request.PortfolioItemRequest;
import org.skillbridge.dto.response.PortfolioItemResponse;
import org.skillbridge.entity.PortfolioItem;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.PortfolioItemMapper;
import org.skillbridge.repository.PortfolioItemRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PortfolioItemServiceImpl {

    private final PortfolioItemRepository repo;
    private final UserRepository userRepository;
    private final PortfolioItemMapper mapper;
    private final CurrentUser currentUser;

    public PortfolioItemServiceImpl(PortfolioItemRepository repo, UserRepository userRepository,
                                    PortfolioItemMapper mapper, CurrentUser currentUser) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<PortfolioItemResponse> listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PortfolioItemResponse create(PortfolioItemRequest req) {
        User u = currentUser.require();
        PortfolioItem p = new PortfolioItem();
        p.setUser(u);
        apply(p, req);
        return mapper.toResponse(repo.save(p));
    }

    @Transactional
    public PortfolioItemResponse update(Long id, PortfolioItemRequest req) {
        User u = currentUser.require();
        PortfolioItem p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found with id " + id));
        if (!p.getUser().getId().equals(u.getId())) throw new AccessDeniedException("Not your record");
        apply(p, req);
        return mapper.toResponse(repo.save(p));
    }

    @Transactional
    public void delete(Long id) {
        User u = currentUser.require();
        PortfolioItem p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found with id " + id));
        if (!p.getUser().getId().equals(u.getId())) throw new AccessDeniedException("Not your record");
        repo.delete(p);
    }

    private void apply(PortfolioItem p, PortfolioItemRequest r) {
        p.setTitle(r.title());
        p.setDescription(r.description());
        p.setImageUrl(r.imageUrl());
        p.setProjectUrl(r.projectUrl());
        p.setTechnologies(r.technologies());
    }
}