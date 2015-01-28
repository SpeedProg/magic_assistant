package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class TakeScreenShotCommandHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public TakeScreenShotCommandHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Display display = Display.getDefault();
		final Shell shell = display.getActiveShell();
		final Shell control = shell != null ? shell : window.getShell();
		final Rectangle bounds = control.getClientArea();
		GC gc = new GC(control);
		final Image image = new Image(control.getDisplay(), bounds.width, bounds.height);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		new ScreenShotDialog(control.getShell(), image).open();
		image.dispose();
		return null;
	}
}
