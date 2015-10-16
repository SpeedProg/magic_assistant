package com.reflexit.magiccards.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TimerTracer {
	private SimpleDateFormat timestampFormat = new SimpleDateFormat("<kk:mm:ss.SSS>");
	private static boolean tracing = false;
	private HashMap<String, Timer> timers = new HashMap<String, Timer>();

	public static class Timer {
		long start;
		long end;
		long total;
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
			total += curr;
			count++;
			return curr;
		}

		public long prob() {
			this.end = System.currentTimeMillis();
			long curr = end - start;
			total += curr;
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

		public long getTotal() {
			return total;
		}

		public double getAve() {
			if (count == 0)
				return total;
			return total / (double) count;
		}

		public int getCount() {
			return count;
		}

		public String getName() {
			return name;
		}

		public void reset() {
			total = 0;
			count = 0;
			start();
		}
	}

	public void addTimer(String timer) {
		timers.put(timer, new Timer(timer));
	}

	public void trace(String timer, String message) {
		Timer t = getTimer(timer);
		long diff = t.end();
		dump(t, message, diff);
		t.start();
	}

	private void dump(Timer t, String message, long diff) {
		if (tracing) {
			String prefix = timestampFormat.format(Calendar.getInstance().getTime()) + "["
					+ Thread.currentThread().getId() + "]";
			if (t.getName().equals("-")) {
				String text = String.format("+%03d %s", diff, message);
				System.out.println(prefix + " " + text);
			} else {
				String text = String.format("+%03d (%3d ms/%3d = %5.2f ms) %6s %s", diff, t.total, t.count,
						t.getAve(), message, t.name);
				System.out.println(prefix + " " + text);
			}
		}
	}

	public void traceStart(String string) {
		Timer t = getTimer(string);
		dump(t, "start", 0);
		t.start();
	}

	public void traceEnd(String string) {
		Timer t = getTimer(string);
		long cur = t.end();
		dump(t, "end", cur);
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

	public static void setTracing(boolean tr) {
		tracing = tr;
	}
}
