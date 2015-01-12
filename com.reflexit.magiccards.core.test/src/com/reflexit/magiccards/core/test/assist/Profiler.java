package com.reflexit.magiccards.core.test.assist;

import java.sql.Time;
import java.text.SimpleDateFormat;

public class Profiler {
	public static long getUsedHeap() {
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		long usedHeap = runtime.totalMemory() - runtime.freeMemory();
		return usedHeap;
	}

	public static long onMem(Runnable run) {
		long before = getUsedHeap();
		run.run();
		long after = getUsedHeap();
		return after - before;
	}

	public static long onTime(Runnable run) {
		long before = System.currentTimeMillis();
		run.run();
		long after = System.currentTimeMillis();
		return after - before;
	}

	public static void testMem(Runnable run, long maxmem) {
		long curmem = onMem(run);
		if (curmem > maxmem)
			throw new IllegalStateException("Memory limit over " + curmem + ">" + maxmem);
	}

	public static void testTime(Runnable run, long maxtime) {
		long curtime = onTime(run);
		if (curtime > maxtime)
			throw new IllegalStateException("Timeout exceeded " + curtime + ">" + maxtime);
	}

	public static void testTimeAndMem(Runnable run, long maxtime, long maxmem) {
		long before = getUsedHeap();
		// System.err.println("memory1: " + niceMem(before));
		long curtime = onTime(run);
		long after = getUsedHeap();
		long curmem = after - before;
		// System.err.println("memory2: " + niceMem(after));
		System.err.println("memory: " + niceMem(curmem) + ", time: " + niceTime(curtime));
		if (curtime > maxtime)
			throw new IllegalStateException("Timeout exceeded " + curtime + ">" + maxtime);
		if (curmem > maxmem)
			throw new IllegalStateException("Memory limit over " + curmem + ">" + maxmem);
	}

	static final SimpleDateFormat msformat = new SimpleDateFormat("ss.SSS");
	static final SimpleDateFormat minformat = new SimpleDateFormat("mm.ss");

	private static String niceTime(long curtime) {
		if (curtime < 60 * 1000)
			return msformat.format(new Time(curtime)) + " ms";
		return minformat.format(new Time(curtime)) + " min";
	}

	private static String niceMem(long curmem) {
		if (curmem < 1000)
			return curmem + " bytes";
		curmem = curmem / 1024;
		if (curmem < 1000)
			return curmem + " kb";
		curmem = curmem / 1024;
		return curmem + " mb";
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// done
		}
	}

	public static void secsleep(int sec) {
		sleep(sec * 1000);
	}
}
