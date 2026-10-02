package org.skillbridge.service.impl;

import org.skillbridge.dto.request.SkillRequest;
import org.skillbridge.dto.response.SkillResponse;
import org.skillbridge.entity.Skill;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.SkillMapper;
import org.skillbridge.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SkillServiceImpl {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public SkillServiceImpl(SkillRepository skillRepository, SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.skillMapper = skillMapper;
    }

    public List<SkillResponse> findAll(String category) {
        List<Skill> list = (category == null || category.isBlank())
                ? skillRepository.findAllByOrderByNameAsc()
                : skillRepository.findByCategoryIgnoreCaseOrderByNameAsc(category);
        return list.stream().map(skillMapper::toResponse).toList();
    }

    public SkillResponse findById(Long id) {
        return skillMapper.toResponse(skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + id)));
    }

    @Transactional
    public SkillResponse create(SkillRequest req) {
        if (skillRepository.existsByNameIgnoreCase(req.name())) {
            throw new DuplicateResourceException("Skill already exists: " + req.name());
        }
        Skill s = new Skill();
        s.setName(req.name());
        s.setDescription(req.description());
        s.setCategory(req.category());
        return skillMapper.toResponse(skillRepository.save(s));
    }

    @Transactional
       public SkillResponse update(Long id, SkillRequest req) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + id));
        if (!s.getName().equalsIgnoreCase(req.name()) && skillRepository.existsByNameIgnoreCase(req.name())) {
            throw new DuplicateResourceException("Skill already exists: " + req.name());
        }
        s.setName(req.name());
        s.setDescription(req.description());
        s.setCategory(req.category());
        return skillMapper.toResponse(skillRepository.save(s));

}

   @Transactional
    public void delete(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found with id " + id);
        }
        skillRepository.deleteById(id);
    }
}