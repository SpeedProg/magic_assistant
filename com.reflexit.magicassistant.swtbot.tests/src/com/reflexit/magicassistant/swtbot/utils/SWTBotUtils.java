package com.reflexit.magicassistant.swtbot.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;

/**
 * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
 * 
 */
public class SWTBotUtils {
	/**
	 * SWTWorkbenchBot
	 */
	private static SWTWorkbenchBot bot = new SWTWorkbenchBot();

	/**
	 * Press the enter key
	 * 
	 * @param widget
	 * @throws Exception
	 */
	public static void pressEnterKey(final Widget widget) {
		UIThreadRunnable.asyncExec(new VoidResult() {
			public void run() {
				widget.notifyListeners(SWT.Traverse, keyEvent(SWT.NONE, SWT.CR, SWT.Selection, widget));
			}
		});
	}

	/**
	 * @param c
	 *            the character.
	 * @param modificationKey
	 *            the modification key.
	 * @param keyCode
	 *            the keycode.
	 * @return a key event with the specified keys.
	 * @see Event#keyCode
	 * @see Event#character
	 * @see Event#stateMask
	 * @since 1.2
	 */
	private static Event keyEvent(int modificationKey, char c, int keyCode, Widget widget) {
		Event keyEvent = createEvent(widget);
		keyEvent.stateMask = modificationKey;
		keyEvent.character = c;
		keyEvent.keyCode = keyCode;
		return keyEvent;
	}

	private static Event createEvent(Widget widget) {
		Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = widget;
		event.display = bot.getDisplay();
		return event;
	}

	/**
	 * Create a event <br>
	 * 
	 * @param x
	 *            the x coordinate of the mouse event.
	 * @param y
	 *            the y coordinate of the mouse event.
	 * @param button
	 *            the mouse button that was clicked.
	 * @param stateMask
	 *            the state of the keyboard modifier keys.
	 * @param count
	 *            the number of times the mouse was clicked.
	 * @return an event that encapsulates {@link #widget} and {@link #display}
	 */
	private static Event createEvent(final Widget widget, final int x, final int y, final int button, final int stateMask, final int count) {
		final Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = widget;
		event.display = bot.getDisplay();
		event.x = x;
		event.y = y;
		event.button = button;
		event.stateMask = stateMask;
		event.count = count;
		return event;
	}
}
