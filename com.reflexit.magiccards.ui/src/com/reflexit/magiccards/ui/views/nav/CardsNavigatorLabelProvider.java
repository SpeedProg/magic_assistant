package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class CardsNavigatorLabelProvider extends LabelProvider implements IColorProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof CardElement) {
			String name = ((CardElement) element).getLabel();
			String activeDeckId = DataManager.getCardHandler().getActiveDeckId();
			if (activeDeckId != null && element instanceof CardCollection) {
				String collId = ((CardCollection) element).getId();
				if (activeDeckId.equals(collId)) {
					return name + " (Active)";
				}
			}
			return name;
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof CardCollection) {
			if (((CardCollection) element).isDeck())
				return MagicUIActivator.getDefault().getImage("icons/obj16/ideck16.png");
			else
				return MagicUIActivator.getDefault().getImage("icons/obj16/lib16.png");
		}
		if (element instanceof CollectionsContainer) {
			return MagicUIActivator.getDefault().getImage("icons/obj16/folder-lib.png");
		}
		if (element instanceof MagicDbContainter) {
			return MagicUIActivator.getDefault().getImage("icons/obj16/m16.png");
		}
		if (element instanceof CardOrganizer) {
			return MagicUIActivator.getDefault().getImage("icons/obj16/folder-lib.png");
		}
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		boolean own = true;
		if (element instanceof CardCollection) {
			CardCollection cardCollection = (CardCollection) element;
			own = !cardCollection.isVirtual();
		}
		return MagicUIActivator.getDefault().getBgColor(own);
	}
}
