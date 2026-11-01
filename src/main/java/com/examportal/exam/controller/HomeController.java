package com.examportal.exam.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @Hidden
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "Exam Management Microservice (MS2)",
                "status", "UP",
                "swaggerUi", "https://ms2-w51n.onrender.com/swagger-ui.html",
                "apiDocs", "https://ms2-w51n.onrender.com/v3/api-docs"
        ));
    }

    @Hidden
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
