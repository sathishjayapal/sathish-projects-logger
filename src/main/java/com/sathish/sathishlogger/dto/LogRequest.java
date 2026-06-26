package com.sathish.sathishlogger.dto;

import com.sathish.sathishlogger.model.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

public class LogRequest {
    
    @NotBlank(message = "Application name is required")
    private String applicationName;
    
    private String correlationId;
    
    @NotNull(message = "Log level is required")
    private LogLevel logLevel;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String loggerName;
    private String threadName;
    private String exceptionMessage;
    private String stackTrace;
    private LocalDateTime timestamp;
    private Map<String, String> metadata;
    
    // Constructors
    public LogRequest() {}
    
    public LogRequest(String applicationName, LogLevel logLevel, String message) {
        this.applicationName = applicationName;
        this.logLevel = logLevel;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public LogLevel getLogLevel() { return logLevel; }
    public void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getLoggerName() { return loggerName; }
    public void setLoggerName(String loggerName) { this.loggerName = loggerName; }
    
    public String getThreadName() { return threadName; }
    public void setThreadName(String threadName) { this.threadName = threadName; }
    
    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
    
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
