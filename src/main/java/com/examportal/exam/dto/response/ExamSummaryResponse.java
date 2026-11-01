package com.examportal.exam.dto.response;

import com.examportal.exam.entity.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSummaryResponse {

    private UUID id;
    private String title;
    private ExamStatus status;
    private Integer durationMinutes;
    private Instant createdAt;
    private String shareCode;
}
