package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Listener;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class ColorIdentityColumn extends AbstractImageColumn implements Listener {
	public ColorIdentityColumn() {
		super(MagicCardField.COLOR_IDENTITY, "Color Identity");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			String icost = ((IMagicCard) element).getString(MagicCardField.COLOR_IDENTITY);
			return Colors.getColorName(icost);
		}
		return "";
	}

	@Override
	public Image getActualImage(Object element) {
		if (element instanceof IMagicCard) {
			String icost = ((IMagicCard) element).getString(MagicCardField.COLOR_IDENTITY);
			return SymbolConverter.buildCostImage(icost);
		}
		return null;
	}
}
