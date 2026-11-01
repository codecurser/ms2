package com.examportal.exam.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String error;
    private String message;
    private Instant timestamp;
    private String path;
    private List<ValidationError> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
    }
}
