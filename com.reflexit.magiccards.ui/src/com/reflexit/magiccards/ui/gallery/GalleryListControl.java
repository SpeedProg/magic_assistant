package com.reflexit.magiccards.ui.gallery;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.ui.views.IMagicViewer;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;

public class GalleryListControl extends DeckListControl {
	public GalleryListControl() {
		super(Presentation.GALLERY);
	}

	@Override
	public IMagicViewer createViewer(Composite parent) {
		return new SplitGalleryViewer(parent, getPreferencePageId());
	}

	@Override
	public void saveColumnLayout() {
		// we don't save columns here
	}

	@Override
	public ColumnCollection getSortColumnCollection() {
		return new MagicColumnCollection("");
	}

	@Override
	protected String getPreferencePageId() {
		return GalleryPreferencePage.getId();
	}
}