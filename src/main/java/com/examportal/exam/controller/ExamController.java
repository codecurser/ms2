package com.examportal.exam.controller;

import com.examportal.exam.dto.request.CreateExamRequest;
import com.examportal.exam.dto.request.UpdateExamRequest;
import com.examportal.exam.dto.response.ExamResponse;
import com.examportal.exam.dto.response.ExamSummaryResponse;
import com.examportal.exam.dto.response.PageResponse;
import com.examportal.exam.dto.response.ShareLinkResponse;
import com.examportal.exam.service.ExamService;
import com.examportal.exam.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exam Management", description = "Endpoints for authenticated exam creators to manage examinations")
@SecurityRequirement(name = "bearerAuth")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @Operation(summary = "Create a new exam draft", description = "Creates an examination draft for the authenticated creator.")
    @ApiResponse(responseCode = "201", description = "Exam draft created successfully")
    @ApiResponse(responseCode = "400", description = "Validation or payload error")
    public ResponseEntity<ExamResponse> createExam(@Valid @RequestBody CreateExamRequest request) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        ExamResponse response = examService.createExam(creatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get creator's exams", description = "Returns a paginated list of examinations created by the authenticated creator.")
    @ApiResponse(responseCode = "200", description = "Exams retrieved successfully")
    public ResponseEntity<PageResponse<ExamSummaryResponse>> getCreatorExams(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        PageResponse<ExamSummaryResponse> response = examService.getCreatorExams(creatorId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}")
    @Operation(summary = "Get exam by ID", description = "Retrieves an examination by ID. Must belong to the authenticated creator.")
    @ApiResponse(responseCode = "200", description = "Exam retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Exam not found or unauthorized access")
    public ResponseEntity<ExamResponse> getExamById(@PathVariable UUID examId) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        ExamResponse response = examService.getExamById(creatorId, examId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{examId}")
    @Operation(summary = "Update exam", description = "Updates an examination's details. Must belong to the authenticated creator.")
    @ApiResponse(responseCode = "200", description = "Exam updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid exam state or payload validation failure")
    @ApiResponse(responseCode = "404", description = "Exam not found or unauthorized access")
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable UUID examId,
            @Valid @RequestBody UpdateExamRequest request) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        ExamResponse response = examService.updateExam(creatorId, examId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{examId}")
    @Operation(summary = "Delete exam draft", description = "Deletes an exam draft. Only allowed if exam status is DRAFT.")
    @ApiResponse(responseCode = "204", description = "Exam draft deleted successfully")
    @ApiResponse(responseCode = "400", description = "Cannot delete published/active/closed exams")
    @ApiResponse(responseCode = "404", description = "Exam not found or unauthorized access")
    public ResponseEntity<Void> deleteExam(@PathVariable UUID examId) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        examService.deleteExam(creatorId, examId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{examId}/publish")
    @Operation(summary = "Publish exam", description = "Publishes an exam draft, generates a unique share code, and creates a student share link.")
    @ApiResponse(responseCode = "200", description = "Exam published successfully")
    @ApiResponse(responseCode = "400", description = "Invalid exam state for publishing")
    @ApiResponse(responseCode = "404", description = "Exam not found or unauthorized access")
    public ResponseEntity<ShareLinkResponse> publishExam(@PathVariable UUID examId) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        ShareLinkResponse response = examService.publishExam(creatorId, examId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}/share-link")
    @Operation(summary = "Get exam share link", description = "Retrieves the public share link for a published exam.")
    @ApiResponse(responseCode = "200", description = "Share link retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Exam is not published yet")
    @ApiResponse(responseCode = "404", description = "Exam not found or unauthorized access")
    public ResponseEntity<ShareLinkResponse> getShareLink(@PathVariable UUID examId) {
        UUID creatorId = SecurityUtil.getCurrentCreatorId();
        ShareLinkResponse response = examService.getShareLink(creatorId, examId);
        return ResponseEntity.ok(response);
    }
}
