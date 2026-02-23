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

	public static boolean isIPv6Address(final String ip) {
		if (ip == null) {
			return false;
		}
		String ipToCheck = ip;
		int zoneIdIndex = ipToCheck.indexOf('%');
		if (zoneIdIndex > 0) {
			ipToCheck = ipToCheck.substring(0, zoneIdIndex);
		}
		try {
			java.net.InetAddress address = java.net.InetAddress.getByName(ipToCheck);
			return address instanceof java.net.Inet6Address;
		} catch (java.net.UnknownHostException e) {
			return false;
		}
	}
}