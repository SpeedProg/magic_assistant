package com.reflexit.magiccards.ui.views.columns;

import java.text.DecimalFormat;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class CommunityRatingColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public CommunityRatingColumn() {
		super(MagicCardField.COMMUNITYRATING, "Rating");
	}
	DecimalFormat decimalFormat = new DecimalFormat("#0.000");

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard m = (IMagicCard) element;
			float rating = m.getCommunityRating();
			if (rating == 0)
				return "";
			return decimalFormat.format(rating);
		}
		return null;
	}
}
