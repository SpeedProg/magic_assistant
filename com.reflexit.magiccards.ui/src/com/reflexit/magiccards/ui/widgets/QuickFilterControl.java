package com.reflexit.magiccards.ui.widgets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.utils.WaitUtils;

public class QuickFilterControl extends Composite {
	private static final String ALL_TYPES = "";
	private static final String ADVANCED = "<advanced filter>";
	private static final String ALL_NAMES = "";
	private Text searchText;
	private IPreferenceStore store;
	private Runnable runnable;
	private Combo typeCombo;
	private ToolBar toolbar;
	private EditionTextControl setCombo;
	private long lastMod = 0;
	private boolean pendingUpdate = false;
	private Object updateLock = new Object();
	private UpdateThread uthread;
	private int updateDelay = 300;
	private boolean suppressUpdates = false;

	class UpdateThread extends Thread {
		public UpdateThread() {
			super("Quick Filter Update Thread");
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (updateLock) {
						if (pendingUpdate == false) {
							updateLock.wait();
							// MagicLogger.trace("QUPDATE", "got update on
							// wait");
							// we got notification
							if (pendingUpdate == false)
								continue; // hmm misfire?
							while (pendingUpdate && System.currentTimeMillis() - lastMod < updateDelay) {
								updateLock.wait(updateDelay);
								// MagicLogger.trace("QUPDATE", "got update on
								// wait " +
								// (System.currentTimeMillis() - lastMod));
							}
							if (pendingUpdate == false)
								continue;
							// we got more than 500 ms timeout
						}
						// pendingUpdate is true now
					}
					// System.err.println(System.currentTimeMillis() + " running
					// now");
					doUpdate();
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private void doUpdate() {
		synchronized (updateLock) {
			pendingUpdate = false;
			updateLock.notifyAll();
		}
		updateStore();
		runnable.run();
	}

	private void updateStore() {
		WaitUtils.syncExec(() -> {
			filterSet(setCombo.getText());
			filterText(searchText.getText());
			filterType(typeCombo.getText());
		});
	}

	public QuickFilterControl(Composite composite, Runnable run, boolean visible) {
		super(composite, SWT.NONE);
		setLayout(GridLayoutFactory.fillDefaults().create());
		setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		createBar(this);
		this.runnable = run;
		setVisible(visible);
	}

	public void setUpdateDelay(int updateDelay) {
		this.updateDelay = updateDelay;
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		GridData gd = (GridData) getLayoutData();
		gd.exclude = !vis;
		gd.widthHint = vis ? SWT.DEFAULT : 0;
		getParent().getParent().layout(true);
		if (!vis) {
			if (uthread != null) {
				uthread.interrupt();
				uthread = null;
			}
		} else {
			setFocus();
			if (uthread != null)
				uthread.interrupt();
			uthread = new UpdateThread();
			uthread.start();
		}
	}

	@Override
	public boolean setFocus() {
		boolean x = searchText.setFocus();
		this.searchText.setSelection(0, this.searchText.getText().length());
		return x;
	}

	void createBar(Composite comp) {
		setLayout(GridLayoutFactory.fillDefaults().numColumns(6).create());
		// search field
		createSearchField(comp);
		// toolbar
		toolbar = new ToolBar(comp, SWT.FLAT);
		toolbar.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		// this has to be first to set proper hight of toolbar
		createActions(toolbar);
		createColorButton(toolbar, "White");
		createColorButton(toolbar, "Blue");
		createColorButton(toolbar, "Black");
		createColorButton(toolbar, "Red");
		createColorButton(toolbar, "Green");
		// type
		createTypeField(comp);
		// set
		createEditionField(comp);
		// hide
		// createHideButton(comp);
	}

	private void createActions(ToolBar toolbar) {
		final ToolItem button = new ToolItem(toolbar, SWT.PUSH);
		button.setImage(MagicUIActivator.getDefault().getImage("icons/clcl16/reset_filter.gif"));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetFilter();
				refresh();
				kickUpdate();
			}
		});
	}

	private void resetFilter() {
		Collection<String> allIds = FilterField.getAllIds();
		for (Iterator<String> iterator = allIds.iterator(); iterator.hasNext();) {
			String id = iterator.next();
			store.setToDefault(id);
		}
		store.setToDefault(EditionsFilterPreferencePage.LAST_SET);
	}

	private void createSearchField(Composite toolbar) {
		this.searchText = new Text(toolbar, SWT.SEARCH | SWT.ICON_CANCEL);
		this.searchText.setText(ALL_NAMES);
		searchText.setLayoutData(GridDataFactory.fillDefaults().hint(200, 16).create());
		this.searchText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// nothing
			}

			@Override
			public void focusGained(FocusEvent e) {
				searchText.setSelection(0, searchText.getText().length());
			}
		});
		this.searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				kickUpdate();
			}
		});
		this.searchText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doUpdate();
			}
		});
		searchText.setToolTipText("Name filter");
		searchText.addFocusListener(new SearchContextFocusListener());
		if (toolbar instanceof ToolBar) {
			ToolItem text = new ToolItem((ToolBar) toolbar, SWT.SEPARATOR);
			text.setControl(this.searchText);
			text.setWidth(200);
		}
	}

	private void createTypeField(Composite toolbar) {
		typeCombo = new Combo(toolbar, SWT.BORDER);
		typeCombo.add(ALL_TYPES);
		typeCombo.setText(ALL_TYPES);
		typeCombo.setLayoutData(GridDataFactory.fillDefaults().hint(100, 16).create());
		Collection<String> names = CardTypes.getInstance().getLocalizedNames();
		for (String type : names) {
			typeCombo.add(type);
		}
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		typeCombo.setLayoutData(td);
		typeCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				kickUpdate();
			}
		});
		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doUpdate();
			}
		});
		typeCombo.setToolTipText("Type filter");
		typeCombo.addFocusListener(new SearchContextFocusListener());
		if (toolbar instanceof ToolBar) {
			ToolItem item = new ToolItem((ToolBar) toolbar, SWT.SEPARATOR);
			item.setControl(typeCombo);
			item.setWidth(150);
		}
	}

	private void createEditionField(Composite toolbar) {
		EditionTextControl setCombo = new EditionTextControl(toolbar, SWT.BORDER);
		setCombo.setToolTipText("Set filter");
		setCombo.setLayoutData(GridDataFactory.fillDefaults().hint(150, 16).create());
		setCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				kickUpdate();
			}
		});
		setCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doUpdate();
			}
		});
		this.setCombo = setCombo;
		if (toolbar instanceof ToolBar) {
			ToolItem item = new ToolItem((ToolBar) toolbar, SWT.SEPARATOR);
			item.setControl(setCombo);
			item.setWidth(180);
		}
	}

	// private void createToolBarLabel(ToolBar toolbar, String string) {
	// Label label = new Label(toolbar, SWT.NONE);
	// label.setText(string);
	// ToolItem text = new ToolItem(toolbar, SWT.SEPARATOR);
	// text.setControl(label);
	// text.setWidth(50);
	// }
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
				kickUpdate();
			}
		});
		button.setSelection(false);
		button.setToolTipText(name);
		button.setData("id", id);
	}

	private void setButtonToStoreValue(ToolBar toolbar, String id) {
		ToolItem[] children = toolbar.getItems();
		for (int i = 0; i < children.length; i++) {
			ToolItem control = children[i];
			Object data = control.getData("id");
			if (data != null && id.equals(data)) {
				control.setSelection(store.getBoolean(id));
			}
		}
	}

	// private void createHideButton(Composite comp) {
	// ToolBar toolbar = new ToolBar(comp, SWT.FLAT);
	// GridData gd = new GridData();
	// gd.horizontalAlignment = GridData.END;
	// //
	// ToolItem hideButton = new ToolItem(toolbar, SWT.PUSH);
	// hideButton.setImage(MagicUIActivator.getDefault().getImage("icons/clcl16/delete_obj.gif"));
	// hideButton.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// setVisible(false);
	// }
	// });
	// }
	public void setPreferenceStore(IPreferenceStore store) {
		this.store = store;
	}

	public void refresh() {
		if (searchText != null && store != null) {
			// text
			String textId = FilterField.NAME_LINE.getPrefConstant();
			String text = store.getString(textId);
			if (text == null || text.trim().length() == 0) {
				searchText.setText(ALL_NAMES);
			} else if (text.startsWith("\"") && text.endsWith("\"")) {
				text = text.replaceAll("\"(.*)\"", "$1");
				searchText.setText(text);
			} else {
				searchText.setText(ADVANCED);
			}
			// type
			String type = ALL_TYPES;
			String typeId = FilterField.TYPE_LINE.getPrefConstant();
			String type1 = store.getString(typeId);
			int typehit = 0;
			CardTypes coreTypes = CardTypes.getInstance();
			for (Iterator<String> iterator = coreTypes.getIds().iterator(); iterator.hasNext();) {
				String id = iterator.next();
				boolean isSet = store.getBoolean(id);
				// System.err.println(id + " " + isSet);
				if (isSet) {
					type = coreTypes.getLocalizedNameById(id);
					typehit++;
				}
			}
			if (typehit > 1 || typehit == 1 && type1.length() > 0) {
				typeCombo.setText(ADVANCED);
			} else if (typehit == 0) {
				typeCombo.setText(type1);
			} else {
				typeCombo.setText(type);
			}
			// set
			Collection<String> ids = Colors.getInstance().getIds();
			for (Iterator<String> iterator = ids.iterator(); iterator.hasNext();) {
				String id = iterator.next();
				setButtonToStoreValue(toolbar, id);
			}
			// type
			String set = ALL_TYPES;
			int sethit = 0;
			Editions editions = Editions.getInstance();
			for (Iterator<String> iterator = editions.getIds().iterator(); iterator.hasNext();) {
				String id = iterator.next();
				boolean isSet = store.getBoolean(id);
				// System.err.println(id + " " + isSet);
				if (isSet) {
					set = editions.getNameById(id);
					sethit++;
				}
			}
			if (sethit > 1) {
				setCombo.setText(ADVANCED);
			} else if (sethit == 0) {
				setCombo.setText(ALL_TYPES);
			} else {
				setCombo.setText(set);
			}
		}
	}

	public void setUpdateHook(Runnable run) {
		this.runnable = run;
	}

	private void kickUpdate() {
		if (suppressUpdates)
			return;
		synchronized (updateLock) {
			lastMod = System.currentTimeMillis();
			pendingUpdate = true;
			// MagicLogger.trace("QUPDATE", "Sending notification for '" + text
			// + "'");
			updateLock.notifyAll();
		}
	}

	protected void filterText(String text) {
		if (ADVANCED.equals(text))
			return;
		if (ALL_NAMES.equals(text))
			text = "";
		String textId = FilterField.NAME_LINE.getPrefConstant();
		if (text.trim().length() == 0) {
			this.store.setValue(textId, "");
		} else {
			this.store.setValue(textId, "\"" + text + "\"");
		}
	}

	protected void filterType(String text) {
		if (ADVANCED.equals(text))
			return;
		if (ALL_TYPES.equals(text))
			text = "";
		CardTypes coreTypes = CardTypes.getInstance();
		String selId = null;
		String textId = FilterField.TYPE_LINE.getPrefConstant();
		for (Iterator<String> iterator = coreTypes.getIds().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			store.setValue(id, "false");
			if (coreTypes.getLocalizedNameById(id).equals(text)) {
				selId = id;
			}
		}
		if (selId == null) {
			store.setValue(textId, text.trim());
		} else {
			store.setValue(textId, "");
			store.setValue(selId, "true");
		}
	}

	protected void filterSet(String set) {
		if (ADVANCED.equals(set))
			return;
		if (ALL_TYPES.equals(set))
			set = "";
		Editions editions = Editions.getInstance();
		String lset = set.toLowerCase(Locale.ENGLISH);
		boolean exactMatch = false;
		Collection<String> ids = editions.getIds();
		for (Iterator<String> iterator = ids.iterator(); iterator.hasNext();) {
			String id = iterator.next();
			store.setValue(id, "false");
			if (editions.getNameById(id).toLowerCase().equals(lset)) {
				store.setValue(id, "true");
				exactMatch = true;
			}
		}
		if (!exactMatch && lset.length() > 0) {
			for (Iterator<String> iterator = ids.iterator(); iterator.hasNext();) {
				String id = iterator.next();
				if (editions.getNameById(id).toLowerCase().contains(lset)) {
					store.setValue(id, "true");
				}
			}
		}
	}

	public boolean isSuppressUpdates() {
		return suppressUpdates;
	}

	public void setSuppressUpdates(boolean suppressUpdates) {
		this.suppressUpdates = suppressUpdates;
	}
}
