package com.reflexit.magiccards.ui.gallery;

import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class GalleryDeckPage extends GalleryListControl implements IDeckPage {
	@Override
	public void handleEvent(CardEvent event) {
		mcpEventHandler(event);
	}
}
