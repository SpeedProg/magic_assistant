package com.reflexit.magiccards.ui.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
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
	private int totalSize;
	private int filSize;
	private int selSize;
	public static final int USE_SELECTION = 1;
	public static final int USE_FILTER = 2;
	public static final int USE_ALL = 3;

	public LoadExtrasDialog(Shell parentShell, int selSize, int filSize, int totalSize) {
		super(parentShell);
		this.selSize = selSize;
		this.filSize = filSize;
		this.totalSize = totalSize;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Extra Card Fields");
		if (selSize > 0 && selSize != filSize)
			listChoice = USE_SELECTION;
		else if (filSize != totalSize)
			listChoice = USE_FILTER;
		else
			listChoice = USE_ALL;
		String cards = "";
		if (USE_SELECTION == listChoice)
			cards += "Selected " + selSize + " cards. ";
		if (USE_FILTER == listChoice)
			cards += "Visible " + filSize + " out of unique " + totalSize + ".";
		else
			cards += "Total of " + totalSize + " unique cards.";
		setTitle("Load Extra Card Fields...");
		setMessage("Choose which fields to load or update " + cards);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite panel = new Composite(area, SWT.NONE);
		panel.setLayout(new GridLayout(1, false));
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonGridData = GridDataFactory.fillDefaults();
		createListChoiceGroup(panel);
		createFieldsGroup(panel);
		return area;
	}

	protected void createFieldsGroup(Composite panel) {
		Composite checkArea = new Composite(panel, SWT.BORDER);
		checkArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout la = new GridLayout();
		checkArea.setLayout(la);
		la.marginHeight = 0;
		la.marginWidth = 0;
		buttons = new Composite(checkArea, SWT.NONE);
		buttons.setLayout(new GridLayout(4, true));
		buttons.setLayoutData(new GridData(GridData.FILL_BOTH));
		createFieldCheck("Rulings", MagicCardField.RULINGS);
		createFieldCheck("Artist", MagicCardField.ARTIST);
		createFieldCheck("Rating", MagicCardField.RATING);
		createFieldCheck("Collector's Number", MagicCardField.COLLNUM);
		createFieldCheck("Oracle Text", MagicCardField.ORACLE);
		createFieldCheck("Image", MagicCardField.ID);
		createFieldCheck("Price", MagicCardField.DBPRICE);
		createSelectAllButtons(checkArea);
	}

	protected void createListChoiceGroup(Composite panel) {
		Composite group = panel;
		// Group group = new Group(panel, SWT.NONE);
		// group.setLayout(new GridLayout());
		// group.setLayoutData(new GridData(GridData.FILL_BOTH));
		Button b1 = createRadioButton(group, USE_SELECTION, "Update selected cards", selSize);
		Button b2 = createRadioButton(group, USE_FILTER, "Update only cards in filtered list", filSize);
		Button b3 = createRadioButton(group, USE_ALL, "Update all cards in the collection", totalSize);
		if (filSize == selSize) {
			b1.setEnabled(false);
		}
		if (filSize == totalSize) {
			b2.setEnabled(false);
		}
	}

	private int listChoice;

	public int getListChoice() {
		return listChoice;
	}

	private Button createRadioButton(Composite group, int i, String text, int size) {
		final Button button = new Button(group, SWT.RADIO);
		button.setText(text + (size > 0 ? (" (" + size + " cards)") : ""));
		button.setData(i);
		button.setSelection(i == listChoice);
		if (size == 0) {
			button.setEnabled(false);
		}
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					listChoice = (Integer) button.getData();
				}
			}
		});
		buttonGridData.applyTo(button);
		return button;
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

	public Set<ICardField> getFields() {
		return selectedSet;
	}
}
