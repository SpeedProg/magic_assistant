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
	private CardFilterPreferencePage cardFilterPreference;

	public CardFilterDialog2(Shell parentShell) {
		super(parentShell, new PreferenceManager());
		this.cardFilterPreference = new CardFilterPreferencePage();
		getPreferenceManager().addToRoot(new PreferenceNode("basic", this.cardFilterPreference));
		getPreferenceManager().addToRoot(new PreferenceNode("editions", new EditionsFilterPreferencePage()));
	}

	protected boolean isResizable() {
		return true;
	}

	public IPreferenceStore getPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}
}
