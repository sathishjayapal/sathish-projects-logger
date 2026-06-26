#!/usr/bin/env python3
"""
Python client example for SathishLogger
Simple HTTP client to send logs to SathishLogger service
"""

import json
import requests
import uuid
from datetime import datetime
from typing import Optional, Dict, Any
import traceback

class SathishLoggerClient:
    def __init__(self, base_url: str, application_name: str, api_key: Optional[str] = None):
        self.base_url = base_url.rstrip('/')
        self.application_name = application_name
        self.api_key = api_key
        self.session = requests.Session()
        if api_key:
            self.session.headers.update({'X-API-Key': api_key})
    
    def info(self, message: str, correlation_id: Optional[str] = None, metadata: Optional[Dict[str, str]] = None):
        self._log('INFO', message, correlation_id, metadata)
    
    def warn(self, message: str, correlation_id: Optional[str] = None, metadata: Optional[Dict[str, str]] = None):
        self._log('WARN', message, correlation_id, metadata)
    
    def error(self, message: str, correlation_id: Optional[str] = None, exception: Optional[Exception] = None, metadata: Optional[Dict[str, str]] = None):
        self._log('ERROR', message, correlation_id, metadata, exception)
    
    def debug(self, message: str, correlation_id: Optional[str] = None, metadata: Optional[Dict[str, str]] = None):
        self._log('DEBUG', message, correlation_id, metadata)
    
    def _log(self, level: str, message: str, correlation_id: Optional[str] = None, 
             metadata: Optional[Dict[str, str]] = None, exception: Optional[Exception] = None):
        try:
            log_data = {
                'applicationName': self.application_name,
                'logLevel': level,
                'message': message,
                'correlationId': correlation_id or str(uuid.uuid4()),
                'timestamp': datetime.now().isoformat(),
                'metadata': metadata or {}
            }
            
            if exception:
                log_data['exceptionMessage'] = str(exception)
                log_data['stackTrace'] = traceback.format_exc()
            
            response = self.session.post(
                f'{self.base_url}/api/logs/log',
                json=log_data,
                timeout=5
            )
            response.raise_for_status()
            
        except Exception as e:
            # Fallback to console logging
            print(f"Failed to send log to SathishLogger: {e}")
            print(f"[{self.application_name}] [{correlation_id or 'N/A'}] [{level}] {message}")

# Usage Example
if __name__ == "__main__":
    # Initialize logger
    logger = SathishLoggerClient("http://localhost:8080", "python-app")
    
    # Simple logging
    logger.info("Python application started")
    logger.warn("This is a warning message")
    
    # With correlation ID and metadata
    correlation_id = str(uuid.uuid4())
    metadata = {"user_id": "12345", "request_path": "/api/users"}
    logger.info("Processing user request", correlation_id, metadata)
    
    # Error logging with exception
    try:
        raise ValueError("Something went wrong")
    except Exception as e:
        logger.error("An error occurred", correlation_id, e)
    
    print("Logs sent to SathishLogger service!")
