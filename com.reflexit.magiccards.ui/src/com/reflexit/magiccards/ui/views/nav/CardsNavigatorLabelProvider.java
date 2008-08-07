package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.jface.viewers.LabelProvider;

import com.reflexit.magiccards.core.model.nav.CardElement;

public class CardsNavigatorLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof CardElement) {
			String name = ((CardElement) element).getName();
			return name;
		}
		return super.getText(element);
	}
}
