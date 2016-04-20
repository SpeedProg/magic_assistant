package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class SuperDeckPage extends DeckListControl implements IDeckPage, IMagicCardListControl {
	public SuperDeckPage() {
		setPresentation(Presentation.TABLE);
	}
}
