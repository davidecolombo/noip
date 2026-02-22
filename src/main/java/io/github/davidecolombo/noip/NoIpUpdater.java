package io.github.davidecolombo.noip.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Response;
import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.exception.IpifyException;
import io.github.davidecolombo.noip.exception.NoIpException;
import io.github.davidecolombo.noip.ipify.IpifyResponse;
import io.github.davidecolombo.noip.utils.IpUtils;
import io.github.davidecolombo.noip.utils.ObjectMapperUtils;
import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.NoIpResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class NoIpUpdater {
    
    private NoIpUpdater() {
        // Utility class - prevent instantiation
    }

    public static final int ERROR_RETURN_CODE = -1;

    /*
     * IPIFY is a simple public IP address API
     */
    private static final String IPIFY_URL = "https://api.ipify.org/?format=json";

    /*
     * Used to load No-IP responses
     */
    private static final String RESPONSES_FILE = "responses.json";

    /*
     * Setup a response for unknown status
     */
    private static final NoIpResponse UNKNOWN_RESPONSE = NoIpResponse.builder()
            .successful(false)
            .exitCode(ERROR_RETURN_CODE)
            .build();

    /**
     * Automatically updates the DNS at No-IP whenever it changes
     *
     * @param settings No-IP settings
     * @param ip           IP address that must be set
     * @return return code mapped to the received response
     * @throws IOException I/O exception has occurred
     */
    private static Integer doUpdate(@NonNull NoIpSettings settings, @NonNull String ip) throws IOException {
        
        // Validate input parameters
        if (ip == null) {
            throw new IllegalArgumentException("IP address cannot be null");
        }
        if (!IpUtils.isIPv4Address(ip)) {
            throw new IllegalArgumentException("IP '" + ip + "' is not a valid IPv4 address");
        }

        logger.info("Updating No-IP hostname '{}' to IP address '{}'", settings.getHostName(), ip);

        // Build API and synchronously update No-IP
        long startTime = System.currentTimeMillis();
        try {
            INoIpApi api = NoIpApiImpl.create(
                    settings.getUserName(),
                    settings.getEffectivePassword(),
                    settings.getUserAgent()
            ).create(INoIpApi.class);
            
            Response<String> response = api.update(
                    settings.getHostName(), ip
            ).execute();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("No-IP API request completed in {}ms - HTTP status: {} {}", 
                       duration, response.code(), response.message());

            // Process No-IP response
            String message = null;
            if (response.isSuccessful()) {
                message = response.body();
            } else if (response.errorBody() != null) {
                message = response.errorBody().string();
            }
            
            if (StringUtils.isEmpty(message)) {
                throw new NoIpException("No-IP response is empty for hostname '" + settings.getHostName() + "'");
            }
            
            message = message.trim();
            logger.info("No-IP response for hostname '{}': {}", settings.getHostName(), message);

            // Match No-IP string with known responses (use regular stream, not parallel)
            String status = message.split(" ")[0];
            Integer exitCode = settings.getResponses().stream()
                    .filter(item -> status.equals(item.getStatus())).findAny()
                    .orElse(UNKNOWN_RESPONSE)
                    .getExitCode();
                    
            logger.debug("Mapped No-IP status '{}' to exit code: {}", status, exitCode);
            return exitCode;
            
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("No-IP API request failed after {}ms: {}", duration, e.getMessage());
            throw new NoIpException("Failed to update No-IP hostname '" + settings.getHostName() + "'", e);
        }
    }

    public static Integer update(NoIpSettings settings, String ip) {
        try {
            return doUpdate(settings, ip);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for No-IP update: {}", e.getMessage());
            throw e; // Don't wrap validation errors
        } catch (IOException e) {
            logger.error("Network error during No-IP update: {}", e.getMessage());
            throw new NoIpException("Network error during No-IP update", e);
        } catch (Exception e) {
            logger.error("Unexpected error during No-IP update: {}", e.getMessage());
            throw new NoIpException("Unexpected error during No-IP update", e);
        }
    }

    public static Integer updateFromIpify(@NonNull String fileName) throws IOException {
        
        logger.debug("Loading No-IP settings from file: {}", fileName);
        
        // Build settings
        ObjectMapper objectMapper = ObjectMapperUtils.createObjectMapper();
        NoIpSettings noIpSettings;
        try {
            noIpSettings = objectMapper.readValue(new File(fileName), NoIpSettings.class);
            objectMapper.readerForUpdating(noIpSettings).readValue(NoIpUpdater.class.getClassLoader().getResource(RESPONSES_FILE));
            
            // Validate configuration
            noIpSettings.validate();
            logger.info("No-IP configuration loaded and validated successfully");
            
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load or parse settings file: " + fileName, e);
        }

        // Get Ipify response
        logger.info("Retrieving current IP address from Ipify API");
        IpifyResponse ipifyResponse;
        long startTime = System.currentTimeMillis();
        try {
            ipifyResponse = objectMapper.readValue(new URL(IPIFY_URL), IpifyResponse.class);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Retrieved IP address '{}' from Ipify in {}ms", ipifyResponse.getIp(), duration);
            
            if (ipifyResponse.getIp() == null || ipifyResponse.getIp().trim().isEmpty()) {
                throw new IpifyException("Ipify returned null or empty IP address");
            }
            
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to retrieve IP from Ipify after {}ms: {}", duration, e.getMessage());
            throw new IpifyException("Failed to retrieve IP address from Ipify service", e);
        }

        // Update DNS at No-IP
        return update(noIpSettings, ipifyResponse.getIp());
    }
}