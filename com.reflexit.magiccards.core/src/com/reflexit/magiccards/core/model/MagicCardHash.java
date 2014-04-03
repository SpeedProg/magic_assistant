package com.reflexit.magiccards.core.model;

import java.util.LinkedHashMap;

public class MagicCardHash extends AbstractMagicCard {
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
	public Object clone() throws CloneNotSupportedException {
		MagicCardHash obj = (MagicCardHash) super.clone();
		if (this.fields != null)
			obj.fields = (LinkedHashMap<ICardField, Object>) this.fields.clone();
		return obj;
	}

	@Override
	public boolean set(ICardField field, String value) {
		fields.put(field, value);
		return true;
	}

	public boolean set(ICardField field, Object value) {
		fields.put(field, value);
		return true;
	}

	@Override
	public void setLocation(Location location) {
		fields.put(MagicCardField.LOCATION, location);
	}

	protected void clear() {
		fields.clear();
	}
}
