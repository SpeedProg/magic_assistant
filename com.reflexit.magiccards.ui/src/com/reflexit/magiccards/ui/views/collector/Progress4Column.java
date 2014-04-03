package com.reflexit.magiccards.ui.views.collector;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.aggr.FieldCount4Aggregator;

public class Progress4Column extends ProgressColumn {
	private static final String PERCENT4_KEY = "percent4";

	@Override
	protected String getPercentKey() {
		return PERCENT4_KEY;
	}

	public Progress4Column() {
		super(MagicCardField.OWN_UNIQUE, "Progress4");
	}

	@Override
	public int getTotal(ICard element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int size = getSetSize(cardGroup);
			return size * 4;
		}
		return 1;
	}

	@Override
	public int getProgressSize(ICard element) {
		return (Integer) element.accept(FieldCount4Aggregator.getInstance(), null);
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof ICardGroup) {
			return "X/Y (Z%) - Means you have X cards you own (max 4 each)\n out of Y possible in this class (Y is number of cards in set/group x 4), which represents Z%";
		}
		return super.getToolTipText(element);
	}

	@Override
	public String getColumnTooltip() {
		return "Collection progress for sets of 4 cards";
	}
}
