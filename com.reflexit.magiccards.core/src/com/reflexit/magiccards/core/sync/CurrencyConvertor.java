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
import java.util.List;
import java.util.Locale;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.exports.CsvImporter;

public class CurrencyConvertor {
	public static Currency USD = Currency.getInstance("USD");
	private static final String FILE = "currency.txt";
	private static HashMap<String, Double> rates = new HashMap<String, Double>();
	private static HashMap<String, Date> dates = new HashMap<String, Date>();
	private static Currency currency = USD;
	private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ENGLISH);

	public static double loadRate(String from, String to) {
		return loadRate(from + to);
	}

	public static synchronized double loadRate(String cu) {
		try {
			URL url = getURL(cu);
			CsvImporter importer = new CsvImporter(WebUtils.openUrl(url), ',');
			try {
				List<String> list = importer.readLine();
				String srate = list.get(1);
				double rate = Double.valueOf(srate);
				rates.put(cu, rate);
				String d = list.get(2);
				String t = list.get(3);
				// System.err.println(d + " " + t); // 5/6/2014 8:42pm
				Date date = Calendar.getInstance().getTime();
				try {
					date = DATE_PARSER.parse(d + " " + t);
				} catch (ParseException e) {
					// ignore
				}
				dates.put(cu, date);
				// System.err.println(cu + "=" + rate + " on " + date);
				return rate;
			} finally {
				importer.close();
			}
		} catch (IOException e) {
			return 0;
		}
	}

	public static URL getURL(String cu) throws MalformedURLException {
		URL url = new URL("http://finance.yahoo.com/d/quotes.csv?e=.csv&f=sl1d1t1&s=" + cu + "=X");
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
			date = DATE_PARSER.parse("5/6/2014 8:42pm");
		} catch (ParseException e) {
			date = new Date();
		}
		rates.put("USDEUR", 0.72);
		rates.put("USDCAD", 1.09598);
		rates.put("USDUSD", 1.0);
		dates.put("USDEUR", date);
		dates.put("USDCAD", date);
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
					if (!cu.isEmpty()) {
						if (rate != 0) {
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
