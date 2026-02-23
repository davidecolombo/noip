package io.github.davidecolombo.noip;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.NoIpResponse;
import io.github.davidecolombo.noip.utils.CryptoUtils;

import java.util.List;
import java.util.regex.Pattern;

@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"userName",
	"password",
	"hostName",
	"userAgent",
	"ipProtocol",
	"responses"
})
public class NoIpSettings {

	public enum IpProtocol {
		IPV4("ipv4"),
		IPV6("ipv6"),
		DUAL("dual");

		private final String value;

		IpProtocol(String value) {
			this.value = value;
		}

		@JsonValue
		public String getValue() {
			return value;
		}

		@JsonCreator
		public static IpProtocol fromValue(String value) {
			if (value == null) {
				return DUAL;
			}
			for (IpProtocol protocol : IpProtocol.values()) {
				if (protocol.value.equalsIgnoreCase(value)) {
					return protocol;
				}
			}
			return DUAL;
		}
	}

	@JsonProperty("userName") private String userName;
	@JsonProperty("password") private String password;
	@JsonProperty("hostName") private String hostName;
	@JsonProperty("userAgent") private String userAgent;
	@JsonProperty("ipProtocol") private IpProtocol ipProtocol;
	@JsonProperty("responses") private List<NoIpResponse> responses;

	private static final Pattern USER_AGENT_PATTERN = 
			Pattern.compile("^[^/]+/[^\\s]+ [^@]+@[^\\s]+$");

	// Environment variables names for configuration overrides
	private static final String ENV_USERNAME = "NOIP_USERNAME";
	private static final String ENV_PASSWORD = "NOIP_PASSWORD";
	private static final String ENV_USER_AGENT = "NOIP_USER_AGENT";
	private static final String ENV_HOSTNAME = "NOIP_HOSTNAME";
	private static final String ENV_IP_PROTOCOL = "NOIP_IP_PROTOCOL";

	// Fallback user-agent when not provided via env var or config file
	private static final String DEFAULT_USER_AGENT = "NoIP-Java/1.0 no-reply@noip.local";

	/**
	 * Gets username. Environment variable takes precedence over config file.
	 * Priority: NOIP_USERNAME env var > config file
	 */
	public String getUserName() {
		String envValue = System.getenv(ENV_USERNAME);
		return envValue != null && !envValue.isEmpty() ? envValue : this.userName;
	}

	/**
	 * Gets password. Environment variable takes precedence over config file.
	 * If password starts with ENC(...), it will be decrypted using the encryptor key.
	 * Priority: NOIP_PASSWORD env var > encrypted config file > plaintext config file
	 */
	public String getPassword() throws ConfigurationException {
		String envValue = System.getenv(ENV_PASSWORD);
		if (envValue != null && !envValue.isEmpty()) {
			if (CryptoUtils.isEncrypted(envValue)) {
				try {
					return CryptoUtils.decryptValue(envValue);
				} catch (Exception e) {
					throw new ConfigurationException("Failed to decrypt password: invalid encryption key or corrupted encrypted value", e);
				}
			}
			return envValue;
		}
		if (CryptoUtils.isEncrypted(password)) {
			try {
				return CryptoUtils.decryptValue(password);
			} catch (Exception e) {
				throw new ConfigurationException("Failed to decrypt password: invalid encryption key or corrupted encrypted value", e);
			}
		}
		return password;
	}

	/**
	 * Gets user-agent. Environment variable takes precedence over config file.
	 * Falls back to default if neither is provided.
	 * Priority: NOIP_USER_AGENT env var > config file > default
	 */
	public String getUserAgent() {
		String envValue = System.getenv(ENV_USER_AGENT);
		if (envValue != null && !envValue.isEmpty()) {
			return envValue;
		}
		if (this.userAgent != null && !this.userAgent.isEmpty()) {
			return this.userAgent;
		}
		return DEFAULT_USER_AGENT;
	}

	/**
	 * Gets hostname. Environment variable takes precedence over config file.
	 * Priority: NOIP_HOSTNAME env var > config file
	 */
	public String getHostName() {
		String envValue = System.getenv(ENV_HOSTNAME);
		return envValue != null && !envValue.isEmpty() ? envValue : this.hostName;
	}

	public List<NoIpResponse> getResponses() {
		return responses;
	}

	/**
	 * Gets IP protocol. Environment variable takes precedence over config file.
	 * Falls back to dual if neither is provided.
	 * Priority: NOIP_IP_PROTOCOL env var > config file > dual (default)
	 */
	public IpProtocol getIpProtocol() {
		String envValue = System.getenv(ENV_IP_PROTOCOL);
		String effectiveValue = (envValue != null && !envValue.isEmpty()) ? envValue : (ipProtocol != null ? ipProtocol.getValue() : null);
		return IpProtocol.fromValue(effectiveValue);
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setIpProtocol(IpProtocol ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	public void setResponses(List<NoIpResponse> responses) {
		this.responses = responses;
	}

	public void validate() throws ConfigurationException {
		
		String effectiveUserName = getUserName();
		String effectivePassword = getPassword();
		String effectiveHostName = getHostName();
		String effectiveUserAgent = getUserAgent();
		
		if (effectiveUserName == null || effectiveUserName.trim().isEmpty()) {
			throw new ConfigurationException("userName is required and cannot be empty");
		}
		
		if (effectivePassword == null || effectivePassword.trim().isEmpty()) {
			throw new ConfigurationException("password is required and cannot be empty");
		}
		
		if (effectiveHostName == null || effectiveHostName.trim().isEmpty()) {
			throw new ConfigurationException("hostName is required and cannot be empty");
		}
		
		if (!DEFAULT_USER_AGENT.equals(effectiveUserAgent) 
				&& !USER_AGENT_PATTERN.matcher(effectiveUserAgent.trim()).matches()) {
			throw new ConfigurationException(
				String.format("userAgent '%s' is invalid. Expected format: 'Name/Version contact@domain.com'", 
				effectiveUserAgent)
			);
		}
		
		if (responses == null || responses.isEmpty()) {
			throw new ConfigurationException("responses list cannot be null or empty");
		}
	}
}
