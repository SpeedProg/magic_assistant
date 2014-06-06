package com.reflexit.magiccards.ui.widgets;

import org.eclipse.nebula.animation.AnimationRunner;
import org.eclipse.nebula.animation.effects.IEffect;
import org.eclipse.nebula.animation.effects.MoveControlEffect;
import org.eclipse.nebula.animation.movement.ExpoOut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SlidingPaneAnimation {
	public static SlidingPaneAnimation INSTANCE = new SlidingPaneAnimation();
	private int effectTime = 1000;
	private AnimationRunner runner;
	private ExpoOut movement;

	public SlidingPaneAnimation() {
		runner = new AnimationRunner();
		movement = new ExpoOut();
	}

	private void runExample() {
		Display display = new Display();
		final Shell shell = new Shell(display, SWT.CLOSE | SWT.RESIZE | SWT.DOUBLE_BUFFERED);
		shell.setSize(600, 600);
		final Composite composite1 = new Composite(shell, SWT.NONE);
		final Composite composite2 = new Composite(shell, SWT.PUSH);
		setFormLayout(shell); // after childrent creates, set their layout
								// properties
		// contents
		composite2.setLayout(new GridLayout());
		composite2.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		Button back = new Button(composite2, SWT.PUSH);
		back.setText("Back");
		back.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				popControl(composite2, 1, 0);
			}
		});
		Button fw = new Button(composite2, SWT.PUSH);
		fw.setText("Forward");
		fw.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushControl(composite1, 1, 0);
			}
		});
		composite1.setLayout(new GridLayout());
		composite1.setBackground(display.getSystemColor(SWT.COLOR_RED));
		Button show = new Button(composite1, SWT.PUSH);
		show.setText("Show");
		show.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushControl(composite2, 0, 1);
			}
		});
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public FormData createFormDataFill() {
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0);
		data1.top = new FormAttachment(0);
		data1.bottom = new FormAttachment(100);
		data1.right = new FormAttachment(100);
		return data1;
	}

	public void setFormLayout(Composite parent) {
		parent.setLayout(new FormLayout());
		Control[] children = parent.getChildren();
		for (Control control : children) {
			control.setLayoutData(createFormDataFill());
		}
	}

	public void pushControl(final Composite comp, int horDir, int verDir) {
		runner.cancel();
		int startX = comp.getSize().x * horDir;
		int startY = comp.getSize().y * verDir;
		comp.setLocation(startX, startY);
		comp.moveAbove(null); // show control
		Runnable onStop = new Runnable() {
			@Override
			public void run() {
				comp.setLocation(0, 0);
				comp.moveAbove(null); // show control
				comp.getParent().layout(true, true);
			}
		};
		IEffect effect1 = new MoveControlEffect(comp, startX, 0, startY, 0, effectTime, movement, onStop, onStop);
		runner.runEffect(effect1);
	}

	public void popControl(final Composite comp, int horDir, int verDir) {
		runner.cancel();
		final int endX = comp.getSize().x * horDir;
		final int endY = comp.getSize().y * verDir;
		comp.moveAbove(null); // show control
		Runnable onStop = new Runnable() {
			@Override
			public void run() {
				// System.err.println("on stop");
				comp.setLocation(endX, endY);
				comp.moveBelow(null); // hide control
				comp.getParent().layout(true, true);
			}
		};
		// comp.setLocation(width / 2, 0);
		IEffect effect1 = new MoveControlEffect(comp, 0, endX, 0, endY, effectTime, movement, onStop, onStop);
		runner.runEffect(effect1);
	}

	public static void main(String[] args) throws InterruptedException {
		new SlidingPaneAnimation().runExample();
	}
}
