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
	private boolean sel;
	private int size;

	/**
	 * @param parentShell
	 * @param iFilteredCardStore
	 * @param iSelection
	 * @param max
	 */
	public LoadExtrasDialog(Shell parentShell, boolean sel, int size) {
		super(parentShell);
		this.sel = sel;
		this.size = size;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Extra Card Fields");
		String cards = sel ? "selected " + size + " cards" : "updating " + size + " cards";
		setTitle("Load Extra Card Fields (" + cards + ")...");
		setMessage("Choose which fields to load or update.");
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
		createSelectAllButtons(area);
		return area;
	}

	private void createSelectAllButtons(Composite parent) {
		Composite sbuttons = new Composite(parent, SWT.NONE);
		sbuttons.setLayout(new GridLayout(2, false));
		final Button buttonSelect = new Button(sbuttons, SWT.PUSH);
		buttonSelect.setText("Select All");
		buttonSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control[] children = buttons.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control control = children[i];
					if (control instanceof Button) {
						ICardField field = (ICardField) control.getData();
						selectedSet.add(field);
						((Button) control).setSelection(true);
					}
				}
			}
		});
		final Button buttondeselect = new Button(sbuttons, SWT.PUSH);
		buttondeselect.setText("Deselect All");
		buttondeselect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control[] children = buttons.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control control = children[i];
					if (control instanceof Button) {
						ICardField field = (ICardField) control.getData();
						selectedSet.remove(field);
						((Button) control).setSelection(false);
					}
				}
			}
		});
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
