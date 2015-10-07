package com.reflexit.magiccards.ui.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

public final class MagicViewParameterValues implements IParameterValues {
	@Override
	public final Map getParameterValues() {
		final Map values = new HashMap();
		final IViewDescriptor[] views = PlatformUI.getWorkbench().getViewRegistry().getViews();
		for (int i = 0; i < views.length; i++) {
			final IViewDescriptor view = views[i];
			String id = view.getId();
			if (id.startsWith("com.reflexit"))
				values.put(view.getLabel(), id);
		}
		return values;
	}
}