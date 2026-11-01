package com.examportal.exam.controller;

import com.examportal.exam.dto.response.PublicExamResponse;
import com.examportal.exam.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/exams")
@RequiredArgsConstructor
@Tag(name = "Public Exam Access", description = "Public student-facing endpoint to access published examinations via share code")
public class PublicExamController {

    private final ExamService examService;

    @GetMapping("/{shareCode}")
    @Operation(
            summary = "Get public exam details by share code",
            description = "Retrieves student-safe examination details using a public share code. Requires no authentication."
    )
    @ApiResponse(responseCode = "200", description = "Public exam details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Exam not found or not published")
    public ResponseEntity<PublicExamResponse> getPublicExamByShareCode(@PathVariable String shareCode) {
        PublicExamResponse response = examService.getPublicExamByShareCode(shareCode);
        return ResponseEntity.ok(response);
    }
}
