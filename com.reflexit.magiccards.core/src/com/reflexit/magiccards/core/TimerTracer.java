package com.reflexit.magiccards.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TimerTracer {
	private static SimpleDateFormat timestampFormat = new SimpleDateFormat("<kk:mm:ss.SSS>");
	private static boolean tracing = false;
	private HashMap<String, Timer> timers = new HashMap<String, Timer>();

	public static class Timer {
		long start;
		long end;
		long diff;
		int count;
		private String name;

		public Timer(String name) {
			this.name = name;
			start();
		}

		public void start() {
			this.start = this.end = System.currentTimeMillis();
		}

		public long end() {
			this.end = System.currentTimeMillis();
			long curr = end - start;
			diff += curr;
			count++;
			return curr;
		}

		public long prob() {
			this.end = System.currentTimeMillis();
			long curr = end - start;
			diff += curr;
			count++;
			this.start = this.end;
			return curr;
		}

		public long getStart() {
			return start;
		}

		public long getEnd() {
			return end;
		}

		public long getDiff() {
			return diff;
		}

		public int getCount() {
			return count;
		}

		public String getName() {
			return name;
		}

		public void reset() {
			diff = 0;
			count = 0;
			start();
		}
	}

	public void addTimer(String timer) {
		timers.put(timer, new Timer(timer));
	}

	public void trace(String timer, String message) {
		Timer t = getTimer(timer);
		long diff = t.prob();
		if (tracing) {
			long time = System.currentTimeMillis();
			String prefix = timestampFormat.format(Calendar.getInstance().getTime());
			Timer timerStats = getTimer(timer);
			if (t.count == 0) {
				System.out.println(prefix + " " + timer + ": " + message);
			} else {
				System.out.println(prefix + " " + timer + " +" + (diff) + "ms (" + timerStats.count + " times " + t.diff
						/ (float) timerStats.count + " ave " + (t.diff) + "ms): " + message);
			}
			timerStats.end = time;
		}
	}

	public void trace(Timer t, String message) {
		if (tracing) {
			String prefix = timestampFormat.format(Calendar.getInstance().getTime());
			if (t.count == 0) {
				System.out.println(prefix + " " + t.name + ": " + message);
			} else {
				System.out.println(prefix + " " + t.name + " +" + (t.end - t.start) + "ms (" + t.count + " times " + t.diff
						/ (float) t.count + " ave " + t.diff + " ms): " + message);
			}
		}
	}

	public void traceStart(String string) {
		Timer t = getTimer(string);
		t.start();
		trace(t, "start");
	}

	public void traceEnd(String string) {
		Timer t = getTimer(string);
		t.end();
		trace(t, "end");
	}

	public void removeTimer(String t) {
		timers.remove(t);
	}

	public Timer getTimer(String timer) {
		Timer timerStats = timers.get(timer);
		if (timerStats != null)
			return timerStats;
		timers.put(timer, timerStats = new Timer(timer));
		return timerStats;
	}

	public void setTracing(boolean tracing) {
		this.tracing = tracing;
	}
}
