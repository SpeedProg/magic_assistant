package com.reflexit.magiccards.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TimerTracer {
	private static SimpleDateFormat timestampFormat = new SimpleDateFormat("<kk:mm:ss.SSS>");
	private HashMap<String, TimerStats> timers = new HashMap<String, TimerStats>();

	static class TimerStats {
		long start;
		long end;

		public TimerStats() {
			this.start = this.end = System.currentTimeMillis();
		}
	}

	public void addTimer(String timer) {
		timers.put(timer, new TimerStats());
	}

	public void trace(String timer, String message) {
		long time = System.currentTimeMillis();
		String prefix = timestampFormat.format(Calendar.getInstance().getTime());
		TimerStats timerStats = getTimerStats(timer);
		System.out.println(prefix + " " + timer + " +" + (time - timerStats.end) + "ms (" + (time - timerStats.start) + "ms): " + message);
		timerStats.end = time;
	}

	private TimerStats getTimerStats(String timer) {
		TimerStats timerStats = timers.get(timer);
		if (timerStats != null)
			return timerStats;
		timers.put(timer, timerStats = new TimerStats());
		return timerStats;
	}
}
