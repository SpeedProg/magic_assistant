package com.reflexit.magiccards.core.model;

import java.util.LinkedHashMap;

public class MagicCardHash implements ICard, ICardModifiable {
	private LinkedHashMap<ICardField, Object> fields = new LinkedHashMap<ICardField, Object>();

	@Override
	public Object get(ICardField field) {
		return fields.get(field);
	}

	@Override
	public String getName() {
		return (String) fields.get(MagicCardField.NAME);
	}

	@Override
	public ICard cloneCard() {
		return (MagicCardHash) clone();
	}

	@Override
	public Object clone() {
		try {
			MagicCardHash obj = (MagicCardHash) super.clone();
			if (this.fields != null)
				obj.fields = (LinkedHashMap<ICardField, Object>) this.fields.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public int accept(ICardVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public boolean set(ICardField field, String value) {
		fields.put(field, value);
		return true;
	}
}
