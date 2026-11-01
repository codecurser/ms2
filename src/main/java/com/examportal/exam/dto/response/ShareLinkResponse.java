package com.examportal.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLinkResponse {

    private UUID examId;
    private String shareCode;
    private String shareUrl;
}
