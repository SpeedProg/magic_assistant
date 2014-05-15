package com.reflexit.magiccards.ui.views.columns;

import java.text.DecimalFormat;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.sync.CurrencyConvertor;

public class SellerPriceColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public SellerPriceColumn() {
		super(MagicCardField.DBPRICE, "SPrice");
	}

	public SellerPriceColumn(MagicCardField field, String name) {
		super(field, name);
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
		java.util.Currency cur = CurrencyConvertor.getCurrency();
		double price = Double.valueOf(text);
		if (price == 0)
			return "";
		double rate = CurrencyConvertor.getRate("USD" + CurrencyConvertor.getCurrency());
		if (rate == 0)
			return CurrencyConvertor.USD.getSymbol() + " " + decimalFormat.format(price);
		else
			return cur.getSymbol() + " " + decimalFormat.format(price * rate);
	}

	@Override
	public int getColumnWidth() {
		return 50;
	}
}
