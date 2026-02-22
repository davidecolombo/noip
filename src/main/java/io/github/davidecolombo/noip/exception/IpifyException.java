package io.github.davidecolombo.noip.exception;

/**
 * Exception for IPify API related errors.
 * Used when there are failures retrieving the current IP address from Ipify service.
 */
public class IpifyException extends NoIpException {
    
    public IpifyException(String message) {
        super(message);
    }
    
    public IpifyException(String message, Throwable cause) {
        super(message, cause);
    }
}