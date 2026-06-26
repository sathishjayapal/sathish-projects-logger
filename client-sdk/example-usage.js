/**
 * Node.js/JavaScript client example for SathishLogger
 * Simple HTTP client to send logs to SathishLogger service
 */

const axios = require('axios');
const { v4: uuidv4 } = require('uuid');

class SathishLoggerClient {
    constructor(baseUrl, applicationName, apiKey = null) {
        this.baseUrl = baseUrl.replace(/\/$/, '');
        this.applicationName = applicationName;
        this.apiKey = apiKey;
        
        this.client = axios.create({
            timeout: 5000,
            headers: {
                'Content-Type': 'application/json',
                ...(apiKey && { 'X-API-Key': apiKey })
            }
        });
    }
    
    async info(message, correlationId = null, metadata = {}) {
        return this._log('INFO', message, correlationId, metadata);
    }
    
    async warn(message, correlationId = null, metadata = {}) {
        return this._log('WARN', message, correlationId, metadata);
    }
    
    async error(message, correlationId = null, error = null, metadata = {}) {
        return this._log('ERROR', message, correlationId, metadata, error);
    }
    
    async debug(message, correlationId = null, metadata = {}) {
        return this._log('DEBUG', message, correlationId, metadata);
    }
    
    async _log(level, message, correlationId = null, metadata = {}, error = null) {
        try {
            const logData = {
                applicationName: this.applicationName,
                logLevel: level,
                message: message,
                correlationId: correlationId || uuidv4(),
                timestamp: new Date().toISOString(),
                metadata: metadata
            };
            
            if (error) {
                logData.exceptionMessage = error.message;
                logData.stackTrace = error.stack;
            }
            
            const response = await this.client.post(`${this.baseUrl}/api/logs/log`, logData);
            return response.data;
            
        } catch (err) {
            // Fallback to console logging
            console.error(`Failed to send log to SathishLogger: ${err.message}`);
            console.log(`[${this.applicationName}] [${correlationId || 'N/A'}] [${level}] ${message}`);
        }
    }
    
    // Batch logging
    async logBatch(logs) {
        try {
            const response = await this.client.post(`${this.baseUrl}/api/logs/batch`, logs);
            return response.data;
        } catch (err) {
            console.error(`Failed to send batch logs to SathishLogger: ${err.message}`);
        }
    }
}

// Usage Example
async function main() {
    // Initialize logger
    const logger = new SathishLoggerClient('http://localhost:8080', 'nodejs-app');
    
    // Simple logging
    await logger.info('Node.js application started');
    await logger.warn('This is a warning message');
    
    // With correlation ID and metadata
    const correlationId = uuidv4();
    const metadata = { userId: '12345', requestPath: '/api/users' };
    await logger.info('Processing user request', correlationId, metadata);
    
    // Error logging with exception
    try {
        throw new Error('Something went wrong');
    } catch (error) {
        await logger.error('An error occurred', correlationId, error);
    }
    
    // Batch logging
    const batchLogs = [
        {
            applicationName: 'nodejs-app',
            logLevel: 'INFO',
            message: 'Batch log 1',
            correlationId: uuidv4(),
            timestamp: new Date().toISOString()
        },
        {
            applicationName: 'nodejs-app',
            logLevel: 'INFO',
            message: 'Batch log 2',
            correlationId: uuidv4(),
            timestamp: new Date().toISOString()
        }
    ];
    
    await logger.logBatch(batchLogs);
    
    console.log('Logs sent to SathishLogger service!');
}

// Run if this file is executed directly
if (require.main === module) {
    main().catch(console.error);
}

module.exports = SathishLoggerClient;
