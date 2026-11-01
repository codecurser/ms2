package com.examportal.exam.integration;

import com.examportal.exam.dto.request.CreateExamRequest;
import com.examportal.exam.dto.response.ExamResponse;
import com.examportal.exam.dto.response.PageResponse;
import com.examportal.exam.dto.response.PublicExamResponse;
import com.examportal.exam.dto.response.ShareLinkResponse;
import com.examportal.exam.entity.ExamStatus;
import com.examportal.exam.repository.ExamRepository;
import com.examportal.exam.service.ExamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ExamServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("exam_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Test
    @DisplayName("End to end creation, publication, and public querying via Testcontainers PostgreSQL")
    void endToEndExamLifecycle() {
        UUID creatorId = UUID.randomUUID();

        // 1. Create Exam Draft
        CreateExamRequest createRequest = CreateExamRequest.builder()
                .title("Database Engineering Assessment")
                .description("Advanced PostgreSQL and Database Design")
                .durationMinutes(90)
                .build();

        ExamResponse createdExam = examService.createExam(creatorId, createRequest);

        assertNotNull(createdExam);
        assertNotNull(createdExam.getId());
        assertEquals(creatorId, createdExam.getCreatorId());
        assertEquals(ExamStatus.DRAFT, createdExam.getStatus());

        // 2. Fetch Creator Exams
        PageResponse<?> pageResponse = examService.getCreatorExams(
                creatorId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertEquals(1, pageResponse.getTotalElements());

        // 3. Publish Exam
        ShareLinkResponse shareLink = examService.publishExam(creatorId, createdExam.getId());
        assertNotNull(shareLink.getShareCode());
        assertTrue(shareLink.getShareUrl().contains(shareLink.getShareCode()));

        // 4. Query Public Exam by Share Code
        PublicExamResponse publicExam = examService.getPublicExamByShareCode(shareLink.getShareCode());
        assertNotNull(publicExam);
        assertEquals("Database Engineering Assessment", publicExam.getTitle());
        assertEquals(ExamStatus.PUBLISHED, publicExam.getStatus());
    }
}
