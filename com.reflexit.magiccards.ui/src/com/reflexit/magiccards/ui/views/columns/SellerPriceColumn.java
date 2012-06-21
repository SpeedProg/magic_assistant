package com.reflexit.magiccards.ui.views.columns;

import java.text.DecimalFormat;

import com.reflexit.magiccards.core.model.MagicCardField;

public class SellerPriceColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public SellerPriceColumn() {
		super(MagicCardField.DBPRICE, "SPrice");
	}

	@Override
	public String getColumnFullName() {
		return "Seller Price";
	}

	DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	@Override
	public String getText(Object element) {
		String text = super.getText(element);
		if (text.length() == 0)
			return text;
		if (text.equals("0.0"))
			return "";
		return "$" + decimalFormat.format(Float.parseFloat(text));
	}

	@Override
	public int getColumnWidth() {
		return 50;
	}
}
