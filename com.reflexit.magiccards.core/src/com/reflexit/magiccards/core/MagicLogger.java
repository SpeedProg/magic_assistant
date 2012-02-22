package com.reflexit.magiccards.core;

import com.reflexit.magiccards.core.Activator;

public class MagicLogger {
	static boolean rcp = false;
	static {
		if (System.getProperty("eclipse.home.location") != null) {
			rcp = true;
		}
	}

	public static void log(String message) {
		if (rcp) {
			System.err.println("Log: " + message);
		} else {
			Activator.log(message);
		}
	}

	public static void log(Throwable e) {
		if (rcp) {
			System.err.println("Exception: " + e.getStackTrace());
		} else {
			Activator.log(e);
		}
	}
}
