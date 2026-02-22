package io.github.davidecolombo.noip.utils;

import lombok.experimental.UtilityClass;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.util.text.AES256TextEncryptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class CryptoUtils {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";
    private static final Pattern ENC_PATTERN = Pattern.compile("^ENC\\((.+)\\)$");

    private static final String ENV_VAR_KEY = "NOIP_ENCRYPT_KEY";
    private static final String SYSTEM_PROP_KEY = "noip.encrypt.key";

    private static final String DEFAULT_ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";

    public static String encrypt(String plaintext, String password) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        StandardPBEStringEncryptor encryptor = createEncryptor(password);
        String encrypted = encryptor.encrypt(plaintext);
        return ENC_PREFIX + encrypted + ENC_SUFFIX;
    }

    public static String decrypt(String encrypted, String password) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }
        String value = encrypted.trim();
        if (!isEncrypted(value)) {
            return value;
        }
        String encryptedValue = extractEncryptedValue(value);
        StandardPBEStringEncryptor encryptor = createEncryptor(password);
        return encryptor.decrypt(encryptedValue);
    }

    public static boolean isEncrypted(String value) {
        if (value == null) {
            return false;
        }
        return ENC_PATTERN.matcher(value.trim()).matches();
    }

    private static String extractEncryptedValue(String value) {
        Matcher matcher = ENC_PATTERN.matcher(value.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid ENC() format: " + value);
    }

    private static StandardPBEStringEncryptor createEncryptor(String password) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(DEFAULT_ALGORITHM);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());
        config.setStringOutputType("base64");

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }

    public static String getEncryptionKey() {
        String key = System.getenv(ENV_VAR_KEY);
        if (key != null && !key.isEmpty()) {
            return key;
        }
        key = System.getProperty(SYSTEM_PROP_KEY);
        if (key != null && !key.isEmpty()) {
            return key;
        }
        return null;
    }

    public static boolean hasEncryptionKey() {
        return getEncryptionKey() != null;
    }

    public static String decryptValue(String value) {
        String key = getEncryptionKey();
        if (key == null) {
            throw new IllegalStateException(
                "Encryption key not found. Set " + ENV_VAR_KEY + " environment variable " +
                "or " + SYSTEM_PROP_KEY + " system property."
            );
        }
        return decrypt(value, key);
    }
}
