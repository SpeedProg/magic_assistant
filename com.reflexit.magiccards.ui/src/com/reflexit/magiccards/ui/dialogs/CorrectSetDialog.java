package com.reflexit.magiccards.ui.dialogs;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.model.Editions;

public class CorrectSetDialog extends TrayDialog {
	public static final String NEW = "Create New Set";
	public static final String SKIP = "Skip Import";
	private String set;
	private Combo combo;
	private String initialSet;

	public CorrectSetDialog(Shell shell, String set, String selSet) {
		super(shell);
		this.setSet(set);
		this.initialSet = selSet;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Correct the set name");
		Composite area = (Composite) super.createDialogArea(parent);
		Label label = new Label(area, SWT.NONE);
		label.setText("Cannot find set: \"" + getSet() + "\"");
		Label label2 = new Label(area, SWT.NONE);
		label2.setText("Select set from the list (leave empty to create new set):");
		combo = new Combo(area, SWT.DROP_DOWN);
		Collection<String> names1 = Editions.getInstance().getNames();
		combo.add(NEW);
		combo.add(SKIP);
		for (Iterator<String> iterator = names1.iterator(); iterator.hasNext();) {
			String x = iterator.next();
			combo.add(x);
		}
		if (initialSet == null)
			combo.select(0);
		else {
			combo.setText(initialSet);
		}
		return area;
	}

	@Override
	protected void okPressed() {
		String nset = combo.getText();
		if (nset.length() > 0) {
			if (nset.equals(NEW)) {
				// .. create new set
				NewSetDialog newdia = new NewSetDialog(getShell(), set);
				if (newdia.open() == Window.OK) {
					nset = newdia.getSet().getName();
				} else {
					nset = CorrectSetDialog.SKIP;
					combo.setText(nset);
					return;
				}
			}
			combo.setText(nset);
			set = nset;
		}
		super.okPressed();
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}
}
