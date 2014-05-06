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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.exports.CsvImporter;

public class Currency {
	private static final String FILE = "currency.txt";
	private static HashMap<String, Double> rates = new HashMap<String, Double>();
	private static HashMap<String, Date> dates = new HashMap<String, Date>();
	private static String currency = "USD";

	public static double loadRate(String from, String to) {
		return loadRate(from + to);
	}

	public static double loadRate(String cu) {
		try {
			URL url = getURL(cu);
			CsvImporter importer = new CsvImporter(WebUtils.openUrl(url), ',');
			List<String> list = importer.readLine();
			String srate = list.get(1);
			double rate = Double.valueOf(srate);
			rates.put(cu, rate);
			String d = list.get(2);
			String t = list.get(3);
			// System.err.println(d + " " + t); // 5/6/2014 8:42pm
			Date date = Calendar.getInstance().getTime();
			SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy hh:mmaa");
			try {
				date = parser.parse(d + " " + t);
			} catch (ParseException e) {
				// ignore
			}
			dates.put(cu, date);
			System.err.println(cu + "=" + rate + " on " + date);
			return rate;
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
		File file = new File(DataManager.getTablesDir(), FILE);
		save(file);
	}

	public synchronized static void save(File file) throws FileNotFoundException {
		PrintStream st = new PrintStream(file);
		try {
			Date date = Calendar.getInstance().getTime();
			for (String cu : rates.keySet()) {
				double rate = rates.get(cu);
				st.println(cu + "|" + rate + "|" + date);
			}
		} finally {
			st.close();
		}
	}

	private static synchronized void load() throws IOException {
		File file = new File(DataManager.getTablesDir(), FILE);
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

	private static void initialize() throws IOException, FileNotFoundException {
		Date date = new Date(Date.parse("Tue May 06 20:48:00 EDT 2014"));
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
					long time = Date.parse(sdate);
					Date date = new Date(time);
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
		currency = string;
	}

	public static String getCurrency() {
		return currency;
	}

	public static double getRate(String string) {
		//
		// if (!WebUtils.isWorkOffline())
		// return loadRate(string);
		return rates.get(string);
	}
}
