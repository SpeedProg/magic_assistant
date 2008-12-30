package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class SellerPriceColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public SellerPriceColumn() {
		super(MagicCardField.DBPRICE, "Seller Price");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard m = (IMagicCard) element;
			float dbPrice = m.getDbPrice();
			if (dbPrice == 0)
				return "";
			return "$" + String.valueOf(dbPrice);
		} else {
			return "";
		}
	}
}
