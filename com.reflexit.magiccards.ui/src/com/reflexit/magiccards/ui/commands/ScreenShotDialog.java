package com.reflexit.magiccards.ui.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.MagicUIActivator;

public final class ScreenShotDialog extends Dialog implements Listener {
	private final Image imageOrig;
	private Image image;
	private IDialogSettings dialogSettings;
	private boolean drag = false;
	private Rectangle selection;
	private int endX;
	private int endY;
	private Canvas canvas;

	private enum Mode {
		DRAG,
		PANE
	};

	private Mode mode = Mode.DRAG;

	public ScreenShotDialog(Shell parentShell, Image image) {
		super(parentShell);
		try {
			dialogSettings = MagicUIActivator.getDefault().getDialogSettings("screenShot");
		} catch (Exception e) {
			dialogSettings = new DialogSettings("screenShot");
		}
		this.imageOrig = image;
		this.image = new Image(parentShell.getDisplay(), imageOrig.getImageData());
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	public boolean close() {
		image.dispose();
		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		canvas = new Canvas(parent, SWT.BORDER);
		setUpCanvas();
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		Rectangle bounds = image.getBounds();
		layoutData.widthHint = bounds.width;
		layoutData.heightHint = bounds.height;
		canvas.setLayoutData(layoutData);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 3, "Save As...", true);
		createButton(parent, 4, "Crop", false);
		createButton(parent, 5, "600x400", false);
		createButton(parent, 6, "Restore", false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 3) { // save
			saveToClipboard(image);
			String fileStr = dialogSettings.get("file");
			File defaultFile = fileStr == null ? null : new File(fileStr);
			File file = getFile(getShell(), defaultFile);
			if (file != null) {
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] { image.getImageData() };
				loader.save(file.getPath(), SWT.IMAGE_PNG);
				dialogSettings.put("file", file.getPath());
				try {
					MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
				} catch (IOException e) {
					MagicUIActivator.log(e);
				}
				super.okPressed();
			}
			return;
		} else if (buttonId == 4) { // crop
			Rectangle selection = getSelection();
			if (selection.isEmpty()) {
				MessageDialog.openError(getShell(), "Error",
						"Nothing is selected. Use mouse to select a region to crop.");
				return;
			}
			Image image1 = new Image(getShell().getDisplay(), selection.width, selection.height);
			GC gc = new GC(image1);
			gc.drawImage(image,
					selection.x, selection.y,
					selection.width, selection.height,
					0, 0,
					selection.width, selection.height);
			gc.dispose();
			// image.dispose();
			image = image1;
			GridData layoutData = new GridData(GridData.FILL_BOTH);
			Rectangle bounds = image.getBounds();
			layoutData.widthHint = bounds.width;
			layoutData.heightHint = bounds.height;
			canvas.setLayoutData(layoutData);
			setEmptySelection();
			mode = Mode.DRAG;
			getShell().pack(true);
		} else if (buttonId == 5) { // frame
			selection.width = 600;
			selection.height = 400;
			mode = Mode.PANE;
			canvas.redraw();
		} else if (buttonId == 6) { // undo
			this.image = new Image(getShell().getDisplay(), imageOrig.getImageData());
			setEmptySelection();
			mode = Mode.DRAG;
			canvas.redraw();
		} else if (IDialogConstants.OK_ID == buttonId) {
			// save to clipboard
			saveToClipboard(image);
		}
		super.buttonPressed(buttonId);
	}

	public void setEmptySelection() {
		selection.x = 0;
		selection.y = 0;
		selection.width = 0;
		selection.height = 0;
		endX = 0;
		endY = 0;
	}

	public void saveToClipboard(final Image image) {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		ImageTransfer imageTransfer = ImageTransfer.getInstance();
		clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[] { imageTransfer });
	}

	public Rectangle getSelection() {
		return selection;
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
			case SWT.MouseDown:
				if (mode == Mode.DRAG) {
					selection.x = event.x;
					selection.y = event.y;
					selection.width = 0;
					selection.height = 0;
				}
				endX = event.x;
				endY = event.y;
				drag = true;
				break;
			case SWT.MouseUp:
				if (drag) {
					drag = false;
					onMove(event);
				}
				break;
			case SWT.MouseMove:
				if (drag) {
					onMove(event);
				}
				break;
			default:
				break;
		}
	}

	public void onMove(Event event) {
		int offX = event.x - endX;
		int offY = event.y - endY;
		endX = event.x;
		endY = event.y;
		switch (mode) {
			case DRAG:
				selection.width += offX;
				selection.height += offY;
				if (selection.width < 0)
					selection.width = 0;
				if (selection.height < 0)
					selection.height = 0;
				break;
			case PANE:
				selection.x += offX;
				selection.y += offY;
				Rectangle bounds = image.getBounds();
				if (selection.x < 0)
					selection.x = 0;
				else if (selection.x + selection.width >= bounds.width)
					selection.x = bounds.width - 1 - selection.width;
				if (selection.y < 0)
					selection.y = 0;
				else if (selection.y + selection.height >= bounds.height)
					selection.y = bounds.height - 1 - selection.height;
				break;
		}
		canvas.redraw();
	}

	public void setUpCanvas() {
		selection = new Rectangle(0, 0, 0, 0);
		canvas.addListener(SWT.MouseDown, this);
		canvas.addListener(SWT.MouseUp, this);
		canvas.addListener(SWT.MouseMove, this);
		canvas.addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(Event e) {
				// draw original image
				e.gc.drawImage(image, 0, 0);
				if (getSelection().isEmpty() && !drag)
					return;
				Rectangle bounds = image.getBounds();
				GC gc = e.gc;
				// draw black on it with transparency to dim it
				gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				gc.setAlpha(64);
				gc.fillRectangle(0, 0, bounds.width, bounds.height);
				// draw selected image region with full color
				gc.setAlpha(255);
				gc.drawImage(image,
						selection.x, selection.y,
						selection.width, selection.height,
						selection.x, selection.y,
						selection.width, selection.height);
				// gc.fillRectangle(minX, minY, width, height);
			}
		});
	}

	private File getFile(Shell shell, File defaultFile) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE | SWT.SHEET);
		if (defaultFile != null) {
			dialog.setFileName(defaultFile.getPath());
			dialog.setFilterPath(defaultFile.getParent());
		}
		dialog.setFilterExtensions(new String[] { "*.png" });
		String file = dialog.open();
		if (file != null) {
			file = file.trim();
			if (file.length() > 0) {
				return new File(file);
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Drawing Example");
		final Shell control = shell;
		final Rectangle bounds = control.getClientArea();
		GC gc = new GC(control);
		final Image image = new Image(control.getDisplay(), bounds.width, bounds.height);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		new ScreenShotDialog(shell, image).open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}