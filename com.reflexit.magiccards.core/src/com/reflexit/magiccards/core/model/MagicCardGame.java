package com.reflexit.magiccards.core.model;

import java.util.HashMap;
import java.util.Locale;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class MagicCardGame extends AbstractMagicCard implements IMagicCard {
	public enum MagicCardGameField implements ICardField {
		ZONE,
		TAPPED,
		DRAWID,
		NOTE;
		@Override
		public boolean isTransient() {
			return true;
		}

		@Override
		public String getLabel() {
			String name = name();
			name = name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
			name = name.replace('_', ' ');
			return name;
		}

		@Override
		public Object aggregateValueOf(ICard card) {
			return card.get(this);
		}
	};

	public enum Zone {
		BATTLEFIELD,
		HAND,
		SCRY,
		GRAVEYARD,
		EXILE,
		LIBRARY,
		SIDEBOARD;
		public String getLabel() {
			String name = name();
			name = name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
			name = name.replace('_', ' ');
			return name;
		}
	}

	private MagicCard card;
	private HashMap<ICardField, Object> properties;

	public MagicCardGame(IMagicCard elem) {
		card = elem.getBase();
		properties = new HashMap<ICardField, Object>(3);
		set(MagicCardGameField.ZONE, Zone.LIBRARY);
	}

	@Override
	public MagicCard getBase() {
		return card;
	}

	@Override
	public Object get(ICardField field) {
		if (field instanceof MagicCardGameField)
			return properties.get(field);
		return card.get(field);
	}

	@Override
	public boolean set(ICardField field, Object value) {
		if (field instanceof MagicCardGameField) {
			if (value == null)
				properties.remove(field);
			else
				properties.put(field, value);
			return true;
		} else
			return card.set(field, value);
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return card.getName();
	}

	public Zone getZone() {
		return (Zone) get(MagicCardGameField.ZONE);
	}

	public void setZone(Zone zone) {
		if (getZone() != zone) {
			set(MagicCardGameField.ZONE, zone);
			setTapped(false);
		}
	}

	public void setTapped(boolean value) {
		if (value)
			set(MagicCardGameField.TAPPED, value);
		else
			set(MagicCardGameField.TAPPED, null);
	}

	public boolean isTapped() {
		return getBoolean(MagicCardGameField.TAPPED);
	}

	public int getDrawId() {
		return getInt(MagicCardGameField.DRAWID);
	}

	public void setDrawId(int i) {
		set(MagicCardGameField.DRAWID, i);
	}

	@Override
	public String toString() {
		return card.getName() + " " + properties;
	}
}
