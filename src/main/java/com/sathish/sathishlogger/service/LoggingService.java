package com.sathish.sathishlogger.service;

import com.sathish.sathishlogger.dto.LogRequest;
import com.sathish.sathishlogger.model.LogEntry;
import com.sathish.sathishlogger.model.LogLevel;
import com.sathish.sathishlogger.repository.LogEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    private final LogEntryRepository logEntryRepository;

    public LoggingService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public LogEntry saveLog(LogRequest logRequest) {
        LogEntry logEntry = new LogEntry();
        logEntry.setApplicationName(logRequest.getApplicationName());
        logEntry.setCorrelationId(logRequest.getCorrelationId() != null ? 
                logRequest.getCorrelationId() : generateCorrelationId());
        logEntry.setLogLevel(logRequest.getLogLevel());
        logEntry.setMessage(logRequest.getMessage());
        logEntry.setLoggerName(logRequest.getLoggerName());
        logEntry.setThreadName(logRequest.getThreadName());
        logEntry.setExceptionMessage(logRequest.getExceptionMessage());
        logEntry.setStackTrace(logRequest.getStackTrace());
        logEntry.setTimestamp(logRequest.getTimestamp() != null ? 
                logRequest.getTimestamp() : LocalDateTime.now());
        logEntry.setMetadata(logRequest.getMetadata());
        
        LogEntry savedEntry = logEntryRepository.save(logEntry);
        
        // Also log to console for immediate visibility
        logToConsole(logRequest);
        
        return savedEntry;
    }
    
    public Page<LogEntry> getLogsByApplication(String applicationName, Pageable pageable) {
        return logEntryRepository.findByApplicationNameOrderByTimestampDesc(applicationName, pageable);
    }
    
    public Page<LogEntry> getLogsByCorrelationId(String correlationId, Pageable pageable) {
        return logEntryRepository.findByCorrelationIdOrderByTimestampDesc(correlationId, pageable);
    }
    
    public Page<LogEntry> getLogsByLevel(LogLevel logLevel, Pageable pageable) {
        return logEntryRepository.findByLogLevelOrderByTimestampDesc(logLevel, pageable);
    }
    
    public Page<LogEntry> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return logEntryRepository.findByTimestampBetween(startTime, endTime, pageable);
    }
    
    public Page<LogEntry> getLogsByApplicationAndTimeRange(String applicationName, 
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return logEntryRepository.findByApplicationNameAndTimestampBetween(
                applicationName, startTime, endTime, pageable);
    }
    
    public List<String> getApplicationNames() {
        return logEntryRepository.findDistinctApplicationNames();
    }
    
    public Long getLogCountByApplicationAndLevel(String applicationName, LogLevel logLevel) {
        return logEntryRepository.countByApplicationNameAndLogLevel(applicationName, logLevel);
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    private void logToConsole(LogRequest logRequest) {
        String message = String.format("[%s] [%s] [%s] %s", 
                logRequest.getApplicationName(),
                logRequest.getCorrelationId(),
                logRequest.getLogLevel(),
                logRequest.getMessage());
        
        switch (logRequest.getLogLevel()) {
            case TRACE -> logger.trace(message);
            case DEBUG -> logger.debug(message);
            case INFO -> logger.info(message);
            case WARN -> logger.warn(message);
            case ERROR, FATAL -> logger.error(message);
        }
    }
}
