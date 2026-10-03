package com.academiaindustry.repository;

import com.academiaindustry.entity.StudentPortfolioItem;
import com.academiaindustry.entity.PortfolioItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentPortfolioItemRepository extends JpaRepository<StudentPortfolioItem, Long> {
    List<StudentPortfolioItem> findByStudentUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    Optional<StudentPortfolioItem> findByIdAndStudentUserEmailIgnoreCase(Long id, String email);
    List<StudentPortfolioItem> findByIdInAndStudentIdAndTypeIn(
            Collection<Long> ids, Long studentId, Collection<PortfolioItemType> types);
}
