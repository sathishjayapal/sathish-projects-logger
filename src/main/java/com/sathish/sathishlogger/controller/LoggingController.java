package com.sathish.sathishlogger.controller;

import com.sathish.sathishlogger.dto.LogRequest;
import com.sathish.sathishlogger.model.LogEntry;
import com.sathish.sathishlogger.model.LogLevel;
import com.sathish.sathishlogger.service.LoggingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LoggingController {

    private final LoggingService loggingService;

    public LoggingController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logMessage(@Valid @RequestBody LogRequest logRequest) {
        LogEntry savedEntry = loggingService.saveLog(logRequest);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "logId", savedEntry.getId(),
                "correlationId", savedEntry.getCorrelationId(),
                "timestamp", savedEntry.getTimestamp()
        ));
    }
    
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> logBatch(@Valid @RequestBody List<LogRequest> logRequests) {
        int successCount = 0;
        for (LogRequest logRequest : logRequests) {
            try {
                loggingService.saveLog(logRequest);
                successCount++;
            } catch (Exception e) {
                // Continue processing other logs even if one fails
            }
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "totalRequests", logRequests.size(),
                "successfulLogs", successCount
        ));
    }
    
    @GetMapping("/application/{applicationName}")
    public ResponseEntity<Page<LogEntry>> getLogsByApplication(
            @PathVariable String applicationName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs = loggingService.getLogsByApplication(applicationName, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<Page<LogEntry>> getLogsByCorrelationId(
            @PathVariable String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs = loggingService.getLogsByCorrelationId(correlationId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/level/{logLevel}")
    public ResponseEntity<Page<LogEntry>> getLogsByLevel(
            @PathVariable LogLevel logLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs = loggingService.getLogsByLevel(logLevel, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<LogEntry>> searchLogs(
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs;
        
        if (applicationName != null && startTime != null && endTime != null) {
            logs = loggingService.getLogsByApplicationAndTimeRange(applicationName, startTime, endTime, pageable);
        } else if (startTime != null && endTime != null) {
            logs = loggingService.getLogsByTimeRange(startTime, endTime, pageable);
        } else if (applicationName != null) {
            logs = loggingService.getLogsByApplication(applicationName, pageable);
        } else {
            // Return empty page if no valid search criteria
            logs = Page.empty(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/applications")
    public ResponseEntity<List<String>> getApplicationNames() {
        List<String> applications = loggingService.getApplicationNames();
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/stats/{applicationName}")
    public ResponseEntity<Map<String, Long>> getApplicationStats(@PathVariable String applicationName) {
        Map<String, Long> stats = Map.of(
                "ERROR", loggingService.getLogCountByApplicationAndLevel(applicationName, LogLevel.ERROR),
                "WARN", loggingService.getLogCountByApplicationAndLevel(applicationName, LogLevel.WARN),
                "INFO", loggingService.getLogCountByApplicationAndLevel(applicationName, LogLevel.INFO),
                "DEBUG", loggingService.getLogCountByApplicationAndLevel(applicationName, LogLevel.DEBUG)
        );
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "SathishLogger"));
    }
}
