package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class GroupOrder {
	private final ICardField fields[];

	public GroupOrder(ICardField... fields) {
		if (fields != null && fields.length > 0)
			this.fields = fields;
		else
			this.fields = null;
	}

	public ICardField[] getFields() {
		return fields;
	}
}
