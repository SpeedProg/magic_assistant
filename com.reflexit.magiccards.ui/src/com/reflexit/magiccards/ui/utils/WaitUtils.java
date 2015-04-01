package com.reflexit.magiccards.ui.utils;

import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public class WaitUtils {
	public static boolean waitForCondition(BooleanSupplier tester, long timeoutmillis, long intervals) {
		long start = System.currentTimeMillis();
		while (tester.getAsBoolean() == false) {
			long now = System.currentTimeMillis();
			if (now - start >= timeoutmillis) return false;
			Display display = Display.getCurrent();
			if (display == null) {
				try {
					Thread.sleep(intervals);
				} catch (InterruptedException e) {
					// ok
				}
			} else {
				uisleep(intervals, display);
			}
		}
		return true;
	}

	public static void scheduleWaitingJob(BooleanSupplier tester, long timeout, Runnable onSuccess) {
		new Job("working") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				runWhenCondition(tester, timeout, onSuccess);
				return Status.OK_STATUS;
			};
		}.schedule();
	}

	public static boolean runWhenCondition(BooleanSupplier tester, long timeoutmillis, Runnable onSuccess) {
		if (waitForCondition(tester, timeoutmillis, timeoutmillis / 10)) {
			onSuccess.run();
			return true;
		}
		return false;
	}

	public static void uisleep(long msec, Display display) {
		long cur = System.currentTimeMillis();
		long pass = 0;
		while (pass < msec) {
			if (!display.readAndDispatch())
				display.sleep();
			pass = System.currentTimeMillis() - cur;
		}
	}

	public static void scheduleJob(Runnable runable) {
		new Job("working") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				runable.run();
				return Status.OK_STATUS;
			};
		}.schedule();
	}

	public static void scheduleJob(Runnable runable, Runnable onDone) {
		new Job("working") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					runable.run();
				} finally {
					onDone.run();
				}
				return Status.OK_STATUS;
			};
		}.schedule();
	}

	public static void syncExec(Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}

	public static void asyncExec(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}
}
