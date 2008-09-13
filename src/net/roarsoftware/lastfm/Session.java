package net.roarsoftware.lastfm;

import net.roarsoftware.xml.DomElement;

/**
 * Contains Session data relevant for making API calls which require authentication.
 * A <code>Session</code> instance is passed to all methods requiring previous authentication.
 *
 * @author Janni Kovacs
 * @see net.roarsoftware.lastfm.Authenticator
 */
public class Session {

	private String apiKey;
	private String secret;
	private String username;
	private String key;
	private boolean subscriber;

	public String getSecret() {
		return secret;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getKey() {
		return key;
	}

	public boolean isSubscriber() {
		return subscriber;
	}

	public String getUsername() {
		return username;
	}

	static Session sessionFromElement(DomElement element, String apiKey, String secret) {
		Session s = new Session();
		s.username = element.getChildText("name");
		s.key = element.getChildText("key");
		s.subscriber = element.getChildText("subscriber").equals("1");
		s.apiKey = apiKey;
		s.secret = secret;
		return s;
	}
}
