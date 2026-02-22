package io.github.davidecolombo.noip.exception;

/**
 * Exception for configuration-related errors.
 * Used when settings are missing, invalid, or malformed.
 */
public class ConfigurationException extends NoIpException {
    
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}