package com.reflexit.magicassistant.swtbot.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.lang.reflect.Method;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotWorkbenchPart;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * A dnd utility class to help performing drag and drop. This code will be in SWTbot in future but
 * need to be polished before.
 * 
 * @author mchauvin
 */
public class DndUtil {
	/**
	 * Number of pixels traversed before a drag starts. Ms : 10 OSX : 10(1.3.1), 5(1.4.1) Linux/X11:
	 * delay+16
	 */
	private static final int DRAG_THRESHOLD = "gtk".equals(SWT.getPlatform()) ? 16 : 10;
	/**
	 * delay before moving after mouse is pressed. could be used for delay after moving before mouse
	 * is released
	 */
	private static final int DRAG_DELAY = 400;
	private Display display;

	/**
	 * Construct a new instance.
	 * 
	 * @param display
	 *            the display to use
	 */
	public DndUtil(Display display) {
		this.display = display;
	}

	/**
	 * Performs a drag and drop operation from this widget to the given target. The drag start
	 * location will be chosen depending on this widget's default implementation.
	 * 
	 * @param source
	 *            the source widget to drag
	 * @param target
	 *            To perform the drop on
	 * @see #dragAndDrop(Point)
	 */
	// TODO add modifier key mask
	public void dragAndDrop(final AbstractSWTBot<? extends Widget> source, final AbstractSWTBot<? extends Widget> target) {
		dragAndDrop(source, on(target));
	}

	public void dragAndDrop(final AbstractSWTBot<? extends Widget> source, final SWTBotView target) {
		Control con = (Control) callPrivateMethod(SWTBotWorkbenchPart.class, "getControl", target);
		Rectangle abs = absoluteLocation(con);
		dragAndDrop(source, Geometry.centerPoint(abs));
	}

	protected Rectangle absoluteLocation(final Control control) {
		final Rectangle result[] = new Rectangle[] { null };
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				result[0] = display.map(control.getParent(), null, control.getBounds());
			}
		});
		return result[0];
	}

	/**
	 * Performs a drag and drop operation from this widget to the given target at the given location
	 * from target origin. The drag start location will be chosen depending on this widget's default
	 * implementation.
	 * 
	 * @param source
	 *            the source widget to drag
	 * @param target
	 *            To perform the drop on
	 * @param locationOnTarget
	 *            The target locations, from target origin, where the DND shall finish.
	 * @see #dragAndDrop(Point)
	 */
	public void dragAndDrop(final AbstractSWTBot<? extends Widget> source, final AbstractSWTBot<? extends Widget> target,
			final Point locationOnTarget) {
		Rectangle targetRectangle = absoluteLocation(target);
		Point dropTarget = new Point(targetRectangle.x + locationOnTarget.x, targetRectangle.y + locationOnTarget.y);
		dragAndDrop(source, dropTarget);
	}

	/**
	 * Performs a DND operation to an arbitrary location. The drag start location will be chosen
	 * depending on this widget's default implementation.
	 * 
	 * @param source
	 *            the source widget to drag
	 * @param target
	 *            The target locations where the DND shall finish.
	 * @see #before(AbstractSWTBot)
	 * @see #on(AbstractSWTBot)
	 * @see #after(AbstractSWTBot)
	 */
	public void dragAndDrop(final AbstractSWTBot<? extends Widget> source, final Point target) {
		// Rectangle sourceBounds = getBounds(source);
		final Rectangle sourceLocation = absoluteLocation(source);
		final Point slightOffset = Geometry.add(Geometry.getLocation(sourceLocation), new Point(DRAG_THRESHOLD, DRAG_THRESHOLD));
		// System.err.println(sourceBounds);
		System.err.println(sourceLocation);
		Point sourceCenter = Geometry.centerPoint(sourceLocation);
		System.err.println(sourceCenter);
		System.err.println(target);
		doDragAndDrop(sourceCenter, target);
	}

	/**
	 * Performs a DND operation starting at an arbitrary location, targeting the given widget.
	 * 
	 * @param source
	 *            From where to start dragging
	 * @param target
	 *            Where to drop onto
	 * @see #dragAndDrop(AbstractSWTBot)
	 * @see #dragAndDrop(Point)
	 */
	protected void dragAndDrop(final Point source, final AbstractSWTBot<? extends Widget> target) {
		doDragAndDrop(source, on(target));
	}

	/**
        *
        */
	private void doDragAndDrop(final Point source, final Point dest) {
		// log.debug(MessageFormat.format("Drag-and-dropping from ({0},{1}) to ({2},{3})",
		// source.x, source.y, dest.x, dest.y));
		try {
			final Robot awtRobot = new Robot();
			awtRobot.setAutoDelay(DRAG_DELAY);
			// the x+10 motion is needed to let native functions register a drag
			// detect. It did not work under Windows
			// otherwise and has been reported to be required for linux, too.
			// But I could not test that.
			syncExec(new VoidResult() {
				@Override
				public void run() {
					awtRobot.mouseMove(source.x, source.y);
					awtRobot.mousePress(InputEvent.BUTTON1_MASK);
					awtRobot.mouseMove(source.x + DRAG_THRESHOLD, source.y);
				}
			});
			/* drag delay */
			SWTUtils.sleep(DRAG_DELAY);
			syncExec(new VoidResult() {
				@Override
				public void run() {
					awtRobot.mouseMove(dest.x + DRAG_THRESHOLD, dest.y);
					awtRobot.mouseMove(dest.x, dest.y);
				}
			});
			/* drop delay */
			SWTUtils.sleep(DRAG_DELAY);
			syncExec(new VoidResult() {
				@Override
				public void run() {
					awtRobot.mouseRelease(InputEvent.BUTTON1_MASK);
				}
			});
		} catch (final AWTException e) {
			// log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes {@link VoidResult#run()} on the UI thread.
	 * 
	 * @param toExecute
	 *            the object to be invoked in the UI thread.
	 */
	private void syncExec(VoidResult toExecute) {
		UIThreadRunnable.syncExec(display, toExecute);
	}

	/**
	 * Calculates a position which can be used to insert an item <em>before</em> the given one by a
	 * DND operation.
	 * 
	 * @param targetItem
	 *            Before which the new item shall appear
	 * @param <T>
	 *            .
	 * @return A point suitable to drop an item before {@code targetItem}
	 * @see #dragAndDrop(Point)
	 * @see DND#FEEDBACK_INSERT_BEFORE
	 */
	public static <T extends Widget> Point before(final AbstractSWTBot<T> targetItem) {
		return pointOnUpperBorder(absoluteLocation(targetItem));
	}

	/**
	 * Calculates a position which can be used to drop something <em>onto</em> the given widget by a
	 * DND operation. For tree structures, this will most likely result in another child node being
	 * added. But how this is handled in detail ultimately depends on the "drop action"'s
	 * implementation.
	 * 
	 * @param targetItem
	 *            On which to drop
	 * @param <T>
	 *            .
	 * @return The {@code targetItem}'s center
	 * @see #dragAndDrop(Point)
	 * @see DND#FEEDBACK_SELECT
	 */
	public static <T extends Widget> Point on(final AbstractSWTBot<T> targetItem) {
		return Geometry.centerPoint(absoluteLocation(targetItem));
	}

	/**
	 * Calculates a position which can be used to insert an item after the given one by a DND
	 * operation.
	 * 
	 * @param targetItem
	 *            After which the new item shall appear
	 * @param <T>
	 *            .
	 * @return A point suitable to drop an item after {@code targetItem}
	 * @see #dragAndDrop(Point)
	 * @see DND#FEEDBACK_INSERT_AFTER
	 */
	public static <T extends Widget> Point after(final AbstractSWTBot<T> targetItem) {
		return pointOnLowerBorder(absoluteLocation(targetItem));
	}

	private static Point pointOnLowerBorder(Rectangle rect) {
		return new Point(Geometry.centerPoint(rect).x, rect.y + rect.height - 1);
	}

	private static Point pointOnUpperBorder(Rectangle rect) {
		return new Point(Geometry.centerPoint(rect).x, rect.y + 1);
	}

	private static <T extends Widget> Rectangle absoluteLocation(final AbstractSWTBot<T> item) {
		// return absoluteLocation(item.getW)
		AbstractSWTBot<?> bot = null;
		if (item instanceof SWTBotTreeItem) {
			bot = new SWTBotTreeItemForDnd(((SWTBotTreeItem) item).widget);
		} else if (item instanceof SWTBotTableItem) {
			bot = new SWTBotTableItemForDnd(((SWTBotTableItem) item).widget);
		} else {
			bot = item;
		}
		Object result = callPrivateMethod(AbstractSWTBot.class, "absoluteLocation", bot);
		return (Rectangle) result;
	}

	public static Object callPrivateMethod(Class cl, String method, Object o, Object... args) {
		Object result = null;
		try {
			Method m = cl.getDeclaredMethod(method);
			m.setAccessible(true);
			result = m.invoke(o, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Subclass to return the correct absolute location.
	 * 
	 * @author mchauvin
	 */
	private static class SWTBotTreeItemForDnd extends SWTBotTreeItem {
		public SWTBotTreeItemForDnd(TreeItem treeItem) throws WidgetNotFoundException {
			super(treeItem);
		}

		@Override
		protected Rectangle absoluteLocation() {
			return UIThreadRunnable.syncExec(new Result<Rectangle>() {
				@Override
				public Rectangle run() {
					return display.map(widget.getParent(), null, widget.getBounds());
				}
			});

		}
	}

	private static class SWTBotTableItemForDnd extends SWTBotTableItem {
		public SWTBotTableItemForDnd(TableItem treeItem) throws WidgetNotFoundException {
			super(treeItem);
		}

		@Override
		protected Rectangle absoluteLocation() {
			return UIThreadRunnable.syncExec(new Result<Rectangle>() {
				@Override
				public Rectangle run() {
					Table table = widget.getParent();
					Rectangle rectangle = widget.getBounds();
					int h = 0;
					if (table.getHeaderVisible()) {
						h = table.getHeaderHeight();
					}
					return display.map(widget.getParent(), null, rectangle.x, rectangle.y + h, rectangle.width,
							rectangle.height);
				}
			});
		}
	}
}