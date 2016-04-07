package com.reflexit.magiccards.ui.actions;

import java.util.Collection;
import java.util.function.Consumer;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.Presentation;

public class ViewAsAction extends DropDownAction<Presentation> {
	public ViewAsAction(Collection<Presentation> pres, Consumer<Presentation> onSelect) {
		super(pres, "View As", MagicUIActivator.getImageDescriptor("icons/clcl16/crosstab.png"), onSelect);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Presentation) {
			return ((Presentation) element).getLabel();
		}
		return super.getText();
	}

	@Override
	protected Presentation getDefault() {
		return Presentation.TABLE;
	}

	@Override
	public boolean isChecked(Object element) {
		System.err.println("is checked " + element + " " + getSelected());
		return element.equals(getSelected());
	}
}