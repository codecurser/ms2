package com.examportal.exam.service;

import com.examportal.exam.dto.request.CreateExamRequest;
import com.examportal.exam.dto.request.UpdateExamRequest;
import com.examportal.exam.dto.response.ExamResponse;
import com.examportal.exam.dto.response.PageResponse;
import com.examportal.exam.dto.response.PublicExamResponse;
import com.examportal.exam.dto.response.ShareLinkResponse;
import com.examportal.exam.entity.Exam;
import com.examportal.exam.entity.ExamStatus;
import com.examportal.exam.exception.ExamNotFoundException;
import com.examportal.exam.exception.InvalidExamStateException;
import com.examportal.exam.exception.UnauthorizedExamAccessException;
import com.examportal.exam.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ShareCodeService shareCodeService;

    @InjectMocks
    private ExamService examService;

    private UUID creatorId;
    private UUID examId;
    private Exam draftExam;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        examId = UUID.randomUUID();
        ReflectionTestUtils.setField(examService, "frontendUrl", "http://localhost:3000");

        draftExam = Exam.builder()
                .id(examId)
                .creatorId(creatorId)
                .title("Java Core Assessment")
                .description("Core Java test")
                .durationMinutes(60)
                .status(ExamStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create exam draft for authenticated creator")
    void createExam_Success() {
        CreateExamRequest request = CreateExamRequest.builder()
                .title("Java Core Assessment  ")
                .description("Core Java test")
                .durationMinutes(60)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> {
            Exam saved = invocation.getArgument(0);
            saved.setId(examId);
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            return saved;
        });

        ExamResponse response = examService.createExam(creatorId, request);

        assertNotNull(response);
        assertEquals(examId, response.getId());
        assertEquals(creatorId, response.getCreatorId());
        assertEquals("Java Core Assessment", response.getTitle());
        assertEquals(ExamStatus.DRAFT, response.getStatus());
        assertNull(response.getShareCode());
        verify(examRepository, times(1)).save(any(Exam.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when end time is before start time")
    void createExam_InvalidTimeRange() {
        CreateExamRequest request = CreateExamRequest.builder()
                .title("Java Core Assessment")
                .durationMinutes(60)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(1)) // invalid: end before start
                .build();

        assertThrows(IllegalArgumentException.class, () -> examService.createExam(creatorId, request));
        verify(examRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return paginated summary responses for creator")
    void getCreatorExams_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Exam> page = new PageImpl<>(List.of(draftExam), pageable, 1);

        when(examRepository.findAllByCreatorId(creatorId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))))
                .thenReturn(page);

        PageResponse<?> response = examService.getCreatorExams(creatorId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getExams().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should get exam by ID if owner matches")
    void getExamById_OwnerMatches() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(draftExam));

        ExamResponse response = examService.getExamById(creatorId, examId);

        assertNotNull(response);
        assertEquals(examId, response.getId());
        assertEquals(creatorId, response.getCreatorId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedExamAccessException when non-owner accesses exam")
    void getExamById_NonOwner_ThrowsException() {
        UUID strangerId = UUID.randomUUID();
        when(examRepository.findById(examId)).thenReturn(Optional.of(draftExam));

        assertThrows(UnauthorizedExamAccessException.class, () -> examService.getExamById(strangerId, examId));
    }

    @Test
    @DisplayName("Should delete exam draft successfully")
    void deleteExam_Draft_Success() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(draftExam));

        examService.deleteExam(creatorId, examId);

        verify(examRepository, times(1)).delete(draftExam);
    }

    @Test
    @DisplayName("Should throw InvalidExamStateException when attempting to delete published exam")
    void deleteExam_Published_ThrowsException() {
        draftExam.setStatus(ExamStatus.PUBLISHED);
        when(examRepository.findById(examId)).thenReturn(Optional.of(draftExam));

        assertThrows(InvalidExamStateException.class, () -> examService.deleteExam(creatorId, examId));
        verify(examRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should publish draft exam, generate share code and timestamp")
    void publishExam_Success() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(draftExam));
        when(shareCodeService.generateUniqueShareCode()).thenReturn("X7K92P88");

        ShareLinkResponse response = examService.publishExam(creatorId, examId);

        assertNotNull(response);
        assertEquals(examId, response.getExamId());
        assertEquals("X7K92P88", response.getShareCode());
        assertEquals("http://localhost:3000/exam/X7K92P88", response.getShareUrl());
        assertEquals(ExamStatus.PUBLISHED, draftExam.getStatus());
        assertNotNull(draftExam.getPublishedAt());
        verify(examRepository, times(1)).save(draftExam);
    }

    @Test
    @DisplayName("Should throw InvalidExamStateException when getting share link for draft exam")
    void getShareLink_DraftExam_ThrowsException() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(draftExam));

        assertThrows(InvalidExamStateException.class, () -> examService.getShareLink(creatorId, examId));
    }

    @Test
    @DisplayName("Should return safe public exam response for published exam share code")
    void getPublicExamByShareCode_Success() {
        draftExam.setStatus(ExamStatus.PUBLISHED);
        draftExam.setShareCode("X7K92P88");
        when(examRepository.findByShareCode("X7K92P88")).thenReturn(Optional.of(draftExam));

        PublicExamResponse response = examService.getPublicExamByShareCode("X7K92P88");

        assertNotNull(response);
        assertEquals(examId, response.getExamId());
        assertEquals("Java Core Assessment", response.getTitle());
        assertEquals(ExamStatus.PUBLISHED, response.getStatus());
    }
}
