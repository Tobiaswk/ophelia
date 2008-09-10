package net.roarsoftware.lastfm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.roarsoftware.lastfm.Result.Status;
import static net.roarsoftware.util.StringUtilities.encode;
import static net.roarsoftware.util.StringUtilities.map;
import static net.roarsoftware.util.StringUtilities.md5;

/**
 * The <code>Caller</code> class handles the low-level communication between the client and last.fm.<br/>
 * Direct usage of this class should be unnecessary since all method calls are available via the methods in
 * the <code>Artist</code>, <code>Album</code>, <code>User</code>, etc. classes.
 * If specialized calls which are not covered by the Java API are necessary this class may be used directly.<br/>
 * Supports the setting of a custom {@link Proxy} and a custom <code>User-Agent</code> HTTP header.
 *
 * @author Janni Kovacs
 */
public class Caller {

	private static final String API_ROOT = "http://ws.audioscrobbler.com/2.0/";
	private static final Caller instance = new Caller();

	private DocumentBuilder documentBuilder;
	private Proxy proxy;
	private String userAgent = "tst";

	private boolean debugMode = false;

	private Caller() {
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// better never happens
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the single instance of the <code>Caller</code> class.
	 *
	 * @return a <code>Caller</code>
	 */
	public static Caller getInstance() {
		return instance;
	}

	/**
	 * Sets a {@link Proxy} instance this Caller will use for all upcoming HTTP requests. May be <code>null</code>.
	 *
	 * @param proxy A <code>Proxy</code> or <code>null</code>.
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Sets a User Agent this Caller will use for all upcoming HTTP requests. For testing purposes use "tst".
	 * If you distribute your application use an identifiable User-Agent.
	 *
	 * @param userAgent a User-Agent string
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Sets the <code>debugMode</code> property. If <code>debugMode</code> is <code>true</code> all call() methods
	 * will print debug information and error messages on failure to stdout and stderr respectively.<br/>
	 * Default is <code>false</code>. Set this to <code>true</code> while in development and for troubleshooting.
	 *
	 * @param debugMode <code>true</code> to enable debug mode
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public Result call(String method, String apiKey, Map<String, String> params) throws CallException {
		return call(buildUrl(method, params, "api_key", apiKey));
	}

	public Result call(String method, String apiKey, String... params) throws CallException {
		return call(buildUrl(method, map(params), "api_key", apiKey));
	}

	private Result call(String url) throws CallException {
		try {
			if (debugMode)
				System.out.println("call: " + url);
			URL u = new URL(url);
			HttpURLConnection urlConnection;
			if (proxy != null)
				urlConnection = (HttpURLConnection) u.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection) u.openConnection();
			urlConnection.setRequestProperty("User-Agent", userAgent);
			int responseCode = urlConnection.getResponseCode();
			InputStream httpInput;
			if (responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
				httpInput = urlConnection.getErrorStream();
			} else if (responseCode != 200) {
				return Result.createHttpErrorResult(responseCode, urlConnection.getResponseMessage());
			} else {
				httpInput = urlConnection.getInputStream();
			}
			Document document = documentBuilder.parse(httpInput);
			Element root = document.getDocumentElement(); // lfm element
			String statusString = root.getAttribute("status");
			Status status = "ok".equals(statusString) ? Status.OK : Status.FAILED;
			if (status == Status.FAILED) {
				Element error = (Element) root.getElementsByTagName("error").item(0);
				int errorCode = Integer.parseInt(error.getAttribute("code"));
				String message = error.getTextContent();
				if (debugMode)
					System.err.printf("Failed. Code: %d, Error: %s%n", errorCode, message);
				return Result.createRestErrorResult(errorCode, message);
			}
			return Result.createOkResult(document);
		} catch (IOException e) {
			throw new CallException(e);
		} catch (SAXException e) {
			throw new CallException(e);
		}
	}

	public Result call(String method, Session session, String... params) {
		return call(method, session, map(params));
	}

	public Result call(String method, Session session, Map<String, String> params) {
		params.put("api_key", session.getApiKey());
		params.put("sk", session.getKey());
		String sig = Authenticator.createSignature(method, params, session.getSecret());
		params.put("api_sig", sig);
		String url = API_ROOT;
		try {
			URL u = new URL(url);
			HttpURLConnection urlConnection;
			if (proxy != null)
				urlConnection = (HttpURLConnection) u.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection) u.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("User-Agent", userAgent);
			urlConnection.setDoOutput(true);
			OutputStream outputStream = urlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
			String post = buildParameterQueue(method, params);
			if (debugMode) {
				System.out.println("call: " + url);
				System.out.println("body: " + post);
			}
			writer.write(post);
			writer.close();
			int responseCode = urlConnection.getResponseCode();
			InputStream httpInput;
			if (responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
				httpInput = urlConnection.getErrorStream();
			} else if (responseCode != 200) {
				return Result.createHttpErrorResult(responseCode, urlConnection.getResponseMessage());
			} else {
				httpInput = urlConnection.getInputStream();
			}
			Document document = documentBuilder.parse(httpInput);
			Element root = document.getDocumentElement(); // lfm element
			String statusString = root.getAttribute("status");
			Status status = "ok".equals(statusString) ? Status.OK : Status.FAILED;
			if (status == Status.FAILED) {
				Element error = (Element) root.getElementsByTagName("error").item(0);
				int errorCode = Integer.parseInt(error.getAttribute("code"));
				String message = error.getTextContent();
				if (debugMode)
					System.err.printf("Failed. Code: %d, Error: %s%n", errorCode, message);
				return Result.createRestErrorResult(errorCode, message);
			}
			return Result.createOkResult(document);
		} catch (IOException e) {
			throw new CallException(e);
		} catch (SAXException e) {
			throw new CallException(e);
		}
	}

	private String buildUrl(String method, Map<String, String> params, String... strings) {
		if (strings.length % 2 != 0)
			throw new IllegalArgumentException("strings.length % 2 != 0");
		StringBuilder builder = new StringBuilder(100);
		builder.append(API_ROOT);
		builder.append('?');
		builder.append(buildParameterQueue(method, params, strings));
		return builder.toString();
	}

	private String buildParameterQueue(String method, Map<String, String> params, String... strings) {
		StringBuilder builder = new StringBuilder(100);
		builder.append("method=");
		builder.append(method);
		builder.append('&');
		for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(encode(entry.getValue()));
			if (it.hasNext() || strings.length > 0)
				builder.append('&');
		}
		int count = 0;
		for (String string : strings) {
			builder.append(count % 2 == 0 ? string : encode(string));
			count++;
			if (count != strings.length) {
				if (count % 2 == 0) {
					builder.append('&');
				} else {
					builder.append('=');
				}
			}
		}
		return builder.toString();
	}

	private String createSignature(Map<String, String> params, String secret) {
		Set<String> sorted = new TreeSet<String>(params.keySet());
		StringBuilder builder = new StringBuilder(50);
		for (String s : sorted) {
			builder.append(s);
			builder.append(encode(params.get(s)));
		}
		builder.append(secret);
		return md5(builder.toString());
	}

	public Proxy getProxy() {
		return proxy;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public boolean isDebugMode() {
		return debugMode;
	}
}
