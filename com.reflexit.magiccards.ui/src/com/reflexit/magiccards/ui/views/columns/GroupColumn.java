package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class GroupColumn extends GenColumn {
	public static final String COL_NAME = "Group";
	private ICardField groupField;

	public GroupColumn() {
		super(null, COL_NAME);
	}

	public GroupColumn(ICardField field, String columnName) {
		super(field, columnName);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof CardGroup) {
			if (((CardGroup) element).getFieldIndex() == MagicCardField.NAME) {
				return ImageCreator.getInstance().getSetImage(((CardGroup) element).getFirstCard());
			}
		}
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			return ImageCreator.getInstance().getSetImage(card);
		}
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 200;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			return ((CardGroup) element).getName() + " (" + ((CardGroup) element).getCount() + ")";
		}
		if (element instanceof IMagicCard) {
			return ((IMagicCard) element).getName();
		}
		return null;
	}

	public void setGroupField(ICardField field) {
		groupField = field;
	}

	public ICardField getGroupField() {
		return groupField;
	}

	@Override
	public ICardField getSortField() {
		return groupField;
	}

	@Override
	public ICardField getDataField() {
		return super.getDataField();
	}
}
