package io.github.davidecolombo.noip;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.exception.NoIpException;
import io.github.davidecolombo.noip.noip.NoIpUpdater;
import io.github.davidecolombo.noip.utils.CryptoUtils;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.IOException;

@Slf4j
public class App {

	@Option(name = "-settings", aliases = {"-s"}, usage = "Path to settings.json file")
	private String fileName;

	@Option(name = "-encrypt", aliases = {"-e"}, usage = "Encrypt a password value")
	private String encryptValue;

	@Option(name = "-decrypt", aliases = {"-d"}, usage = "Decrypt an encrypted value")
	private String decryptValue;

	@Option(name = "-encrypt-key", aliases = {"-k"}, usage = "Encryption/decryption key")
	private String encryptKey;

	private static class SingletonHolder {
		public static final App instance = new App();
	}

	public static App getInstance() {
		return SingletonHolder.instance;
	}

	private App() {}

	public Integer update(String[] args) {
		int status = NoIpUpdater.ERROR_RETURN_CODE;
		try {
			logger.debug("Parsing command line arguments: {}", args != null ? String.join(" ", args) : "null");
			new CmdLineParser(this).parseArgument(args);

			if (encryptValue != null) {
				return handleEncrypt();
			}

			if (decryptValue != null) {
				return handleDecrypt();
			}

			if (fileName == null) {
				logger.error("Missing -settings argument");
				logger.error("Usage: -settings <path_to_settings.json>");
				logger.error("Or use -encrypt/-decrypt for encryption operations");
				return ERROR_MISSING_ARGS;
			}

			logger.info("Starting No-IP update process with settings file: {}", fileName);
			status = NoIpUpdater.updateFromIpify(fileName);
			
			if (status == 0) {
				logger.info("No-IP update completed successfully");
			} else {
				logger.warn("No-IP update completed with status code: {}", status);
			}
			
		} catch (CmdLineException e) {
			logger.error("Command line error: {}", e.getMessage());
			printUsage();
		} catch (ConfigurationException e) {
			logger.error("Configuration error: {}. Please check your settings file.", e.getMessage());
		} catch (NoIpException e) {
			logger.error("No-IP service error: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("I/O error accessing settings or external services: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error: {}", e.getMessage(), e);
		}
		
		logger.info("Application exiting with status code: {}", status);
		return status;
	}

	private Integer handleEncrypt() {
		String key = getEncryptionKey();
		if (key == null) {
			logger.error("Encryption key is required. Use -encrypt-key <key> or set NOIP_ENCRYPT_KEY environment variable");
			return ERROR_MISSING_ARGS;
		}
		String encrypted = CryptoUtils.encrypt(encryptValue, key);
		System.out.println(encrypted);
		logger.info("Password encrypted successfully");
		return 0;
	}

	private Integer handleDecrypt() {
		String key = getEncryptionKey();
		if (key == null) {
			logger.error("Encryption key is required. Use -encrypt-key <key> or set NOIP_ENCRYPT_KEY environment variable");
			return ERROR_MISSING_ARGS;
		}
		try {
			String decrypted = CryptoUtils.decrypt(decryptValue, key);
			System.out.println(decrypted);
			return 0;
		} catch (Exception e) {
			logger.error("Decryption failed: {}", e.getMessage());
			return ERROR_DECRYPT;
		}
	}

	private String getEncryptionKey() {
		if (encryptKey != null && !encryptKey.isEmpty()) {
			return encryptKey;
		}
		return CryptoUtils.getEncryptionKey();
	}

	private void printUsage() {
		logger.error("Usage:");
		logger.error("  Update:  -settings <path_to_settings.json>");
		logger.error("  Encrypt: -encrypt <password> -encrypt-key <key>");
		logger.error("  Decrypt: -decrypt <encrypted_value> -encrypt-key <key>");
		logger.error("");
		logger.error("Environment variables:");
		logger.error("  NOIP_ENCRYPT_KEY - encryption/decryption key");
		logger.error("  noip.encrypt.key - encryption/decryption key (system property)");
	}

	private static final int ERROR_RETURN_CODE = -1;
	private static final int ERROR_MISSING_ARGS = 2;
	private static final int ERROR_DECRYPT = 3;

	/*
	 * Usage: -settings src/test/resources/settings.json
	 *   or: -encrypt mypassword -encrypt-key mysecretkey
	 *   or: -decrypt "ENC(...)" -encrypt-key mysecretkey
	 */
	public static void main(String[] args) {
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
		System.exit(App.getInstance().update(args));
	}
}
