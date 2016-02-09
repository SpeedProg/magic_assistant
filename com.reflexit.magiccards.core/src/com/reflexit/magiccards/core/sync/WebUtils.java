package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.OfflineException;

public class WebUtils {
	private static boolean workOffline = false;

	public static boolean isWorkOffline() {
		return workOffline;
	}

	public static void setWorkOffline(boolean workOffline) {
		WebUtils.workOffline = workOffline;
	}

	private static TrustManager[] trustedCerts = new TrustManager[] {
			new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}
	};

	/**
	 * Open the specified URL. Works with HTTPS as well.<br/>
	 * Do nothing if working offline only mode is enabled.<br/>
	 * <br/>
	 * HTTPS warning: Just to open and be able to read the content of resulting response. This implementation
	 * accept every server certificates therefore, as an example, it is not save against
	 * man-in-the-middle-attacks. Do not use with sensitive data thats needs a secure connection.
	 *
	 * @param url
	 *            Requested URL.
	 * @return response of request with the specified URL as an open stream.
	 * @throws IOException
	 * @see {@link #isWorkOffline(boolean)}, {@link #setWorkOffline(boolean)}
	 */
	public static InputStream openUrl(URL url) throws IOException {
		return openUrl(url, 3);
	}

	public static InputStream openUrl(URL url, int maxAttempts) throws IOException {
		IOException rt = null;
		// 3 attempts
		for (int i = 0; i < maxAttempts; i++) {
			// Don't do anything if offline only working is enabled.
			if (WebUtils.isWorkOffline()) {
				throw new OfflineException();
			}
			try {
				URLConnection openConnection = url.openConnection();
				// Checking if it is a HttpsURLConnection first and HttpURLConnection next.
				// Don't change this order due to HttpsURLConnection extends HttpURLConnection.
				if (openConnection instanceof HttpsURLConnection) {
					// HTTPS
					// Do stuff that trust everything.
					// MAYBE: HTTPS security
					try {
						// Do basics
						configureConnectionDefaults(openConnection);
						// Context stuff
						SSLContext ctx = SSLContext.getInstance("TLS");
						ctx.init(null, trustedCerts, null);
						HttpsURLConnection con = (HttpsURLConnection) openConnection;
						// Additional HTTPS connection configuration
						con.setSSLSocketFactory(ctx.getSocketFactory());
						con.setRequestMethod("GET");
						con.connect();						// MAYBE: HTTPS response code handling
						// System.err.println(con.getResponseCode()+": "+url.toExternalForm());
					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						throw new IOException(e);
					}
				} else if (openConnection instanceof HttpURLConnection) {
					// Do basics
					configureConnectionDefaults(openConnection);
					// HTTP
					HttpURLConnection con = (HttpURLConnection) openConnection;
					con.connect();					// MAYBE: HTTP response code handling
					// System.err.println(con.getResponseCode()+": "+url.toExternalForm());
				} else {
					// Not HTTP nor HTTPS connection, it can be local file i.e. file://
					i = maxAttempts; // we will try to open it only once
				}
				InputStream openStream = openConnection.getInputStream();
				return openStream;
			} catch (IOException e) {
				MagicLogger.log("Connection error on url " + url + ": " + e.getMessage() + ". Attempt " + i);
				rt = e;
				continue;
			}
		}
		if (rt != null) {
			MagicLogger.log("Connection error on url " + url + ": " + rt.getMessage() + ". Giving up");
			throw rt;
		}
		throw new RuntimeException("Not possible");
	}

	public static BufferedReader openUrlReader(URL url, int attempts) throws IOException {
		InputStream openStream = openUrl(url, attempts);
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, FileUtils.CHARSET_UTF_8),
				FileUtils.DEFAULT_BUFFER_SIZE);
		return st;
	}

	/**
	 * Open an URL and return its content.
	 *
	 * @param url
	 * @return response content of specified URL.
	 * @throws IOException
	 *             If connection could not be established or content could not be read.
	 */
	public static String openUrlText(URL url) throws IOException {
		return openUrlText(url, 3);
	}

	public static String openUrlText(URL url, int attempts) throws IOException {
		MagicLogger.traceStart("reading: " + url.toExternalForm());
		try {
			return FileUtils.readStreamAsStringAndClose(openUrl(url, attempts));
		} finally {
			MagicLogger.traceEnd("reading: " + url.toExternalForm());
		}
	}

	/**
	 * Configure specified URLConnection with some defaults like "User-Agent", "Accept-Charset", timeouts,
	 * aso.
	 *
	 * @param connection
	 */
	private static void configureConnectionDefaults(URLConnection connection) {
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:40.0) Gecko/20100101 Firefox/40.0");
		connection.setRequestProperty("Accept-Charset", FileUtils.UTF8);
		connection.setRequestProperty("Accept-Language", "en_US");
		connection.setConnectTimeout(60 * 1000);
		connection.setReadTimeout(60 * 1000);
	}

	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}
}
