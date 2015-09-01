package com.reflexit.magiccards.ui.widgets;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class ContextFocusListener implements FocusListener {
	private IContextActivation activation;
	private String contextId;

	public ContextFocusListener(String contextId) {
		this.contextId = contextId;
	}

	@Override
	public void focusGained(FocusEvent e) {
		IContextService contextService = (PlatformUI.getWorkbench()
				.getService(IContextService.class));
		if (contextService != null) {
			activation = contextService.activateContext(contextId);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		IContextService contextService = (PlatformUI.getWorkbench()
				.getService(IContextService.class));
		if (activation != null) {
			contextService.deactivateContext(activation);
		}
	}
}