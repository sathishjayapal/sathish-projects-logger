package com.sathish.sathishlogger.repository;

import com.sathish.sathishlogger.model.LogEntry;
import com.sathish.sathishlogger.model.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    
    Page<LogEntry> findByApplicationNameOrderByTimestampDesc(String applicationName, Pageable pageable);
    
    Page<LogEntry> findByCorrelationIdOrderByTimestampDesc(String correlationId, Pageable pageable);
    
    Page<LogEntry> findByLogLevelOrderByTimestampDesc(LogLevel logLevel, Pageable pageable);
    
    Page<LogEntry> findByApplicationNameAndLogLevelOrderByTimestampDesc(
            String applicationName, LogLevel logLevel, Pageable pageable);
    
    @Query("SELECT l FROM LogEntry l WHERE l.timestamp BETWEEN :startTime AND :endTime ORDER BY l.timestamp DESC")
    Page<LogEntry> findByTimestampBetween(
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            Pageable pageable);
    
    @Query("SELECT l FROM LogEntry l WHERE l.applicationName = :appName AND l.timestamp BETWEEN :startTime AND :endTime ORDER BY l.timestamp DESC")
    Page<LogEntry> findByApplicationNameAndTimestampBetween(
            @Param("appName") String applicationName,
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            Pageable pageable);
    
    @Query("SELECT DISTINCT l.applicationName FROM LogEntry l ORDER BY l.applicationName")
    List<String> findDistinctApplicationNames();
    
    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.applicationName = :appName AND l.logLevel = :level")
    Long countByApplicationNameAndLogLevel(@Param("appName") String applicationName, @Param("level") LogLevel logLevel);
}
