package com.examportal.exam.repository;

import com.examportal.exam.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    Optional<Exam> findByIdAndCreatorId(UUID id, UUID creatorId);

    Page<Exam> findAllByCreatorId(UUID creatorId, Pageable pageable);

    Optional<Exam> findByShareCode(String shareCode);

    boolean existsByShareCode(String shareCode);
}
