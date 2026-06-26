-- Initialize SathishLogger database
-- This script runs when PostgreSQL container starts for the first time

-- Create database if it doesn't exist (PostgreSQL creates it automatically from POSTGRES_DB)
-- But we can add any initial setup here

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_log_entries_app_name ON log_entries(application_name);
CREATE INDEX IF NOT EXISTS idx_log_entries_correlation_id ON log_entries(correlation_id);
CREATE INDEX IF NOT EXISTS idx_log_entries_log_level ON log_entries(log_level);
CREATE INDEX IF NOT EXISTS idx_log_entries_timestamp ON log_entries(timestamp);
CREATE INDEX IF NOT EXISTS idx_log_entries_app_timestamp ON log_entries(application_name, timestamp);

-- Optional: Create a view for recent error logs
-- CREATE OR REPLACE VIEW recent_errors AS
-- SELECT * FROM log_entries 
-- WHERE log_level IN ('ERROR', 'FATAL') 
-- AND timestamp > NOW() - INTERVAL '24 hours'
-- ORDER BY timestamp DESC;
