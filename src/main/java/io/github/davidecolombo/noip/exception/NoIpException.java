package io.github.davidecolombo.noip.exception;

/**
 * Base exception for No-IP related errors.
 * Provides common functionality for all No-IP specific exceptions.
 */
public class NoIpException extends RuntimeException {
    
    public NoIpException(String message) {
        super(message);
    }
    
    public NoIpException(String message, Throwable cause) {
        super(message, cause);
    }
}