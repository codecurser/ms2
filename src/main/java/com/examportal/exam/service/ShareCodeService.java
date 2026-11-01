package com.examportal.exam.service;

import com.examportal.exam.exception.ShareCodeGenerationException;
import com.examportal.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareCodeService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 10;

    private final ExamRepository examRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateUniqueShareCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = generateRandomCode();
            if (!examRepository.existsByShareCode(code)) {
                return code;
            }
            log.warn("Share code collision detected for code '{}', retrying...", code);
        }
        throw new ShareCodeGenerationException("Failed to generate a unique share code after multiple attempts");
    }

    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(randomIndex));
        }
        return sb.toString();
    }
}
