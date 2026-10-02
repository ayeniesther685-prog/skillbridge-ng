package org.skillbridge.repository;

import org.skillbridge.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    List<PortfolioItem> findByUserIdOrderByCreatedAtDesc(Long userId);
}