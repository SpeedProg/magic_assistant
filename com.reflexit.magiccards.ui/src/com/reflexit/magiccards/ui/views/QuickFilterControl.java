package com.reflexit.magiccards.ui.views;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class QuickFilterControl extends Composite {
	private static final String ALL_TYPES = "";
	private static final String ADVANCED = "<advanced filter>";
	private static final String ALL_NAMES = "";
	private Text searchText;
	private IPreferenceStore store;
	private Runnable runnable;
	private Combo typeCombo;

	public QuickFilterControl(Composite composite, Runnable run) {
		super(composite, SWT.NONE);
		setLayout(new GridLayout());
		createBar(this);
		this.runnable = run;
		setFocus();
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		GridData gd = (GridData) getLayoutData();
		if (!vis) {
			gd.heightHint = 0;
			gd.widthHint = 0;
		} else {
			gd.heightHint = SWT.DEFAULT;
			gd.widthHint = SWT.DEFAULT;
			// gd.minimumHeight = 32;
			setFocus();
		}
		getParent().layout(true);
	}

	@Override
	public boolean setFocus() {
		boolean x = searchText.setFocus();
		this.searchText.setSelection(0, this.searchText.getText().length());
		return x;
	}

	void createBar(Composite comp) {
		// toolbar composite
		GridLayout gridLayout2 = new GridLayout(2, false);
		gridLayout2.marginHeight = 0;
		gridLayout2.marginWidth = 0;
		comp.setLayout(gridLayout2);
		// toolbar
		ToolBar toolbar = new ToolBar(comp, SWT.FLAT);
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// createToolBarLabel(toolbar, "Text:");
		// search field
		createSearchField(toolbar);
		createColorButton(toolbar, "White");
		createColorButton(toolbar, "Blue");
		createColorButton(toolbar, "Black");
		createColorButton(toolbar, "Red");
		createColorButton(toolbar, "Green");
		// type
		createTypeField(toolbar);
		// hide
		// createHideButton(comp);
	}

	private void createSearchField(ToolBar toolbar) {
		this.searchText = new Text(toolbar, SWT.BORDER);
		this.searchText.setText(ALL_NAMES);
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		this.searchText.setLayoutData(td);
		this.searchText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				// nothing
			}

			public void focusGained(FocusEvent e) {
				searchText.setSelection(0, searchText.getText().length());
			}
		});
		this.searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filterText(searchText.getText());
			}
		});
		this.searchText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				filterText(searchText.getText());
			}
		});
		searchText.setToolTipText("Name filter");
		ToolItem text = new ToolItem(toolbar, SWT.SEPARATOR);
		text.setControl(this.searchText);
		text.setWidth(200);
	}

	private void createTypeField(ToolBar toolbar) {
		typeCombo = new Combo(toolbar, SWT.BORDER);
		typeCombo.add(ALL_TYPES);
		typeCombo.setText(ALL_TYPES);
		Collection<String> names = CardTypes.getInstance().getLocalizedNames();
		for (String type : names) {
			typeCombo.add(type);
		}
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		typeCombo.setLayoutData(td);
		typeCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filterType(typeCombo.getText());
			}
		});
		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				filterType(typeCombo.getText());
			}
		});
		typeCombo.setToolTipText("Type filter");
		ToolItem item = new ToolItem(toolbar, SWT.SEPARATOR);
		item.setControl(this.typeCombo);
		item.setWidth(200);
	}

	private void createToolBarLabel(ToolBar toolbar, String string) {
		Label label = new Label(toolbar, SWT.NONE);
		label.setText(string);
		ToolItem text = new ToolItem(toolbar, SWT.SEPARATOR);
		text.setControl(label);
		text.setWidth(50);
	}

	private void createColorButton(ToolBar toolbar, String name) {
		Colors colors = Colors.getInstance();
		final String id = colors.getPrefConstant(name);
		String abbr = Colors.getInstance().getEncodeByName(name);
		//
		final ToolItem button = new ToolItem(toolbar, SWT.CHECK);
		button.setImage(SymbolConverter.buildCostImage("{" + abbr + "}"));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(id, button.getSelection());
				runnable.run();
			}
		});
		button.setSelection(false);
		button.setToolTipText(name);
	}

	private void createHideButton(Composite comp) {
		ToolBar toolbar = new ToolBar(comp, SWT.FLAT);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		//
		ToolItem hideButton = new ToolItem(toolbar, SWT.PUSH);
		hideButton.setImage(MagicUIActivator.getDefault().getImage("icons/clcl16/delete_obj.gif"));
		hideButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setVisible(false);
			}
		});
	}

	public void setPreferenceStore(IPreferenceStore store) {
		this.store = store;
		refresh();
	}

	void refresh() {
		if (searchText != null && store != null) {
			// text
			String textId = FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX);
			String text = store.getString(textId);
			if (text == null || text.trim().length() == 0) {
				searchText.setText(ALL_NAMES);
			} else if (text.startsWith("\"") && text.endsWith("\"")) {
				text = text.replaceAll("\"(.*)\"", "\\1");
				searchText.setText(text);
			} else {
				searchText.setText(ADVANCED);
			}
			// type
			String type = ALL_TYPES;
			int typehit = 0;
			CardTypes coreTypes = CardTypes.getInstance();
			for (Iterator iterator = coreTypes.getIds().iterator(); iterator.hasNext();) {
				String id = (String) iterator.next();
				boolean isSet = store.getBoolean(id);
				// System.err.println(id + " " + isSet);
				if (isSet) {
					type = coreTypes.getLocalizedNameById(id);
					typehit++;
				}
			}
			if (typehit > 1) {
				typeCombo.setText(ADVANCED);
			} else {
				typeCombo.setText(type);
			}
		}
	}

	public void setUpdateHook(Runnable run) {
		this.runnable = run;
	}

	protected void filterText(String text) {
		if (ADVANCED.equals(text))
			return;
		if (ALL_NAMES.equals(text))
			text = "";
		String textId = FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX);
		if (text.trim().length() == 0) {
			this.store.setValue(textId, "");
		} else {
			this.store.setValue(textId, "\"" + text + "\"");
		}
		runnable.run();
	}

	protected void filterType(String text) {
		if (ADVANCED.equals(text))
			return;
		if (ALL_TYPES.equals(text))
			text = "";
		CardTypes coreTypes = CardTypes.getInstance();
		String selId = null;
		String textId = FilterHelper.getPrefConstant(FilterHelper.TYPE_LINE, FilterHelper.TEXT_POSTFIX);
		this.store.setValue(textId, "");
		for (Iterator iterator = coreTypes.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			store.setValue(id, "false");
			if (coreTypes.getLocalizedNameById(id).equals(text)) {
				selId = id;
			}
		}
		if (selId == null) {
			this.store.setValue(textId, text.trim());
		} else {
			store.setValue(selId, "true");
		}
		runnable.run();
	}
}
