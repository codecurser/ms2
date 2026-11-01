package com.examportal.exam.exception;

public class InvalidExamStateException extends RuntimeException {
    public InvalidExamStateException(String message) {
        super(message);
    }
}
