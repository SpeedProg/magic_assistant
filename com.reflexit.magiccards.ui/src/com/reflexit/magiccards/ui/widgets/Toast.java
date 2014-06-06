package com.reflexit.magiccards.ui.widgets;

import org.eclipse.nebula.animation.effects.AlphaEffect;
import org.eclipse.nebula.animation.movement.LinearInOut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Toast {
	private Shell shell;

	public Toast(Display display, String text, int x, int y) {
		shell = new Shell(display, SWT.TOOL | SWT.NO_TRIM);
		shell.setLayout(new GridLayout());
		shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		final Label button = new Label(shell, SWT.WRAP);
		button.setText(text);
		GridData ld = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		button.setLayoutData(ld);
		shell.setSize(200, 100);
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
