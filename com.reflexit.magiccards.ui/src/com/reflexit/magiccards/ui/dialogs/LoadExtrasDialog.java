package com.reflexit.magiccards.ui.dialogs;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.model.Languages;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PriceProviderManager;

public class LoadExtrasDialog extends TitleAreaDialog {
	private Set<ICardField> selectedSet = new HashSet<ICardField>();
	private Set<ICardField> fieldsSet = new HashSet<ICardField>();
	private GridDataFactory buttonGridData;
	private Composite buttons;
	private int totalSize;
	private int filSize;
	private int selSize;
	public static final int USE_SELECTION = 1;
	public static final int USE_FILTER = 2;
	public static final int USE_ALL = 3;
	private static final String ID = LoadExtrasDialog.class.getName();
	private Combo langCombo;
	private Combo priceProviderCombo;

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
		restoreWidgetValues();
		return area;
	}

	private String[] getPriceProviders() {
		PriceProviderManager ppm = PriceProviderManager.getInstance();
		Collection<IPriceProvider> providers = ppm.getProviders();
		String[] res = new String[providers.size()];
		int i = 0;
		for (Iterator iterator = providers.iterator(); iterator.hasNext(); i++) {
			IPriceProviderStore prov = (IPriceProviderStore) iterator.next();
			res[i] = prov.getName();
		}
		return res;
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
		createFieldCheck("Oracle Text", MagicCardField.ORACLE, false);
		createFieldCheck("Printed Text", MagicCardField.TEXT, false);
		createFieldCheck("Image", MagicCardField.ID, false);
		createFieldCheck("Legality", MagicCardField.LEGALITY, false);
		createPriceFields();
		createLangFields();
		createSelectAllButtons(checkArea);
	}

	protected void createLangFields() {
		final Button p = createFieldCheck("Localized version in", MagicCardField.LANG, false);
		langCombo = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		langCombo.setItems(Languages.getLangValues());
		langCombo.setText("English");
		langCombo.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		@SuppressWarnings("unused")
		Label label = new Label(buttons, SWT.NONE); // spacer
		p.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				langCombo.setEnabled(p.getSelection());
			}
		});
		setSelectionAndNotify(p, p.getSelection());
	}

	protected void createPriceFields() {
		final Button p = createFieldCheck("Online Price", MagicCardField.DBPRICE, false);
		priceProviderCombo = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		priceProviderCombo.setItems(getPriceProviders());
		priceProviderCombo.setText(PriceProviderManager.getInstance().getProviderName());
		priceProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = priceProviderCombo.getText();
				if (text.length() > 0)
					PriceProviderManager.getInstance().setProviderName(text);
			}
		});
		priceProviderCombo.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		p.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				priceProviderCombo.setEnabled(p.getSelection());
			}
		});
		setSelectionAndNotify(p, p.getSelection());
		@SuppressWarnings("unused")
		Label label = new Label(buttons, SWT.NONE); // spacer
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
	private String lang;

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
			public void widgetSelected(SelectionEvent se) {
				Control[] children = buttons.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control control = children[i];
					if (control instanceof Button) {
						ICardField field = (ICardField) control.getData();
						selectedSet.add(field);
						setSelectionAndNotify((Button) control, true);
					}
				}
			}
		});
		final Button buttondeselect = new Button(sbuttons, SWT.PUSH);
		buttondeselect.setText("Deselect All");
		buttondeselect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				Control[] children = buttons.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control control = children[i];
					if (control instanceof Button) {
						ICardField field = (ICardField) control.getData();
						selectedSet.remove(field);
						setSelectionAndNotify((Button) control, false);
					}
				}
			}
		});
	}

	protected void createFieldCheck(String name, final ICardField field) {
		createFieldCheck(name, field, true);
	}

	protected Button createFieldCheck(String name, final ICardField field, boolean selection) {
		final Button button = new Button(buttons, SWT.CHECK);
		button.setText(name);
		button.setData(field);
		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					selectedSet.add(field);
				} else {
					selectedSet.remove(field);
				}
			}
		};
		button.addSelectionListener(listener);
		fieldsSet.add(field);
		buttonGridData.applyTo(button);
		// restore value
		String value = getDialogBoundsSettings().get(field.name());
		if (Boolean.valueOf(value)) {
			setSelectionAndNotify(button, true);
		} else if (value != null) {
			setSelectionAndNotify(button, false);
		} else { // value==null
			setSelectionAndNotify(button, selection);
		}
		return button;
	}

	public Set<ICardField> getFields() {
		return selectedSet;
	}

	public String getLanguage() {
		return lang;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return MagicUIActivator.getDefault().getDialogSettings(ID);
	}

	@Override
	protected void okPressed() {
		lang = langCombo.getText();
		saveWidgetValues();
		super.okPressed();
	}

	protected void saveWidgetValues() {
		try {
			IDialogSettings dialogSettings = getDialogBoundsSettings();
			// save lang
			dialogSettings.put("lang", lang);
			// save checks
			for (ICardField field : fieldsSet) {
				dialogSettings.put(field.name(), selectedSet.contains(field));
			}
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void restoreWidgetValues() {
		IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
		// restore file
		this.lang = dialogSettings.get("lang");
		if (lang != null) {
			langCombo.setText(lang);
		}
	}

	protected void setSelectionAndNotify(Button control, boolean sel) {
		control.setSelection(sel);
		final Display d = control.getDisplay();
		final Event e = new Event();
		e.type = SWT.Selection;
		e.widget = control;
		e.display = d;
		control.notifyListeners(SWT.Selection, e);
	}
}
