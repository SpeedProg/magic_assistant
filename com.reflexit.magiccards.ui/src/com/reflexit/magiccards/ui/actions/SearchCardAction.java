package com.reflexit.magiccards.ui.actions;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class SearchCardAction extends ImageAction {
	public SearchCardAction(Runnable runnable) {
		super("Find...", null, runnable);
		// setId(ActionFactory.DELETE.getId());
		setActionDefinitionId("org.eclipse.ui.edit.findReplace");
		setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/search.png"));
	}
}
