package org.skillbridge.service.impl;

import org.skillbridge.dto.response.SkillResponse;
import org.skillbridge.entity.Skill;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.SkillMapper;
import org.skillbridge.repository.SkillRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserSkillServiceImpl {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final CurrentUser currentUser;

    public UserSkillServiceImpl(UserRepository userRepository, SkillRepository skillRepository,
                                SkillMapper skillMapper, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.skillMapper = skillMapper;
        this.currentUser = currentUser;
    }

    public List<SkillResponse> listSkillsForUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        u.getSkills().size();
        return u.getSkills().stream().map(skillMapper::toResponse).toList();
    }

    public List<SkillResponse> addSkillToCurrentUser(Long skillId) {
        User u = currentUser.require();
        Skill s = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + skillId));
        u.getSkills().size();
        u.addSkill(s);
        userRepository.save(u);
        return u.getSkills().stream().map(skillMapper::toResponse).toList();
    }

    public List<SkillResponse> removeSkillFromCurrentUser(Long skillId) {
        User u = currentUser.require();
        u.getSkills().size();
        u.getSkills().removeIf(s -> s.getId().equals(skillId));
        userRepository.save(u);
        return u.getSkills().stream().map(skillMapper::toResponse).toList();
    }
}