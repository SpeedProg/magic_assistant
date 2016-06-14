package com.reflexit.magicassistant.swtbot.model;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;

import com.reflexit.magiccards.ui.gallery.GalleryView;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;

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

	public SWTBotMagicView deck(String title) {
		SWTBotMagicView swtBotDeckView = viewByTitle(title);
		swtBotDeckView.show();
		return swtBotDeckView;
	}

	public SWTBotMagicView deck() {
		SWTBotView deckView = viewById(DeckView.ID);
		return new SWTBotMagicView(deckView.getReference(), this);
	}

	public SWTBotMagicView db() {
		SWTBotView view = viewById(MagicDbView.ID);
		return new SWTBotMagicView(view.getReference(), this);
	}

	public SWTBotMagicView gallery() {
		return viewById(GalleryView.ID);
	}

	@Override
	public SWTBotMagicView viewById(String id) {
		SWTBotView view = super.viewById(id);
		return new SWTBotMagicView(view.getReference(), this);
	}

	@Override
	public SWTBotMagicView viewByTitle(String title) {
		SWTBotView view = super.viewByTitle(title);
		return new SWTBotMagicView(view.getReference(), this);
	}
}
