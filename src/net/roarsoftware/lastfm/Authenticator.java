package net.roarsoftware.lastfm;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static net.roarsoftware.util.StringUtilities.map;
import static net.roarsoftware.util.StringUtilities.md5;
import net.roarsoftware.xml.DomElement;

/**
 * Provides bindings for the authentication methods of the last.fm API.
 * See <a href="http://www.last.fm/api/authentication">http://www.last.fm/api/authentication</a> for
 * authentication methods.
 *
 * @author Janni Kovacs
 * @see Session
 */
public class Authenticator {

	private Authenticator() {
	}

	/**
	 * Create a web service session for a user. Used for authenticating a user when the password can be inputted by the user.
	 *
	 * @param username last.fm username
	 * @param password last.fm password
	 * @param apiKey The API key
	 * @param secret Your last.fm API secret
	 * @return a Session instance
	 * @see Session
	 */
	public static Session getMobileSession(String username, String password, String apiKey, String secret) {
		String authToken = md5(username + md5(password));
		Map<String, String> params = map("api_key", apiKey, "username", username, "authToken", authToken);
		String sig = createSignature("auth.getMobileSession", params, secret);
		Result result = Caller.getInstance()
				.call("auth.getMobileSession", apiKey, "username", username, "authToken", authToken, "api_sig", sig);
		DomElement element = result.getContentElement();
		return Session.sessionFromElement(element, apiKey, secret);
	}

	/**
	 * Fetch an unathorized request token for an API account.
	 *
	 * @param apiKey A last.fm API key.
	 * @return a token
	 */
	public static String getToken(String apiKey) {
		Result result = Caller.getInstance().call("auth.getToken", apiKey);
		return result.getContentElement().getText();
	}

	/**
	 * Fetch a session key for a user.
	 *
	 * @param token A token returned by {@link #getToken(String)}
	 * @param apiKey A last.fm API key
	 * @param secret Your last.fm API secret
	 * @return a Session instance
	 * @see Session
	 */
	public static Session getSession(String token, String apiKey, String secret) {
		Result result = Caller.getInstance().call("auth.getSession", apiKey, "token", token);
		return Session.sessionFromElement(result.getContentElement(), apiKey, secret);
	}

	static String createSignature(String method, Map<String, String> params, String secret) {
		params = new TreeMap<String, String>(params);
		params.put("method", method);
		StringBuilder b = new StringBuilder(100);
		for (Entry<String, String> entry : params.entrySet()) {
			b.append(entry.getKey());
			b.append(entry.getValue());
		}
		b.append(secret);
		return md5(b.toString());
	}
}
