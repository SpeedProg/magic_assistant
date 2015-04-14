package com.reflexit.magiccards.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class MagicLogger {
	private static TimerTracer tracer;
	static boolean rcp = false;
	private static boolean debugging;
	static {
		tracer = new TimerTracer();
		if (System.getProperty("eclipse.home.location") != null) {
			rcp = true;
		}
		if (System.getProperty("magic.tracing") != null) {
			tracer.setTracing(true);
		}
		if (System.getProperty("magic.debugging") != null) {
			debugging = true;
		}
	}

	public static void log(String message) {
		if (rcp == false) {
			System.err.println("Log: " + message);
		} else {
			Activator.log(message);
		}
	}

	public static void info(String message) {
		if (rcp == false) {
			System.err.println("Info: " + message);
		} else {
			Activator.info(message);
		}
	}

	public static void log(Throwable e) {
		if (rcp == false) {
			e.printStackTrace(System.err);
		} else {
			Activator.log(e);
		}
	}

	public static void setDebugging(boolean trace) {
		debugging = trace;
	}

	public static void debug(String message) {
		if (!debugging)
			return;
		if (rcp == false) {
			System.err.println("Debug: " + message);
		} else {
			Activator plugin = Activator.getDefault();
			plugin.getLog().log(new Status(IStatus.ERROR, plugin.PLUGIN_ID, message));
		}
	}

	public static void trace(String message) {
		tracer.trace("-", message);
	}

	public static void trace(String key, String message) {
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
