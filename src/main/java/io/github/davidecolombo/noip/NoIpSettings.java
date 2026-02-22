package io.github.davidecolombo.noip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.NoIpResponse;
import io.github.davidecolombo.noip.utils.CryptoUtils;

import java.util.List;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"userName",
	"password",
	"hostName",
	"userAgent",
	"responses"
})
public class NoIpSettings {

	@JsonProperty("userName") private String userName;
	@JsonProperty("password") private String password;
	@JsonProperty("hostName") private String hostName;
	@JsonProperty("userAgent") private String userAgent;
	@JsonProperty("responses") private List<NoIpResponse> responses;

	// User-Agent format pattern: Name/Version contact@domain.com
	private static final Pattern USER_AGENT_PATTERN = 
			Pattern.compile("^[^/]+/[^\\s]+ [^@]+@[^\\s]+$");

	/**
	 * Validates the configuration settings.
	 * @throws ConfigurationException if any configuration is invalid
	 */
	public void validate() throws ConfigurationException {
		
		if (userName == null || userName.trim().isEmpty()) {
			throw new ConfigurationException("userName is required and cannot be empty");
		}
		
		if (password == null || password.trim().isEmpty()) {
			throw new ConfigurationException("password is required and cannot be empty");
		}
		
		if (hostName == null || hostName.trim().isEmpty()) {
			throw new ConfigurationException("hostName is required and cannot be empty");
		}
		
		if (userAgent == null || userAgent.trim().isEmpty()) {
			throw new ConfigurationException("userAgent is required and cannot be empty");
		}
		
		// Validate User-Agent format (should follow No-IP guidelines)
		if (!USER_AGENT_PATTERN.matcher(userAgent.trim()).matches()) {
			throw new ConfigurationException(
				String.format("userAgent '%s' is invalid. Expected format: 'Name/Version contact@domain.com'", 
				userAgent)
			);
		}
		
		if (responses == null || responses.isEmpty()) {
			throw new ConfigurationException("responses list cannot be null or empty");
		}
	}

	public String getEffectivePassword() {
		if (CryptoUtils.isEncrypted(password)) {
			return CryptoUtils.decryptValue(password);
		}
		return password;
	}
}