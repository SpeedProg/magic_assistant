package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.ui.utils.WaitUtils;

public class LibraryEventListener implements ICardEventListener, IDisposable {
	private final DataManager DM = DataManager.getInstance();
	private ICardEventListener eventHandler;

	public void init(IViewSite site, Runnable postLoad) {
		WaitUtils.scheduleJob("Initializing " + site.getRegisteredName(), () -> {
			if (WaitUtils.waitForLibrary()) {
				DM.getLibraryCardStore().addListener(LibraryEventListener.this);
				DM.getModelRoot().addListener(LibraryEventListener.this);
			} else {
				MagicLogger.log("Timeout on waiting for db init. Listeners are not installed.");
			}
			if (postLoad != null)
				postLoad.run();
		});
	}

	public void setEventHandler(ICardEventListener eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void dispose() {
		DM.getLibraryCardStore().removeListener(this);
		DM.getModelRoot().removeListener(this);
	}

	@Override
	public void handleEvent(CardEvent event) {
		eventHandler.handleEvent(event);
	}
}
