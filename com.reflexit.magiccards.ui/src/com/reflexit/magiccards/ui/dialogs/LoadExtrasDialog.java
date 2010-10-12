package com.reflexit.magiccards.ui.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;

public class LoadExtrasDialog extends TitleAreaDialog {
	private Set<ICardField> selectedSet = new HashSet<ICardField>();
	private GridDataFactory buttonGridData;
	private Composite buttons;

	/**
	 * @param parentShell
	 * @param max
	 */
	public LoadExtrasDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Extra Card Fields");
		setTitle("Load Extra Card Fields...");
		setMessage("Choose which fields to load or update");
		Composite area = (Composite) super.createDialogArea(parent);
		buttons = new Composite(area, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttonGridData = GridDataFactory.fillDefaults().span(2, 1);
		createFieldCheck("Rulings", MagicCardField.RULINGS);
		createFieldCheck("Artist", MagicCardField.ARTIST);
		createFieldCheck("Rating", MagicCardField.RATING);
		createFieldCheck("Collector's Number", MagicCardField.COLLNUM);
		createFieldCheck("Oracle Text", MagicCardField.ORACLE);
		createFieldCheck("Image", MagicCardField.ID);
		return area;
	}

	protected void createFieldCheck(String name, final ICardField field) {
		final Button button = new Button(buttons, SWT.CHECK);
		button.setText(name);
		button.setData(field);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					selectedSet.add(field);
				} else {
					selectedSet.remove(field);
				}
			}
		});
		button.setSelection(true);
		selectedSet.add(field);
		buttonGridData.applyTo(button);
	}

	public Set<ICardField> getFieldMap() {
		return selectedSet;
	}
}
