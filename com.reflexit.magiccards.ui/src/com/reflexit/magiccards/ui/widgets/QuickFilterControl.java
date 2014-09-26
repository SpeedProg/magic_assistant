package com.reflexit.magiccards.ui.widgets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
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
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.FilterField;
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
	private ToolBar toolbar;
	private Text setCombo;
	private long lastMod = 0;
	private boolean pendingUpdate = false;
	private Object updateLock = new Object();
	private UpdateThread uthread;
	private int updateDelay = 700;
	private ContentProposalAdapter proposalAdapter;

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
							// MagicLogger.trace("QUPDATE", "got update on wait");
							// we got notification
							if (pendingUpdate == false)
								continue; // hmm misfire?
							while (pendingUpdate && System.currentTimeMillis() - lastMod < updateDelay) {
								updateLock.wait(updateDelay);
								// MagicLogger.trace("QUPDATE", "got update on wait " +
								// (System.currentTimeMillis() - lastMod));
							}
							// we got more than 500 ms timeout
						}
						// pendingUpdate is true now
						pendingUpdate = false;
					}
					// System.err.println(System.currentTimeMillis() + "  running now");
					runnable.run();
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public QuickFilterControl(Composite composite, Runnable run, boolean visible) {
		super(composite, SWT.NONE);
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
		if (!vis) {
			gd.heightHint = 0;
			gd.widthHint = 0;
			if (uthread != null) {
				uthread.interrupt();
				uthread = null;
			}
		} else {
			gd.heightHint = SWT.DEFAULT;
			gd.widthHint = SWT.DEFAULT;
			// gd.minimumHeight = 32;
			setFocus();
			if (uthread != null)
				uthread.interrupt();
			uthread = new UpdateThread();
			uthread.start();
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
		toolbar = new ToolBar(comp, SWT.FLAT);
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
		// set
		createEditionField(toolbar);
		// hide
		// createHideButton(comp);
	}

	private void createSearchField(ToolBar toolbar) {
		this.searchText = new Text(toolbar, SWT.BORDER);
		this.searchText.setText(ALL_NAMES);
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		this.searchText.setLayoutData(td);
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
			@Override
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
		item.setWidth(150);
	}

	private void createEditionField(ToolBar toolbar) {
		setCombo = new Text(toolbar, SWT.BORDER);
		proposalAdapter = ContextAssist.addContextAssist(setCombo, new String[0], false);
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		setCombo.setLayoutData(td);
		setCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				filterSet(setCombo.getText());
			}
		});
		setCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				filterSet(setCombo.getText());
			}
		});
		setCombo.setToolTipText("Set filter");
		setCombo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// ignore
			}

			@Override
			public void focusGained(FocusEvent e) {
				Collection<String> names = Editions.getInstance().getNames();
				String[] setProposals = new String[names.size()];
				int i = 0;
				for (String type : names) {
					setProposals[i++] = type;
				}
				IContentProposalProvider contentProposalProvider = proposalAdapter.getContentProposalProvider();
				if (contentProposalProvider instanceof SimpleContentProposalProvider) {
					((SimpleContentProposalProvider) contentProposalProvider).setProposals(setProposals);
				}
			}
		});
		ToolItem item = new ToolItem(toolbar, SWT.SEPARATOR);
		item.setControl(setCombo);
		item.setWidth(180);
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
				kickUpdate("color " + id);
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

	private void kickUpdate(@SuppressWarnings("unused") String text) {
		synchronized (updateLock) {
			lastMod = System.currentTimeMillis();
			pendingUpdate = true;
			// MagicLogger.trace("QUPDATE", "Sending notification for '" + text + "'");
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
		kickUpdate(text);
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
		kickUpdate("type " + text);
	}

	protected void filterSet(String text) {
		if (ADVANCED.equals(text))
			return;
		if (ALL_TYPES.equals(text))
			text = "";
		Editions editions = Editions.getInstance();
		String ltext = text.toLowerCase(Locale.ENGLISH);
		for (Iterator<String> iterator = editions.getIds().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			store.setValue(id, "false");
			if (text.length() > 0 && editions.getNameById(id).toLowerCase().contains(ltext)) {
				store.setValue(id, "true");
			}
		}
		kickUpdate("set " + text);
	}
}
