package com.reflexit.magiccards.core;

import com.reflexit.magiccards.core.Activator;

public class MagicLogger {
	private static TimerTracer tracer = new TimerTracer();
	static boolean rcp = false;
	static {
		if (System.getProperty("eclipse.home.location") != null) {
			rcp = true;
		}
	}

	public static void log(String message) {
		if (rcp == false) {
			System.err.println("Log: " + message);
		} else {
			Activator.log(message);
		}
	}

	public static void log(Throwable e) {
		if (rcp == false) {
			System.err.println("Exception: " + e.getStackTrace());
		} else {
			Activator.log(e);
		}
	}

	public static void trace(String message) {
		tracer.trace("m", message);
	}

	public static void tracet(String key, String message) {
		tracer.trace(key, message);
	}

	public static void setTracing(boolean trace) {
		tracer.setTracing(trace);
	}

	public static void traceStart(String string) {
		tracer.traceStart(string);
	}

	public static void traceEnd(String string) {
		tracer.traceEnd(string);
	}

	public static TimerTracer getTracer() {
		return tracer;
	}
}
