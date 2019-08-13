 package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;

public class CurrencyConvertor {
	public static Currency USD = Currency.getInstance("USD");
	private static final String FILE = "currency.txt";
	private static HashMap<String, Double> rates = new HashMap<>();
	private static HashMap<String, Date> dates = new HashMap<>();
	private static LocalDateTime lastError = null;
	private static Currency currency = USD;
	private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ENGLISH);
	private static JSONParser parser = new JSONParser();

	public static double loadRate(String from, String to) {
		return loadRate(from + "_" + to);
	}

	public static double loadRate(String cu) {
		cu = convertCu(cu);
		double res = doLoadRate(cu);
		if (res == 0) {
			MagicLogger.info("We got a conversion rate of 0 from 1st api, trying again");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//
			}
			res = doLoadRate(cu);
		}
		
		if (res == 0) {
			MagicLogger.log("Conversion rate of 1st api was still 0, trying 2nd conversion api");
			res = doLoadRateApi2(cu);
			if (res == 0) {
				MagicLogger.info("We got a conversion rate of 0 from 1st api, trying again");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					//
				}
				res = doLoadRateApi2(cu);
				if (res == 0)
					MagicLogger.log("Conversion rate of 2nd api was still 0. Returning 0.");
			}
		}

		return res;
	}

	protected static synchronized double doLoadRate(String cu) {
		try {
			URL url = getURL(convertCu(cu));
			// {"USD_PHP":50.310001}
			String res = WebUtils.openUrlText(url);
			String[] parts = res.split(":");
			if (parts.length < 2)
				throw new IllegalArgumentException("Cannot parse " + res);
			double rate = Double.parseDouble(parts[1].replace("}", ""));
			rates.put(cu, rate);
			Date date = Calendar.getInstance().getTime();
			MagicLogger.info("Convertion rate " + cu + "=" + rate);
			dates.put(cu, date);
			return rate;
		} catch (Exception e) {
			MagicLogger.log("Failed to get conversion rate from free.currencyconverterapi.com for " + cu);
			return 0;
		}
	}

	public static URL getURL(String cu) throws MalformedURLException {
		URL url = new URL("https://free.currencyconverterapi.com/api/v5/convert?q=" + cu + "&compact=ultra&apiKey=8280553d986bdbde5834");
		return url;
	}

	/**
	 * Gets exchange rate from exchangeratesapi.io
	 * @param cu exchange rate string from_to
	 * @return the exchange rate
	 */
	protected static synchronized double doLoadRateApi2(String cu) {
		String[] currencies = cu.split("_");
		try {
			URL url = getURLApi2(currencies[0], currencies[1]);
			//{"rates":{"GBP":0.89025,"USD":1.1424},"base":"EUR","date":"2019-01-15"}
			String res = WebUtils.openUrlText(url);

			try {
				JSONObject baseObject = (JSONObject) parser.parse(res);
				JSONObject ratesObject = (JSONObject) baseObject.get("rates");
				double rate = ((Number)ratesObject.get(currencies[1])).doubleValue();
				MagicLogger.info("Convertion rate " + cu + "=" + rate);
				rates.put(cu, rate);
				dates.put(cu, Calendar.getInstance().getTime());
				return rate;
			} catch (org.json.simple.parser.ParseException e) {
				return 0;
			}
		} catch (Exception e) {
			MagicLogger.log("Failed to get conversion rate from api.exchangeratesapi.io for " + cu);
			return 0;
		}
	}

	public static URL getURLApi2(String from, String to) throws MalformedURLException {
		URL url = new URL("https://api.exchangeratesapi.io/latest?symbols=" + to + "&base="+from);
		return url;
	}


	public static synchronized void update() {
		if (WebUtils.isWorkOffline())
			return;
		for (String cu : rates.keySet()) {
			loadRate(cu);
		}
	}

	public static void main(String[] args) {
		update();
	}

	static {
		try {
			load();
		} catch (IOException e) {
			MagicLogger.log(e);
		}
	}

	public static synchronized void save() throws FileNotFoundException {
		File file = getCurrencyFile();
		save(file);
	}

	public static File getCurrencyFile() {
		File file = new File(DataManager.getInstance().getTablesDir(), FILE);
		return file;
	}

	public synchronized static void save(File file) throws FileNotFoundException {
		PrintStream st = new PrintStream(file);
		try {
			Date date = Calendar.getInstance().getTime();
			for (String cu : rates.keySet()) {
				double rate = rates.get(cu);
				st.print(cu + "|" + rate + "|" + DATE_PARSER.format(date));
				st.print('\n');
			}
		} finally {
			st.close();
		}
	}

	private static synchronized void load() throws IOException {
		File file = getCurrencyFile();
		if (!file.exists()) {
			initialize();
			save();
		} else {
			try {
				initialize();
			} catch (Exception e) {
				// ignore
			}
			InputStream st = new FileInputStream(file);
			load(st);
		}
	}

	private static synchronized void initialize() throws IOException, FileNotFoundException {
		Date date;
		try {
			date = DATE_PARSER.parse("01/12/2017 8:42pm");
		} catch (ParseException e) {
			date = new Date();
		}
		
	
		rates.put("USD_EUR", 0.840796);
		rates.put("USD_CAD", 1.2874);
		rates.put("USD_USD", 1.0);
		dates.put("USD_EUR", date);
		dates.put("USD_CAD", date);
	}

	private static synchronized void load(InputStream st) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		try {
			String line;
			while ((line = r.readLine()) != null) {
				try {
					String[] attrs = line.split("\\|", 3);
					String cu = attrs[0].trim();
					String srate = attrs.length >= 2 ? attrs[1].trim() : "0";
					String sdate = attrs.length >= 3 ? attrs[2].trim() : "";
					double rate = Double.valueOf(srate);
					Date date;
					try {
						date = DATE_PARSER.parse(sdate);
					} catch (ParseException e) {
						date = new Date();
					}

					if (rate != 0) {
						cu = convertCu(cu);
						if (!cu.isEmpty()) {
							rates.put(cu, rate);
							dates.put(cu, date);
						}
					}
				} catch (Exception e) {
					MagicLogger.log("bad currency record: " + line);
					MagicLogger.log(e);
				}
			}
		} finally {
			r.close();
		}
	}

	private static String convertCu(String cu) {
		if (cu == null || cu.isEmpty())
			return "";
		if (cu.length() == 6) {
			return cu.substring(0, 3) + "_" + cu.substring(3);
		}
		if (cu.contains("_"))
			return cu;
		return "";
	}

	public static void setCurrency(String string) {
		currency = Currency.getInstance(string);
	}

	public static void setCurrency(Currency string) {
		currency = string;
	}

	public static Currency getCurrency() {
		return currency;
	}

	private static double getRate(String string) {
		LocalDateTime current = LocalDateTime.now();
		// if the last error is less then 10min ago just return 0.0 rate
		if (lastError != null && lastError.plus(10, ChronoUnit.MINUTES).isAfter(current)) {
			MagicLogger.log("There was an exchange rate API error in the last 10min not checking again.");
			return 0.0d;
		}
		string = convertCu(string);
		Double rate = rates.get(string);
		if (rate == null) {
			if (!WebUtils.isWorkOffline()) {
				double drate = loadRate(string);
				if (drate == 0) { // invalid currency
					throw new IllegalArgumentException("Cannot find rate for " + string);
				}
				return drate;
			} else {
				throw new IllegalArgumentException("Cannot find rate for " + string);
			}
		}
		return rate.doubleValue();
	}

	public static float convertFromInto(float price, Currency from, Currency cur) {
		if (from == cur)
			return price;
		return (float) (price * getRate(from.getCurrencyCode() + cur.getCurrencyCode()));
	}

	public static double getRate(Currency c1, Currency c2) {
		if (c1.getCurrencyCode().equals(c2.getCurrencyCode()))
			return 1;
		return getRate(c1.getCurrencyCode() + c2.getCurrencyCode());
	}
}
