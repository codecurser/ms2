package com.examportal.exam.dto.response;

import com.examportal.exam.entity.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResponse {

    private UUID id;
    private UUID creatorId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExamStatus status;
    private String shareCode;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;
}
