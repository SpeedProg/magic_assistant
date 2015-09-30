package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class EditTextDialog extends TitleAreaDialog {
	private Text textControl;
	private String contents = "";

	public EditTextDialog(IShellProvider parentShell) {
		super(parentShell.getShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Edit Text");
		setMessage("Type text here (or paste from other source)");
		Composite area = (Composite) super.createDialogArea(parent);
		textControl = new Text(area, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP | SWT.BORDER);
		textControl.setLayoutData(GridDataFactory.fillDefaults().hint(600, 400).create());
		textControl.setText(contents);

		return area;
	}

	public void setContents(String contents) {
		this.contents = contents;
		if (textControl != null)
			textControl.setText(contents);
	}

	@Override
	protected void okPressed() {
		contents = textControl.getText();
		super.okPressed();
	}

	public String getText() {
		return contents;
	}
}
