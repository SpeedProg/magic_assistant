package com.reflexit.magiccards.ui.widgets;

import org.eclipse.nebula.animation.effects.AlphaEffect;
import org.eclipse.nebula.animation.movement.LinearInOut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Toast {
	private static final int MIN_HIGHT = 100;
	private static final int MIN_WIDTH = 200;
	private Shell shell;

	public Toast(Display display, String text, int x, int y) {
		createShell(display, text, x, y);
	}

	public Toast(Shell parent, String text) {
		Point location = parent.getLocation();
		Point size = parent.getSize();
		createShell(parent.getDisplay(), text, location.x + (size.x - MIN_WIDTH) / 2,//
				location.y + (size.y - MIN_HIGHT) / 2);
	}

	private void createShell(Display display, String text, int x, int y) {
		shell = new Shell(display, SWT.TOOL | SWT.NO_TRIM);
		shell.setLayout(new GridLayout());
		shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		final Label button = new Label(shell, SWT.WRAP);
		button.setText(text);
		GridData ld = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		button.setLayoutData(ld);
		shell.setSize(MIN_WIDTH, MIN_HIGHT);
		shell.setLocation(x, y);
	}

	public void open() {
		AlphaEffect.fadeOnClose(shell, 3000, new LinearInOut(), null);
		shell.open();
		shell.close();
	}

	public static void main(String[] args) {
		Display display = new Display();
		Toast shell = new Toast(display, "Some problem!", 300, 300);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public boolean isDisposed() {
		return shell.isDisposed();
	}

	public Shell getShell() {
		return shell;
	}
}
