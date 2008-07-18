package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.CardFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;

public class CardFilterDialog2 extends PreferenceDialog implements IPreferencePageContainer {
	private IPreferenceStore store;

	public CardFilterDialog2(Shell parentShell, IPreferenceStore store) {
		super(parentShell, new PreferenceManager());
		if (store == null)
			this.store = MagicUIActivator.getDefault().getPreferenceStore();
		else
			this.store = store;
		//
		CardFilterPreferencePage basicPage = new CardFilterPreferencePage();
		basicPage.setPreferenceStore(this.store);
		//
		EditionsFilterPreferencePage setsPage = new EditionsFilterPreferencePage();
		setsPage.setPreferenceStore(this.store);
		//
		getPreferenceManager().addToRoot(new PreferenceNode("basic", basicPage));
		getPreferenceManager().addToRoot(new PreferenceNode("editions", setsPage));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return this.store;
	}
}
