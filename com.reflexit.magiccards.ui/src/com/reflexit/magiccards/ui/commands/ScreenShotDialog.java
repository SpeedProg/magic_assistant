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

public final class ScreenShotDialog extends Dialog {
	private final Image imageOrig;
	private Image image;
	private IDialogSettings dialogSettings;
	private boolean drag = false;
	private Rectangle selection;
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	private Canvas canvas;

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
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 3) {
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
		}
		if (buttonId == 4) { // crop
			Rectangle selection = getSelection();
			if (selection.isEmpty()) {
				MessageDialog.openError(getShell(), "Error", "Nothing is selected. Use mouse to select a region to crop.");
			}
			Image image1 = new Image(getShell().getDisplay(), selection.width, selection.height);
			GC gc = new GC(image1);
			gc.drawImage(image,
					selection.x, selection.y,
					selection.width, selection.height,
					0, 0,
					selection.width, selection.height);
			gc.dispose();
			startX = startY = endX = endY = 0;
			// image.dispose();
			image = image1;
			GridData layoutData = new GridData(GridData.FILL_BOTH);
			Rectangle bounds = image.getBounds();
			layoutData.widthHint = bounds.width;
			layoutData.heightHint = bounds.height;
			canvas.setLayoutData(layoutData);
			getShell().pack(true);
		}
		if (IDialogConstants.OK_ID == buttonId) {
			// save to clipboard
			saveToClipboard(image);
		}
		super.buttonPressed(buttonId);
	}

	public void saveToClipboard(final Image image) {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		ImageTransfer imageTransfer = ImageTransfer.getInstance();
		clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[] { imageTransfer });
	}

	public Rectangle getSelection() {
		int minX = Math.min(startX, endX);
		int minY = Math.min(startY, endY);
		int maxX = Math.max(startX, endX);
		int maxY = Math.max(startY, endY);
		int width = maxX - minX;
		int height = maxY - minY;
		selection.x = minX;
		selection.y = minY;
		selection.width = width;
		selection.height = height;
		return selection;
	}

	public void setUpCanvas() {
		selection = new Rectangle(0, 0, 0, 0);
		canvas.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				startX = event.x;
				startY = event.y;
				drag = true;
			}
		});
		canvas.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				endX = event.x;
				endY = event.y;
				drag = false;
				canvas.redraw();
			}
		});
		canvas.addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (drag) {
					endX = event.x;
					endY = event.y;
					canvas.redraw();
				}
			}
		});
		canvas.addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(Event e) {
				e.gc.drawImage(image, 0, 0);
				if (getSelection().isEmpty() && !drag)
					return;
				Rectangle bounds = image.getBounds();
				GC gc = e.gc;
				gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				gc.setAlpha(64);
				gc.fillRectangle(0, 0, bounds.width, bounds.height);
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