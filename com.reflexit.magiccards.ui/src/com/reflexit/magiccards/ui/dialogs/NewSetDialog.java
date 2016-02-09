package com.reflexit.magiccards.ui.dialogs;

import java.io.FileNotFoundException;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class NewSetDialog extends TitleAreaDialog {
	private String setStart = "";
	private Edition set;
	private Text name;
	private Text abbr;

	public NewSetDialog(Shell shell, String set) {
		super(shell);
		setStart = set;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Create new Set");
		setMessage("Enter name, abbreviation and other info required to create new set");
		Composite area1 = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(area1, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		area.setLayout(new GridLayout(2, false));
		Label label = new Label(area, SWT.NONE);
		label.setText("Name:");
		name = new Text(area, SWT.BORDER);
		name.setTextLimit(50);
		name.setText(setStart);
		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		name.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		Label label2 = new Label(area, SWT.NONE);
		label2.setText("Official Abbreviation:");
		abbr = new Text(area, SWT.BORDER);
		abbr.setTextLimit(5);
		abbr.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		validate();
		return area1;
	}

	protected void validate() {
		setErrorMessage(null);
		if (name.getText().trim().length() == 0) {
			setErrorMessage("Enter set name");
		} else if (abbr.getText().trim().length() == 0) {
			setErrorMessage("Enter main abbreviation");
		}
	}

	@Override
	protected void okPressed() {
		validate();
		if (getErrorMessage() != null)
			return;
		String nset = name.getText();
		Edition set1 = new Edition(nset, abbr.getText());
		if (nset.length() > 0) {
			set = Editions.getInstance().addEdition(set1);
			try {
				Editions.getInstance().save();
			} catch (FileNotFoundException e) {
				MagicUIActivator.log(e);
			}
		}
		super.okPressed();
	}

	public Edition getSet() {
		return set;
	}
}
