package com.reflexit.magiccards.core.model;

import java.util.LinkedHashMap;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class MagicCardHash extends AbstractMagicCard {
	private LinkedHashMap<ICardField, Object> fields = new LinkedHashMap<ICardField, Object>();

	@Override
	public Object get(ICardField field) {
		return fields.get(field);
	}

	protected Object doGet(ICardField field) {
		return fields.get(field);
	}

	protected boolean containsKey(ICardField field) {
		return fields.containsKey(field);
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
