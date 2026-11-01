package com.examportal.exam.service;

import com.examportal.exam.dto.request.CreateExamRequest;
import com.examportal.exam.dto.request.UpdateExamRequest;
import com.examportal.exam.dto.response.ExamResponse;
import com.examportal.exam.dto.response.ExamSummaryResponse;
import com.examportal.exam.dto.response.PageResponse;
import com.examportal.exam.dto.response.PublicExamResponse;
import com.examportal.exam.dto.response.ShareLinkResponse;
import com.examportal.exam.entity.Exam;
import com.examportal.exam.entity.ExamStatus;
import com.examportal.exam.exception.ExamNotFoundException;
import com.examportal.exam.exception.InvalidExamStateException;
import com.examportal.exam.exception.UnauthorizedExamAccessException;
import com.examportal.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "title", "startTime", "status"
    );

    private final ExamRepository examRepository;
    private final ShareCodeService shareCodeService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public ExamResponse createExam(UUID creatorId, CreateExamRequest request) {
        validateExamTimeRange(request.getStartTime(), request.getEndTime());

        Exam exam = Exam.builder()
                .creatorId(creatorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ExamStatus.DRAFT)
                .build();

        Exam savedExam = examRepository.save(exam);
        log.info("Exam created with ID: {} by creator: {}", savedExam.getId(), creatorId);
        return mapToExamResponse(savedExam);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExamSummaryResponse> getCreatorExams(UUID creatorId, Pageable pageable) {
        Pageable validatedPageable = validateAndSanitizePageable(pageable);
        Page<Exam> examPage = examRepository.findAllByCreatorId(creatorId, validatedPageable);
        Page<ExamSummaryResponse> summaryPage = examPage.map(this::mapToExamSummaryResponse);
        return PageResponse.from(summaryPage);
    }

    @Transactional(readOnly = true)
    public ExamResponse getExamById(UUID creatorId, UUID examId) {
        Exam exam = findExamAndVerifyOwner(creatorId, examId);
        return mapToExamResponse(exam);
    }

    @Transactional
    public ExamResponse updateExam(UUID creatorId, UUID examId, UpdateExamRequest request) {
        Exam exam = findExamAndVerifyOwner(creatorId, examId);

        if (exam.getStatus() == ExamStatus.ARCHIVED || exam.getStatus() == ExamStatus.CLOSED) {
            throw new InvalidExamStateException("Cannot update an exam with status " + exam.getStatus());
        }

        validateExamTimeRange(request.getStartTime(), request.getEndTime());

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());

        Exam updatedExam = examRepository.save(exam);
        log.info("Exam updated with ID: {} by creator: {}", updatedExam.getId(), creatorId);
        return mapToExamResponse(updatedExam);
    }

    @Transactional
    public void deleteExam(UUID creatorId, UUID examId) {
        Exam exam = findExamAndVerifyOwner(creatorId, examId);

        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new InvalidExamStateException("A " + exam.getStatus().name().toLowerCase() + " exam cannot be deleted");
        }

        examRepository.delete(exam);
        log.info("Exam deleted with ID: {} by creator: {}", examId, creatorId);
    }

    @Transactional
    public ShareLinkResponse publishExam(UUID creatorId, UUID examId) {
        Exam exam = findExamAndVerifyOwner(creatorId, examId);

        if (exam.getStatus() == ExamStatus.CLOSED || exam.getStatus() == ExamStatus.ARCHIVED) {
            throw new InvalidExamStateException("Cannot publish an exam with status " + exam.getStatus());
        }

        if (exam.getStatus() == ExamStatus.DRAFT) {
            String shareCode = shareCodeService.generateUniqueShareCode();
            exam.setShareCode(shareCode);
            exam.setStatus(ExamStatus.PUBLISHED);
            exam.setPublishedAt(Instant.now());
            examRepository.save(exam);
            log.info("Exam published with ID: {}, shareCode: {}", examId, shareCode);
        }

        return ShareLinkResponse.builder()
                .examId(exam.getId())
                .shareCode(exam.getShareCode())
                .shareUrl(buildShareUrl(exam.getShareCode()))
                .build();
    }

    @Transactional(readOnly = true)
    public ShareLinkResponse getShareLink(UUID creatorId, UUID examId) {
        Exam exam = findExamAndVerifyOwner(creatorId, examId);

        if (exam.getStatus() == ExamStatus.DRAFT || exam.getShareCode() == null) {
            throw new InvalidExamStateException("Exam must be published before a share link can be generated");
        }

        return ShareLinkResponse.builder()
                .examId(exam.getId())
                .shareCode(exam.getShareCode())
                .shareUrl(buildShareUrl(exam.getShareCode()))
                .build();
    }

    @Transactional(readOnly = true)
    public PublicExamResponse getPublicExamByShareCode(String shareCode) {
        Exam exam = examRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with share code: " + shareCode));

        if (exam.getStatus() == ExamStatus.DRAFT) {
            throw new ExamNotFoundException("Exam is not published yet");
        }

        return PublicExamResponse.builder()
                .examId(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .status(exam.getStatus())
                .build();
    }

    private Exam findExamAndVerifyOwner(UUID creatorId, UUID examId) {
        return examRepository.findById(examId)
                .map(exam -> {
                    if (!exam.getCreatorId().equals(creatorId)) {
                        log.warn("Access denied for creatorId {} on examId {}", creatorId, examId);
                        throw new UnauthorizedExamAccessException("Exam not found");
                    }
                    return exam;
                })
                .orElseThrow(() -> new ExamNotFoundException("Exam not found"));
    }

    private void validateExamTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private Pageable validateAndSanitizePageable(Pageable pageable) {
        int pageSize = Math.min(pageable.getPageSize(), 100);
        Sort sort = pageable.getSort();

        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException("Invalid sort field: " + order.getProperty() +
                        ". Allowed fields: " + ALLOWED_SORT_FIELDS);
            }
        }

        return PageRequest.of(pageable.getPageNumber(), pageSize, sort);
    }

    private String buildShareUrl(String shareCode) {
        String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return baseUrl + "/exam/" + shareCode;
    }

    private ExamResponse mapToExamResponse(Exam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .creatorId(exam.getCreatorId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .status(exam.getStatus())
                .shareCode(exam.getShareCode())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .publishedAt(exam.getPublishedAt())
                .build();
    }

    private ExamSummaryResponse mapToExamSummaryResponse(Exam exam) {
        return ExamSummaryResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .status(exam.getStatus())
                .durationMinutes(exam.getDurationMinutes())
                .createdAt(exam.getCreatedAt())
                .shareCode(exam.getShareCode())
                .build();
    }
}
