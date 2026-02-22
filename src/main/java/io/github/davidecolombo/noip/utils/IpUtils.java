package io.github.davidecolombo.noip.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class IpUtils {

	private static final Pattern IPV4_PATTERN =
			Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	public static boolean isIPv4Address(final String ip) {
		return ip != null && IPV4_PATTERN.matcher(ip).matches();
	}
}