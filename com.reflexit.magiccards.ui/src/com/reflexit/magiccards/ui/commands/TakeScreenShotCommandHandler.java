package com.reflexit.magiccards.ui.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.ui.MagicUIActivator;

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
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
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
		new Dialog(control.getShell()) {
			private final IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings("screenShot");
			{
				setShellStyle(getShellStyle() | SWT.RESIZE);
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
				Label lab = new Label(composite, SWT.BORDER);
				lab.setImage(image);
				GridData layoutData = new GridData(GridData.FILL_BOTH);
				layoutData.widthHint = bounds.width;
				layoutData.heightHint = bounds.height;
				lab.setLayoutData(layoutData);
				return composite;
			}

			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				createButton(parent, 3, "Save As...", true);
				super.createButtonsForButtonBar(parent);
			};

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
			};
		}.open();
		image.dispose();
		return null;
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
}
