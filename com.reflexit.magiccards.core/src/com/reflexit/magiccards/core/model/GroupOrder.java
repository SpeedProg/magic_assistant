package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class GroupOrder {
	private final ICardField fields[];

	public GroupOrder(ICardField... fields) {
		this.fields = fields.length == 0 ? null : fields;
	}

	public ICardField[] getFields() {
		return fields;
	}
}
