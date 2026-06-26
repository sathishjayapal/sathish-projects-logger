package com.sathish.sathishlogger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "log_entries")
public class LogEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "application_name", nullable = false)
    private String applicationName;
    
    @NotBlank
    @Column(name = "correlation_id")
    private String correlationId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false)
    private LogLevel logLevel;
    
    @NotBlank
    @Column(name = "message", nullable = false, length = 4000)
    private String message;
    
    @Column(name = "logger_name")
    private String loggerName;
    
    @Column(name = "thread_name")
    private String threadName;
    
    @Column(name = "exception_message", length = 2000)
    private String exceptionMessage;
    
    @Column(name = "stack_trace", length = 8000)
    private String stackTrace;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @ElementCollection
    @CollectionTable(name = "log_entry_metadata", joinColumns = @JoinColumn(name = "log_entry_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    // Constructors
    public LogEntry() {
        this.timestamp = LocalDateTime.now();
    }
    
    public LogEntry(String applicationName, String correlationId, LogLevel logLevel, String message) {
        this();
        this.applicationName = applicationName;
        this.correlationId = correlationId;
        this.logLevel = logLevel;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
