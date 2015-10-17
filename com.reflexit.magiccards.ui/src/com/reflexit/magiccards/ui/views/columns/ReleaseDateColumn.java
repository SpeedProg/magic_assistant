package com.reflexit.magiccards.ui.views.columns;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class ReleaseDateColumn extends GenColumn {
	private final SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");

	public ReleaseDateColumn() {
		super(MagicCardField.SET_RELEASE, "Release Date");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			Object object = ((IMagicCard) element).get(dataIndex);
			if (object instanceof Date) {
				return formatter.format(object);
			}
		}
		return super.getText(element);
	}
}
