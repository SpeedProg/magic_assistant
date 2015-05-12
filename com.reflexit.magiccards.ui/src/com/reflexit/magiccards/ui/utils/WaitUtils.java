package com.reflexit.magiccards.ui.utils;

import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.xml.LibraryFilteredCardFileStore;

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

	public static Job scheduleWaitingJob(BooleanSupplier tester, long timeout, Runnable onSuccess) {
		Job j = new Job("working") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				runWhenCondition(tester, timeout, onSuccess);
				return Status.OK_STATUS;
			};
		};
		j.schedule();
		return j;
	}

	public static Job scheduleJob(Runnable runable) {
		Job j = new Job("working") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				runable.run();
				return Status.OK_STATUS;
			};
		};
		j.schedule();
		return j;
	}

	public static Job scheduleJob(Runnable runable, Runnable onDone) {
		Job j = new Job("working") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					runable.run();
				} finally {
					onDone.run();
				}
				return Status.OK_STATUS;
			};
		};
		j.schedule();
		return j;
	}

	public static void syncExec(Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}

	public static void asyncExec(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	public static void waitForLibrary() {
		waitForCondition(() -> {
			DataManager.getInstance().getModelRoot();
			return isLibraryInitialzed();
		}, 30000, 100);
	}

	private static boolean isLibraryInitialzed() {
		LibraryFilteredCardFileStore lib = (LibraryFilteredCardFileStore) DataManager.getCardHandler()
				.getLibraryFilteredStore();
		return lib.isInitialized();
	}

	public static void waitForDb() {
		final IDbCardStore<IMagicCard> magicDBStore = DataManager.getInstance().getMagicDBStore();
		if (!magicDBStore.isInitialized())
			DataManager.getInstance().asyncInitDb();
		waitForCondition(() -> {
			DataManager.getInstance().getModelRoot();
			return magicDBStore.isInitialized();
		}, 30000, 100);
	}
}
