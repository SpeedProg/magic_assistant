package com.reflexit.magiccards.ui.views.columns;

import java.text.DecimalFormat;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class SellerPriceColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public SellerPriceColumn() {
		super(MagicCardField.DBPRICE, "Seller Price");
	}
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard m = (IMagicCard) element;
			float dbPrice = m.getDbPrice();
			if (dbPrice == 0)
				return "";
			return "$" + decimalFormat.format(dbPrice);
		} else {
			return "";
		}
	}
}
