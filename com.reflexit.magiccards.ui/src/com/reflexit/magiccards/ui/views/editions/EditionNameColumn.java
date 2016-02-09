package com.reflexit.magiccards.ui.views.editions;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.Rarity;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class EditionNameColumn extends AbstractEditionColumn {
	public EditionNameColumn() {
		super("Name", EditionField.NAME);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		return ed.getName();
	}

	@Override
	public int getColumnWidth() {
		return 180;
	}

	@Override
	public Image getImage(Object element) {
		Edition ed = (Edition) element;
		MagicCard fake = new MagicCard();
		fake.setSet(ed.getName());
		fake.setRarity(Rarity.MYTHIC_RARE);
		Image im = ImageCreator.getInstance().getSetImage(fake);
		if (im != null)
			return im;
		fake.setRarity(Rarity.SPECIAL);
		im = ImageCreator.getInstance().getSetImage(fake);
		if (im != null)
			return im;
		fake.setRarity(Rarity.RARE);
		fake.setCardId(1);
		return ImageCreator.getInstance().getSetImage(fake);
	}

	@Override
	public void setText(Edition edition, String string) {
		if (string.equals(edition.getName()))
			return;
		throw new UnsupportedOperationException("Cannot change edition name (unsupported)");
	}
}