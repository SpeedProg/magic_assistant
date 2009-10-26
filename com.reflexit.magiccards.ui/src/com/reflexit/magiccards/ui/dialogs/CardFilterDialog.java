package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.AbilitiesFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.CardFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;

public class CardFilterDialog extends PreferenceDialog implements IPreferencePageContainer {
	private IPreferenceStore store;

	public CardFilterDialog(Shell parentShell, IPreferenceStore store) {
		super(parentShell, new PreferenceManager());
		if (store == null)
			this.store = MagicUIActivator.getDefault().getPreferenceStore();
		else
			this.store = store;
		//
		addNode(new PreferenceNode("basic", new CardFilterPreferencePage()));
		addNode(new PreferenceNode("editions", new EditionsFilterPreferencePage()));
		addNode(new PreferenceNode("abilities", new AbilitiesFilterPreferencePage()));
	}

	public void addNode(PreferenceNode node) {
		getPreferenceManager().addToRoot(node);
		PreferencePage page = (PreferencePage) node.getPage();
		page.setPreferenceStore(this.store);
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
