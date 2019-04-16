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
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;

public class CurrencyConvertor {
	public static Currency USD = Currency.getInstance("USD");
	private static final String FILE = "currency.txt";
	private static HashMap<String, Double> rates = new HashMap<>();
	private static HashMap<String, Date> dates = new HashMap<>();
	private static Currency currency = USD;
	private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ENGLISH);

	public static double loadRate(String from, String to) {
		return loadRate(from + "_" + to);
	}

	public static double loadRate(String cu) {
		double res = doLoadRate(cu);
		if (res == 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//
			}
			res = doLoadRate(cu);
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
			MagicLogger.log("Convertion rate " + cu + "=" + rate);
			dates.put(cu, date);
			return rate;
		} catch (Exception e) {
			return 0;
		}
	}

	public static URL getURL(String cu) throws MalformedURLException {
		URL url = new URL("https://free.currencyconverterapi.com/api/v5/convert?q=" + cu + "&compact=ultra&apiKey=8280553d986bdbde5834");
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

	public static double getRate(String string) {
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
		return getRate(c1.getCurrencyCode() + c2.getCurrencyCode());
	}
}
