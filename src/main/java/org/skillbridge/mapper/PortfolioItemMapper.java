package org.skillbridge.mapper;

import org.skillbridge.dto.response.PortfolioItemResponse;
import org.skillbridge.entity.PortfolioItem;
import org.springframework.stereotype.Component;

@Component
public class PortfolioItemMapper {
    public PortfolioItemResponse toResponse(PortfolioItem p) {
        return new PortfolioItemResponse(
                p.getId(), p.getUser().getId(), p.getTitle(), p.getDescription(),
                p.getImageUrl(), p.getProjectUrl(), p.getTechnologies(), p.getCreatedAt());
    }
}