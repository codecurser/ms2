package com.examportal.exam.exception;

public class UnauthorizedExamAccessException extends RuntimeException {
    public UnauthorizedExamAccessException(String message) {
        super(message);
    }
}
