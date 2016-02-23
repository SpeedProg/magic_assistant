package com.reflexit.magiccards.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.animation.AnimationRunner;
import org.eclipse.nebula.animation.effects.IEffect;
import org.eclipse.nebula.animation.effects.MoveControlEffect;
import org.eclipse.nebula.animation.movement.ExpoOut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
		final Composite composite2 = new Composite(shell, SWT.PUSH);
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
		// Button show = new Button(composite1, SWT.PUSH);
		// final Control composite1 = show;
		final Composite composite1 = new Composite(shell, SWT.NONE);
		Label show = new Label(composite1, SWT.PUSH);
		// Control composite1 = show;
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
		// Button show = new Button(composite1, SWT.PUSH);
		show.setText("Show");
		show.setLayoutData(GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).create());
		show.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				pushControl(composite2, 0, 1);
			}
		});
		// show.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// pushControl(composite2, 0, 1);
		// }
		// });
		setAnimationLayoutOn(shell); // after childrent creates, set their
										// layout
		// properties
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

	public void setAnimationLayoutOn(Composite parent) {
		parent.setLayout(new FormLayout());
		Control[] children = parent.getChildren();
		for (Control control : children) {
			control.setLayoutData(createFormDataFill());
		}
	}

	/**
	 * No animation
	 * 
	 * @param comp
	 */
	public void pushControl(final Control comp) {
		runner.cancel();
		comp.moveAbove(null); // show control
		comp.setFocus();
		comp.getParent().layout(true, true);
	}

	public void pushControl(final Control comp, float startWK, float startHK, float endWK, float endHK) {
		runner.cancel();
		Point size = comp.getSize();
		int startX = (int) (size.x * startWK);
		int startY = (int) (size.y * startHK);
		final int endX = (int) (size.x * endWK);
		final int endY = (int) (size.y * endHK);
		comp.setLocation(startX, startY);
		comp.moveAbove(null); // show control
		Runnable onStop = new Runnable() {
			@Override
			public void run() {
				if (comp.isDisposed())
					return;
				IEffect effect = runner.getEffect();
				effect.doEffect(effect.getLength());
				comp.moveAbove(null); // show control
				comp.setFocus();
				// comp.getParent().layout(true, true);
			}
		};
		IEffect effect1 = new MoveControlEffect(comp, startX, endX, startY, endY, effectTime, movement, onStop, onStop);
		runner.runEffect(effect1);
	}

	public void pushControl(final Control comp, int horDir, int verDir) {
		pushControl(comp, horDir, verDir, 0, 0);
	}

	public void popControl(final Control comp, float horDir, float verDir) {
		runner.cancel();
		final int endX = (int) (comp.getSize().x * horDir);
		final int endY = (int) (comp.getSize().y * verDir);
		comp.moveAbove(null); // show control
		Runnable onStop = new Runnable() {
			@Override
			public void run() {
				if (comp.isDisposed())
					return;
				IEffect effect = runner.getEffect();
				effect.doEffect(effect.getLength());
				comp.moveBelow(null); // hide control
				// comp.getParent().layout(true, true);
			}
		};
		// comp.setLocation(width / 2, 0);
		IEffect effect1 = new MoveControlEffect(comp, 0, endX, 0, endY, effectTime, movement, onStop, onStop);
		runner.runEffect(effect1);
	}

	public static void main(String[] args) throws InterruptedException {
		new SlidingPaneAnimation().runExample();
	}

	public void waitForAnimation() {
		Display display = Display.getCurrent();
		if (display != null)
			while (!runner.getEffect().isDone()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		else
			while (!runner.getEffect().isDone()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
	}
}
