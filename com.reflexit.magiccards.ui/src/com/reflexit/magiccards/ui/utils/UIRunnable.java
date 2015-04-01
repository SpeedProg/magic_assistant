package com.reflexit.magiccards.ui.utils;

import org.eclipse.swt.widgets.Display;

@FunctionalInterface
public interface UIRunnable extends Runnable {
	public static UIRunnable syncExec(Runnable runnable) {
		return new UIRunnable() {
			@Override
			public void runInUiThread() {
				runnable.run();
			}
		};
	}

	public static UIRunnable asyncExec(Runnable runnable) {
		return new UIRunnable() {
			@Override
			public void runInUiThread() {
				runnable.run();
			}

			@Override
			public void run() {
				Display display = getDisplay();
				if (display.isDisposed()) return;
				display.asyncExec(() -> runInUiThread());
			}
		};
	}

	public abstract void runInUiThread();

	@Override
	public default void run() {
		Display display = getDisplay();
		if (display.isDisposed()) return;
		display.syncExec(() -> runInUiThread());
	}

	public default Display getDisplay() {
		return Display.getDefault();
	}
}
