package com.reflexit.magiccards.ui.actions;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

public class DeleteCardAction extends ImageAction {
	public DeleteCardAction(Runnable runnable) {
		super("Remove", null, runnable);
		setId(ActionFactory.DELETE.getId());
		setActionDefinitionId(ActionFactory.DELETE.getCommandId());
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		this.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}
}
