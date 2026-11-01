package com.examportal.exam.controller;

import com.examportal.exam.config.SecurityConfig;
import com.examportal.exam.dto.response.PublicExamResponse;
import com.examportal.exam.entity.ExamStatus;
import com.examportal.exam.security.JwtAuthenticationFilter;
import com.examportal.exam.security.JwtService;
import com.examportal.exam.service.ExamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicExamController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamService examService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/public/exams/{shareCode} should be accessible without authentication")
    void getPublicExam_Unauthenticated_Success() throws Exception {
        String shareCode = "X7K92P88";
        UUID examId = UUID.randomUUID();

        PublicExamResponse publicResponse = PublicExamResponse.builder()
                .examId(examId)
                .title("Java Certification")
                .description("Public test description")
                .durationMinutes(90)
                .status(ExamStatus.PUBLISHED)
                .build();

        when(examService.getPublicExamByShareCode(shareCode)).thenReturn(publicResponse);

        mockMvc.perform(get("/api/v1/public/exams/" + shareCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examId").value(examId.toString()))
                .andExpect(jsonPath("$.title").value("Java Certification"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.creatorId").doesNotExist()); // Verification: creatorId MUST NOT exist
    }
}
