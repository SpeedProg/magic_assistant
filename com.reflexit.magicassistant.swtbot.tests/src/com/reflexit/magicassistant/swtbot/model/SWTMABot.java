package com.reflexit.magicassistant.swtbot.model;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;

public class SWTMABot extends SWTWorkbenchBot {
	public SWTMABot() {
		System.setProperty("junit.testing", "true");
	}

	public void resetPrefs() {
		IPreferenceStore mdbStore = PreferenceInitializer.getMdbStore();
		PreferenceInitializer.setToDefault(mdbStore);
		PreferenceInitializer.setToDefault(PreferenceInitializer.getFilterStore(MagicDbViewPreferencePage.PPID));
		mdbStore.setValue(PreferenceConstants.GROUP_FIELD, "");
		IPreferenceStore deckStore = PreferenceInitializer.getDeckStore();
		PreferenceInitializer.setToDefault(deckStore);
		deckStore.setValue(PreferenceConstants.GROUP_FIELD, "");
		try {
			resetWorkbench();
		} catch (Exception e) {
			// ignore
		}
	}

	public SWTBotDeckView deck(String title) {
		SWTBotDeckView swtBotDeckView = new SWTBotDeckView(title, this);
		swtBotDeckView.show();
		return swtBotDeckView;
	}
}
