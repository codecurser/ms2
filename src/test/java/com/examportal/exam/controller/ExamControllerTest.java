package com.examportal.exam.controller;

import com.examportal.exam.config.SecurityConfig;
import com.examportal.exam.dto.request.CreateExamRequest;
import com.examportal.exam.dto.response.ExamResponse;
import com.examportal.exam.entity.ExamStatus;
import com.examportal.exam.security.JwtAuthenticationFilter;
import com.examportal.exam.security.JwtService;
import com.examportal.exam.service.ExamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExamController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExamService examService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/v1/exams without token should return 403 Forbidden")
    void createExam_Unauthenticated_Forbidden() throws Exception {
        CreateExamRequest request = CreateExamRequest.builder()
                .title("Java Spring Boot Test")
                .durationMinutes(60)
                .build();

        mockMvc.perform(post("/api/v1/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_CREATOR")
    @DisplayName("POST /api/v1/exams with CREATOR authority and invalid payload should return 400 Bad Request")
    void createExam_InvalidPayload_BadRequest() throws Exception {
        CreateExamRequest request = CreateExamRequest.builder()
                .title("A") // Title too short (< 3 chars)
                .durationMinutes(0) // Duration invalid (< 1 min)
                .build();

        mockMvc.perform(post("/api/v1/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
