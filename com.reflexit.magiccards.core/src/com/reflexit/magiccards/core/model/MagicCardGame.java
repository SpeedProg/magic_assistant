package com.reflexit.magiccards.core.model;

import java.util.HashMap;
import java.util.Locale;

public class MagicCardGame extends AbstractMagicCard implements IMagicCard {
	public enum MagicCardGameField implements ICardField {
		ZONE,
		TAPPED,
		FACEDOWN;
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

		@Override
		public String getTag() {
			String name = name();
			name = name.toLowerCase(Locale.ENGLISH);
			return name;
		}
	};

	enum Zones {
		LIBRARY,
		SCRY,
		HAND,
		GRAVEYARD,
		BATTLEFIELD,
		EXILE
	}

	private MagicCard card;
	private HashMap<ICardField, Object> properties;

	public MagicCardGame() {
		properties = new HashMap<ICardField, Object>(3);
		set(MagicCardGameField.ZONE, Zones.LIBRARY);
		set(MagicCardGameField.FACEDOWN, false);
	}

	@Override
	public Object get(ICardField field) {
		return card.get(field);
	}

	@Override
	public boolean set(ICardField field, Object value) {
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
}
