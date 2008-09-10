package net.roarsoftware.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitiy class with methods to calculate an md5 hash and to encode URLs.
 *
 * @author Janni Kovacs
 */
public class StringUtilities {

	private static MessageDigest digest;
	private static String MBID_REGEX = "\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}";

	static {
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// better never happens
		}
	}

	public static String md5(String s) {
		try {
			byte[] bytes = digest.digest(s.getBytes("UTF-8"));
			StringBuilder b = new StringBuilder(32);
			for (byte aByte : bytes) {
				String hex = Integer.toHexString((int) aByte & 0xFF);
				if (hex.length() == 1)
					b.append('0');
				b.append(hex);
			}
			return b.toString();
		} catch (UnsupportedEncodingException e) {
			// utf-8 always available
		}
		return null;
	}

	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// utf-8 always available
		}
		return null;
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// utf-8 always available
		}
		return null;
	}

	public static boolean isMbid(String artistOrMbid) {
		// example: bfcc6d75-a6a5-4bc6-8282-47aec8531818
		return artistOrMbid.length() == 36 && artistOrMbid.matches(MBID_REGEX);
	}

	/**
	 * Creates a Map out of an array with Strings.
	 *
	 * @param strings input strings, key-value alternating
	 * @return a parameter map
	 */
	public static Map<String, String> map(String... strings) {
		if (strings.length % 2 != 0)
			throw new IllegalArgumentException("strings.length % 2 != 0");
		Map<String, String> mp = new HashMap<String, String>();
		for (int i = 0; i < strings.length; i += 2) {
			mp.put(strings[i], strings[i + 1]);
		}
		return mp;
	}
}
