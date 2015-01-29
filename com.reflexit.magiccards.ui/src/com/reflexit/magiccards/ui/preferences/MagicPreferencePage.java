package com.reflexit.magiccards.ui.preferences;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magicassistant.p2.UpdateHandlerP2;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;
import com.reflexit.magiccards.core.sync.CurrencyConvertor;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the
 * main plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class MagicPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public MagicPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("To set more preferences please pick subcategory");
	}

	/**
	 * Creates the field editors. CardFieldExpr editors are abstractions of the
	 * common GUI blocks needed to manipulate various types of preferences. Each
	 * field editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		// internet
		createInternetOptionsGroup();
		// presentation
		BooleanFieldEditor grid = new BooleanFieldEditor(PreferenceConstants.SHOW_GRID,
				"Show grid lines in card tables",
				getFieldEditorParent());
		addField(grid);
		// protection
		BooleanFieldEditor owncopy = new BooleanFieldEditor(PreferenceConstants.OWNED_COPY,
				"Allow to copy non-virtual cards",
				getFieldEditorParent()) {
			@Override
			protected void fireStateChanged(String property, boolean oldValue, boolean newValue) {
				super.fireStateChanged(property, oldValue, newValue);
				DataManager.getInstance().setOwnCopyEnabled(newValue);
			}
		};
		addField(owncopy);
		StringFieldEditor cur = new StringFieldEditor(PreferenceConstants.CURRENCY, //
				"Default currency (code)", getFieldEditorParent()) {
			@Override
			protected void fireValueChanged(String property, Object oldValue, Object newValue) {
				super.fireValueChanged(property, oldValue, newValue);
				String val = (String) newValue;
				if (val.length() == 3) {
					CurrencyConvertor.setCurrency(val);
					CurrencyConvertor.loadRate("USD", val);
				}
			}
		};
		addField(cur);
	}

	protected void createInternetOptionsGroup() {
		Group inetOptions = new Group(getFieldEditorParent(), SWT.NONE);
		inetOptions.setText("Internet");
		GridData ld = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		ld.horizontalSpan = 2;
		inetOptions.setLayoutData(ld);
		addField(new BooleanFieldEditor(PreferenceConstants.WORK_OFFLINE, "Work Offline", inetOptions) {
			@Override
			protected void fireStateChanged(String property, boolean oldValue, boolean newValue) {
				super.fireStateChanged(property, oldValue, newValue);
				WebUtils.setWorkOffline(newValue);
			}
		});
		addField(new BooleanFieldEditor(PreferenceConstants.CHECK_FOR_CARDS,
				"Check for new cards on startup", inetOptions));
		addField(new BooleanFieldEditor(PreferenceConstants.CHECK_FOR_UPDATES,
				"Check for software updates on startup", inetOptions));
		String[][] values = getPriceProviders();
		ComboFieldEditor combo = new ComboFieldEditor(PreferenceConstants.PRICE_PROVIDER,
				"Card Prices Provider", values, inetOptions);
		addField(combo);
		createButtons(inetOptions);
		// selection
		createCardSelectGroup(inetOptions);
	}

	protected void createCardSelectGroup(Composite parent) {
		Group onCardSelect = new Group(parent, SWT.NONE);
		onCardSelect.setText("When card is selected");
		GridData ld = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		ld.horizontalSpan = 2;
		onCardSelect.setLayoutData(ld);
		BooleanFieldEditor load = new BooleanFieldEditor(PreferenceConstants.LOAD_IMAGES,
				"Load card graphics from the web", onCardSelect);
		addField(load);
		BooleanFieldEditor rulings = new BooleanFieldEditor(PreferenceConstants.LOAD_RULINGS,
				"Load rulings from the web", onCardSelect);
		addField(rulings);
		BooleanFieldEditor other = new BooleanFieldEditor(PreferenceConstants.LOAD_EXTRAS,
				"Load extra fields and update oracle text from the web", onCardSelect);
		addField(other);
		BooleanFieldEditor printings = new BooleanFieldEditor(PreferenceConstants.LOAD_PRINTINGS,
				"Load all card's printings (all sets and artworks) from the web", onCardSelect);
		addField(printings);
	}

	private void createButtons(Composite fieldEditorParent) {
		Button pressMe = new Button(fieldEditorParent, SWT.PUSH);
		pressMe.setText("Add Software Update Site...");
		pressMe.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new UpdateHandlerP2().openManipulateRepositories();
			}
		});
		GridData ld = new GridData();
		ld.horizontalSpan = 2;
		pressMe.setLayoutData(ld);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
	}

	private String[][] getPriceProviders() {
		PriceProviderManager ppm = PriceProviderManager.getInstance();
		Collection<IPriceProvider> providers = ppm.getProviders();
		String[][] res = new String[providers.size()][2];
		int i = 0;
		for (Iterator iterator = providers.iterator(); iterator.hasNext(); i++) {
			IPriceProviderStore prov = (IPriceProviderStore) iterator.next();
			res[i][0] = res[i][1] = prov.getName();
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
}