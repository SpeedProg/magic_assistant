package com.reflexit.magicassistant.swtbot.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * This class contains method to send raw events, such as keyboard and mouse events. The problem is
 * you need to know precise coordinates where you send events, and for keyboard focus have to be in
 * right location.
 */
public class SWTKeyboardAndMouseUtils {
	public Display display;

	public SWTKeyboardAndMouseUtils(Display display) {
		super();
		this.display = display;
	}

	static void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public void mouseClick(final Point pt) {
		mouseClick(pt, 1);
	}

	public void mouseRightClick(final Point pt) {
		mouseClick(pt, 3);
	}

	public void mouseClick(final Point pt, final int button) {
		new Thread() {
			@Override
			public void run() {
				postMouseClick(pt, button);
			}
		}.start();
	}

	/**
	 * Send key events as text in string would be typed
	 * 
	 * @param string
	 */
	public void typeText(final String string) {
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < string.length(); i++) {
					char ch = string.charAt(i);
					boolean shift = Character.isUpperCase(ch);
					ch = Character.toLowerCase(ch);
					if (shift) {
						Event event = new Event();
						event.type = SWT.KeyDown;
						event.keyCode = SWT.SHIFT;
						display.post(event);
					}
					Event event = new Event();
					event.type = SWT.KeyDown;
					event.character = ch;
					display.post(event);
					pause(10);
					event.type = SWT.KeyUp;
					display.post(event);
					pause(100);
					if (shift) {
						event = new Event();
						event.type = SWT.KeyUp;
						event.keyCode = SWT.SHIFT;
						display.post(event);
					}
				}
			}
		}.start();
	}

	public void keyClick(final char c) {
		final char ch = Character.toLowerCase(c);
		new Thread() {
			@Override
			public void run() {
				Event event = new Event();
				event.type = SWT.KeyDown;
				event.character = ch;
				display.post(event);
				pause(10);
				event.type = SWT.KeyUp;
				display.post(event);
				pause(100);
			}
		}.start();
	}

	public void keyDown(final char c) {
		final char ch = Character.toLowerCase(c);
		new Thread() {
			@Override
			public void run() {
				Event event = new Event();
				event.type = SWT.KeyDown;
				event.character = ch;
				display.post(event);
				pause(10);
			}
		}.start();
	}

	public void keyUp(final char c) {
		final char ch = Character.toLowerCase(c);
		new Thread() {
			@Override
			public void run() {
				Event event = new Event();
				event.type = SWT.KeyUp;
				event.character = ch;
				display.post(event);
				pause(10);
			}
		}.start();
	}

	/**
	 * @param keyCode
	 *            - SWT constant such as SWT.CTRL
	 */
	public void keyCodeDown(final int keyCode) {
		new Thread() {
			@Override
			public void run() {
				Event event = new Event();
				event.type = SWT.KeyDown;
				event.keyCode = keyCode;
				display.post(event);
				pause(10);
			}
		}.start();
	}

	/**
	 * @param keyCode
	 *            - SWT constant such as SWT.CTRL
	 */
	public void keyCodeUp(final int keyCode) {
		new Thread() {
			@Override
			public void run() {
				Event event = new Event();
				event.type = SWT.KeyUp;
				event.keyCode = keyCode;
				display.post(event);
				pause(10);
			}
		}.start();
	}

	/**
	 * Ctrl+key
	 * 
	 * @param c
	 */
	public void ctrlKeyClick(final char c) {
		modKeyClick(SWT.CTRL, c);
	}

	/**
	 * Emulates pressing key with modifier key, modKeyCode is SWT constants: SWT.CTRL, SWT.SHIFT,
	 * etc.
	 */
	public void modKeyClick(final int modKeyCode, final char c) {
		final char ch = Character.toLowerCase(c);
		new Thread() {
			@Override
			public void run() {
				boolean mod = true;
				if (mod) {
					Event event = new Event();
					event.type = SWT.KeyDown;
					event.keyCode = modKeyCode;
					display.post(event);
				}
				Event event = new Event();
				event.type = SWT.KeyDown;
				event.character = ch;
				display.post(event);
				pause(10);
				event.type = SWT.KeyUp;
				display.post(event);
				pause(100);
				if (mod) {
					event = new Event();
					event.type = SWT.KeyUp;
					event.keyCode = modKeyCode;
					display.post(event);
				}
			}
		}.start();
	}

	public static Event mouseDownEvent(int button) {
		return mouseEvent(button, SWT.MouseDown);
	}

	public static Event mouseUpEvent(int button) {
		return mouseEvent(button, SWT.MouseUp);
	}

	private static Event mouseEvent(int button, int type) {
		Event e = new Event();
		e.button = button;
		e.type = type;
		return e;
	}

	public static Event mouseMove(int x, int y) {
		Event e = new Event();
		e.x = x;
		e.y = y;
		e.type = SWT.MouseMove;
		return e;
	}

	public void mouseDrag(final Point start, final Point end) {
		new Thread() {
			@Override
			public void run() {
				postMouseDrag(start, end);
			}
		}.start();
	}

	/**
	 * @param pointStart
	 * @param pointEnd
	 */
	private void postMouseDrag(Point pointStart, Point pointEnd) {
		Display d = display;
		d.post(mouseMove(pointStart.x, pointStart.y));
		pause(10);
		d.post(mouseDownEvent(1));
		pause(100);
		d.post(mouseMove(pointEnd.x, pointEnd.y));
		pause(100);
		d.post(mouseUpEvent(1));
		pause(100);
	}

	private void postMouseClick(final Point pt, final int button) {
		display.post(mouseMove(pt.x, pt.y));
		display.post(mouseDownEvent(button));
		display.post(mouseUpEvent(button));
	}

	public void postMouseDown(final int button) {
		display.post(mouseDownEvent(button));
	}

	public void postMouseUp(final int button) {
		display.post(mouseUpEvent(button));
	}

	public void postMouseMove(int x, int y) {
		display.post(mouseMove(x, y));
	}

	/**
	 * @param shift
	 */
	public void postModKeyClick(int code) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = code;
		display.post(event);
		event.type = SWT.KeyUp;
		display.post(event);
	}
}
