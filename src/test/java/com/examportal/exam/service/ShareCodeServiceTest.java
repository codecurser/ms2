package com.examportal.exam.service;

import com.examportal.exam.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareCodeServiceTest {

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private ShareCodeService shareCodeService;

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Test
    @DisplayName("Should generate code of length 8 containing only allowed characters")
    void generateRandomCode_ValidFormat() {
        String code = shareCodeService.generateRandomCode();
        assertNotNull(code);
        assertEquals(8, code.length());
        for (char c : code.toCharArray()) {
            assertTrue(ALLOWED_CHARS.indexOf(c) >= 0, "Character '" + c + "' is not in allowed alphabet");
        }
    }

    @Test
    @DisplayName("Should generate unique share code when repository check returns false")
    void generateUniqueShareCode_Success() {
        when(examRepository.existsByShareCode(anyString())).thenReturn(false);

        String code = shareCodeService.generateUniqueShareCode();

        assertNotNull(code);
        assertEquals(8, code.length());
        verify(examRepository, times(1)).existsByShareCode(code);
    }

    @Test
    @DisplayName("Should retry when collision occurs and succeed on second attempt")
    void generateUniqueShareCode_CollisionRetrySuccess() {
        when(examRepository.existsByShareCode(anyString()))
                .thenReturn(true)  // First attempt collides
                .thenReturn(false); // Second attempt is unique

        String code = shareCodeService.generateUniqueShareCode();

        assertNotNull(code);
        assertEquals(8, code.length());
        verify(examRepository, times(2)).existsByShareCode(anyString());
    }
}
