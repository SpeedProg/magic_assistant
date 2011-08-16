package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class QuickFilterControl extends Composite {
	private Text searchText;
	private IPreferenceStore store;
	private Runnable runnable;

	public QuickFilterControl(Composite composite, Runnable run) {
		super(composite, SWT.BORDER);
		setLayout(new GridLayout());
		createBar(this);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.runnable = run;
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		GridData gd = (GridData) getLayoutData();
		if (!vis)
			gd.heightHint = 0;
		else {
			gd.heightHint = SWT.DEFAULT;
			gd.minimumHeight = 32;
			setFocus();
			this.searchText.setSelection(0, this.searchText.getText().length());
		}
		getParent().layout(true);
	}

	@Override
	public boolean setFocus() {
		return searchText.setFocus();
	}

	void createBar(Composite comp) {
		// toolbar composite
		GridLayout gridLayout2 = new GridLayout(3, false);
		gridLayout2.marginHeight = 0;
		gridLayout2.marginWidth = 0;
		comp.setLayout(gridLayout2);
		// toolbar
		ToolBar toolbar = new ToolBar(comp, SWT.FLAT);
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// createToolBarLabel(toolbar, "Text:");
		// search field
		this.searchText = new Text(toolbar, SWT.BORDER);
		this.searchText.setText("name filter...");
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		this.searchText.setLayoutData(td);
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
		ToolItem text = new ToolItem(toolbar, SWT.SEPARATOR);
		text.setControl(this.searchText);
		text.setWidth(300);
		createColorButton(toolbar, "White");
		createColorButton(toolbar, "Blue");
		createColorButton(toolbar, "Black");
		createColorButton(toolbar, "Red");
		createColorButton(toolbar, "Green");
		// hide
		createHideButton(comp);
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
	}

	public void setUpdateHook(Runnable run) {
		this.runnable = run;
	}

	protected void filterText(String text) {
		String textId = FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX);
		if (text.trim().length() == 0) {
			this.store.setValue(textId, "");
		} else {
			this.store.setValue(textId, "\"" + text + "\"");
		}
		runnable.run();
	}
}
