package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;

import com.reflexit.magiccards.core.model.nav.CardElement;

public class CardsNavigatorLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof CardElement) {
			String file = ((CardElement) element).getFileName();
			IPath path = new org.eclipse.core.runtime.Path(file).removeFileExtension();
			return path.toString();
		}
		return super.getText(element);
	}
}
