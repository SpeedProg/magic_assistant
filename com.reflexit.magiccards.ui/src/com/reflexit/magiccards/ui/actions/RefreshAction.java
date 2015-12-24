package com.reflexit.magiccards.ui.actions;

import org.eclipse.ui.actions.ActionFactory;

import com.reflexit.magiccards.ui.widgets.ImageAction;

public class RefreshAction extends ImageAction {
	public RefreshAction(Runnable runnable) {
		super("Refresh", "icons/clcl16/refresh.gif", runnable);
		setId(ActionFactory.REFRESH.getId());
		setActionDefinitionId(ActionFactory.REFRESH.getCommandId());
	}
}
