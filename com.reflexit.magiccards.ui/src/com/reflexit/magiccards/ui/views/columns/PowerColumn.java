package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;

public class PowerColumn extends GenColumn {
	private String fullname;

	public PowerColumn(ICardField field, String columnName, String fullname) {
		super(field, columnName);
		this.fullname = fullname;
	}

	@Override
	public String getColumnFullName() {
		return this.fullname;
	}

	@Override
	public int getColumnWidth() {
		return 22;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ICardGroup) {
			CardGroup group = (CardGroup) element;
			if (group.isTransient())
				return super.getText(group.getFirstCard());
			int cc = group.getCreatureCount();
			if (cc > 1) {
				String pow = (String) group.get(dataIndex);
				Float fpow = MagicCard.convertFloat(pow);
				return pow + " (" + String.format("%.1f", 0.009 + fpow / cc) + ")";
			}
		}
		return super.getText(element);
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof ICardGroup) {
			return "X (Y) means X is total " + fullname + " of all creatures, Y is average in the group";
		}
		return super.getToolTipText(element);
	}
}
