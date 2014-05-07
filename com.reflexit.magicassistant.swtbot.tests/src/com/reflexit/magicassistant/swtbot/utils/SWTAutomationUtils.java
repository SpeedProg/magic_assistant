package com.reflexit.magicassistant.swtbot.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Collections for utils to assist automatic junit tests of eclipse ui plugins
 * 
 */
public class SWTAutomationUtils {
	/**
	 * Widget would notify listeners that event of type 'type' happened with widget being the source
	 * of the event.
	 * 
	 * @param w
	 *            - source widget
	 * @param type
	 *            - event type,
	 * @see org.eclipse.swt.SWT for types
	 */
	public static void notifyListeners(final Widget widget, int type) {
		final Display d = widget.getDisplay();
		final Event e = new Event();
		e.type = type;
		e.widget = widget;
		e.display = d;
		d.asyncExec(new Runnable() {
			public void run() {
				widget.notifyListeners(e.type, e);
			}
		});
	}

	/**
	 * Use data object to set selection in the given viewer. Data object is usually what is kept by
	 * TreeItem.getData(), not TreeItem itself. This only applicable for structural viewers (tree,
	 * list).
	 * 
	 * @param element
	 * @param viewer
	 */
	public static void setSelection(Object element, StructuredViewer viewer) {
		IStructuredSelection sel = new StructuredSelection(element);
		viewer.setSelection(sel, true);
		processQueuedDisplayEvents(viewer.getControl().getDisplay());
	}

	/**
	 * This would open given menu at given position.
	 * 
	 * @param menu
	 * @param pop
	 * @return
	 */
	public static Menu emulatePopUp(Menu menu, Point pop) {
		if (pop != null)
			menu.setLocation(pop);
		menu.setVisible(true);
		menu.notifyListeners(SWT.Show, null);
		menu.setVisible(false);
		processQueuedDisplayEvents(menu.getDisplay());
		return menu;
	}

	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	public static File getFileInPlugin(Plugin plugin, IPath path) {
		try {
			URL installURL = FileLocator.find(plugin.getBundle(), path, null);
			URL localURL = FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Emulate selecting a menu item from a menu. Most likely menu itself won't be visible when this
	 * is running. This would trigger action associated with menu. Probably should not use on
	 * non-action menu items.
	 * 
	 * @param menu
	 *            - menu to select from
	 * @param name
	 *            - name of the menu item
	 */
	public static void showMenuAndSelectItem(final Menu menu, final String name) {
		String os = System.getProperty("osgi.os");
		if (!os.equals("win32")) {
			menu.setVisible(true);
			processQueuedDisplayEvents(menu.getDisplay());
			selectMenuItem(menu, name);
			processQueuedDisplayEvents(menu.getDisplay());
			menu.setVisible(false);
			processQueuedDisplayEvents(menu.getDisplay());
		} else {
			menu.setVisible(true);
			SWTAutomationUtils.notifyListeners(menu, SWT.Show);
			menu.setVisible(false);
			processQueuedDisplayEvents(menu.getDisplay());
			// postRandomMouseEvent(menu);
			selectMenuItem(menu, name);
			processQueuedDisplayEvents(menu.getDisplay());
			menu.notifyListeners(SWT.Hide, null);
			processQueuedDisplayEvents(menu.getDisplay());
		}
	}

	public static void selectMenuItem(final Menu menu, final String name) {
		MenuItem item = findMenuItem(menu, name);
		if (item == null)
			throw new IllegalArgumentException("Cannot find item " + name + " in the menu");
		item.setSelection(true);
		SWTAutomationUtils.notifyListeners(item, SWT.Selection);
		postRandomMouseEvent(item);
		processQueuedDisplayEvents(item.getDisplay());
	}

	/**
	 * Run events that emulated double click on given widget. Can be menu item, tree item, button,
	 * etc.
	 * 
	 * @param w
	 *            - widget
	 */
	public static void emulateDoubleClick(final Widget w) {
		w.notifyListeners(SWT.DefaultSelection, null);
		SWTAutomationUtils.processQueuedDisplayEvents(w.getDisplay());
	}

	/**
	 * Finds a menu item with given name in the given menu
	 * 
	 * @param menu
	 *            - menu to search
	 * @param name
	 *            - item name
	 * @return menu item or null if not found
	 */
	public static MenuItem findMenuItem(final Menu menu, final String name) {
		MenuItem[] items = menu.getItems();
		if (items.length == 0)
			throw new IllegalArgumentException("Menu is empty");
		MenuItem item = null;
		for (int i = 0; i < items.length; i++) {
			final MenuItem curItem = items[i];
			if (curItem.getText().equals(name)) {
				item = curItem;
				break;
			}
		}
		return item;
	}

	/**
	 * This event need to dispatch listeners, otherwise notifyListeners nevers finishes Note: this
	 * event moves mouse and press mouse button 1 down, it is pretty safe because mouse up does not
	 * follow
	 * 
	 * @param w
	 *            - widget
	 */
	public static void postRandomMouseEvent(Widget w) {
		SWTKeyboardAndMouseUtils rawEvents2 = new SWTKeyboardAndMouseUtils(w.getDisplay());
		rawEvents2.postMouseMove(10, w.getDisplay().getClientArea().height / 2);
		rawEvents2.postMouseDown(1);
	}

	/**
	 * This event need to dispatch listeners, not no wait forever Posts shift key down and up
	 * 
	 * @param display
	 * 
	 * @param w
	 *            - widget
	 */
	public static void postRandomKeyboadEvent(Display display) {
		SWTKeyboardAndMouseUtils rawEvents2 = new SWTKeyboardAndMouseUtils(display);
		rawEvents2.postModKeyClick(SWT.SHIFT);
	}

	public static void processQueuedDisplayEvents(Display display) {
		while (true) {
			if (!display.readAndDispatch())
				break;
		}
	}

	public static void runWithProgress(final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		IProgressService service = PlatformUI.getWorkbench().getProgressService();
		service.busyCursorWhile(runnable);
	}

	/**
	 * Running ui read and dispatch loop for at most sec seconds. That allow ui to redraw and to
	 * perform async actions.
	 */
	public static void uiSleepSeconds(int sec) {
		uiSleepMillis(sec * 1000, getDisplay());
	}

	/**
	 * Dispatch ui events for at least msec - milliseconds
	 * 
	 * @param msec
	 *            - milliseconds delay
	 * @param display
	 *            - display that dispatches events
	 */
	public static void uiSleepMillis(int msec, Display display) {
		long cur = System.currentTimeMillis();
		long pass = 0;
		while (pass < msec) {
			if (!display.readAndDispatch())
				display.sleep();
			pass = System.currentTimeMillis() - cur;
		}
	}

	/**
	 * This is regular thread sleep in seconds,
	 * 
	 * @param sec
	 *            - number of seconds to sleep
	 */
	public static void sleep(int sec) {
		try {
			Thread.sleep(sec * 1000);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	/**
	 * Toolbar action is action in the view toolbar, usually represented by icon.
	 * 
	 * @param view
	 *            - view for which search for toolbar
	 * @param actionName
	 *            - name of the action. It is either in plugin that defines toolbar extensions or in
	 *            the code.
	 * @param time
	 *            - wait time after action is fired to process consequences
	 * @return false if action is not found/not executed
	 */
	public static boolean runViewToolBarAction(IViewPart view, String actionName, int time) {
		IContributionItem[] items = view.getViewSite().getActionBars().getToolBarManager().getItems();
		boolean done = false;
		for (int i = 0; i < items.length; i++) {
			IContributionItem contributionItem = items[i];
			if (contributionItem instanceof ActionContributionItem) {
				ActionContributionItem action = (ActionContributionItem) contributionItem;
				if (action.getAction().getText().equals(actionName)) {
					action.getAction().run();
					done = true;
					break;
				}
			}
		}
		SWTAutomationUtils.processQueuedDisplayEvents(getDisplay());
		SWTAutomationUtils.uiSleepSeconds(time);
		return done;
	}

	/**
	 * Context action is action in Drop Down context menu, usually in tables, trees, etc.
	 * 
	 * @param control
	 *            - control that has the menu
	 * @param actionName
	 *            - string name of the action, usually same as visible in the menu
	 * @param time
	 *            - wait time after action executed (required for ui to process action consequences)
	 * @return if action not found/not executed return false
	 */
	public static boolean runContextAction(Control control, String actionName, int time) {
		try {
			SWTAutomationUtils.showMenuAndSelectItem(control.getMenu(), actionName);
			SWTAutomationUtils.processQueuedDisplayEvents(getDisplay());
			SWTAutomationUtils.uiSleepSeconds(time);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static void hideMenuRec(final Menu menu) {
		menu.notifyListeners(SWT.Hide, new Event());
		menu.setVisible(false);
		if (menu.getParentMenu() != null) {
			hideMenuRec(menu.getParentMenu());
		}
	}
}
