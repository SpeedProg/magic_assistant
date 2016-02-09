package com.reflexit.magiccards.core.model;

import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.model.abs.ICardVisitor;
import com.reflexit.magiccards.core.model.expr.TextValue;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;

public abstract class AbstractMagicCard implements ICard, ICardModifiable, IMagicCard, IMagicCardPhysical, Cloneable {
	@Override
	public String getString(ICardField field) {
		Object v = get(field);
		if (v == null)
			return null;
		if (v instanceof String)
			return (String) v;
		return String.valueOf(v);
	}

	@Override
	public int getInt(ICardField field) {
		Object v = get(field);
		if (v instanceof String) {
			return Integer.valueOf((String) v);
		}
		Integer x = (Integer) v;
		if (x == null)
			return 0;
		return x;
	}

	@Override
	public float getFloat(ICardField field) {
		Object object = get(field);
		if (object instanceof String) {
			return convertFloat((String) object);
		}
		Float x = (Float) object;
		if (x == null)
			return 0;
		return x;
	}

	public boolean getBoolean(ICardField field) {
		Object v = get(field);
		if (v == null)
			return false;
		if (v instanceof String) {
			return Boolean.valueOf((String) v);
		}
		return (Boolean) v;
	}

	public static float convertFloat(String str) {
		float t;
		if (str == null || str.length() == 0)
			t = NOT_APPLICABLE_POWER;
		else {
			try {
				t = Float.parseFloat(str);
			} catch (NumberFormatException e) {
				// if (str.contains("*"))
				t = STAR_POWER;
			}
		}
		return t;
	}

	@Override
	public IMagicCard cloneCard() {
		try {
			return (AbstractMagicCard) clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public Location getLocation() {
		return (Location) get(MagicCardField.LOCATION);
	}

	@Override
	public int getCount() {
		return getInt(MagicCardField.COUNT);
	}

	@Override
	public String getComment() {
		return getString(MagicCardField.COMMENT);
	}

	@Override
	public boolean isOwn() {
		Object v = get(MagicCardField.OWNERSHIP);
		if (v == null)
			return false;
		return (Boolean) v;
	}

	@Override
	public int getForTrade() {
		return getInt(MagicCardField.FORTRADECOUNT);
	}

	@Override
	public String getSpecial() {
		return getString(MagicCardField.SPECIAL);
	}

	@Override
	public boolean isSideboard() {
		Location v = getLocation();
		if (v == null)
			return false;
		return v.isSideboard();
	}

	@Override
	public int getOwnCount() {
		return getInt(MagicCardField.OWN_COUNT);
	}

	@Override
	public int getOwnUnique() {
		return getInt(MagicCardField.OWN_UNIQUE);
	}

	public boolean isForTrade() {
		return isSpecialTag(MagicCardField.FORTRADECOUNT.specialTag());
	}

	public boolean isSpecialTag(String key) {
		String str = SpecialTags.getInstance().getSpecialValue(getSpecial(), key);
		return Boolean.valueOf(str);
	}

	@Override
	public int getOwnTotalAll() {
		Collection<IMagicCard> cards = db().getCandidates(getName());
		int sum = 0;
		for (IMagicCard card : cards) {
			if (card instanceof MagicCard)
				sum += ((MagicCard) card).getOwnCount();
		}
		return sum;
	}

	public IDbCardStore<IMagicCard> db() {
		return DataManager.getInstance().getMagicDBStore();
	}

	@Override
	public float getPrice() {
		return getFloat(MagicCardField.PRICE);
	}

	@Override
	public boolean isPhysical() {
		return false;
	}

	@Override
	public String getCost() {
		return getString(MagicCardField.COST);
	}

	@Override
	public int getCardId() {
		return getInt(MagicCardField.ID);
	}

	@Override
	public int getGathererId() {
		int id = getCardId();
		if (id > 0)
			return id;
		if (id < 0 && (id & (1 << 30)) != 0)
			return -id;
		return 0;
	}

	@Override
	public String getOracleText() {
		return getString(MagicCardField.ORACLE);
	}

	@Override
	public String getRarity() {
		return getString(MagicCardField.RARITY);
	}

	@Override
	public String getSet() {
		return getString(MagicCardField.SET);
	}

	@Override
	public String getType() {
		return getString(MagicCardField.TYPE);
	}

	@Override
	public String getPower() {
		return getString(MagicCardField.POWER);
	}

	@Override
	public String getToughness() {
		return getString(MagicCardField.TOUGHNESS);
	}

	@Override
	public String getColorType() {
		return getString(MagicCardField.CTYPE);
	}

	@Override
	public int getCmc() {
		return getInt(MagicCardField.CMC);
	}

	@Override
	public float getDbPrice() {
		return getFloat(MagicCardField.DBPRICE);
	}

	@Override
	public float getCommunityRating() {
		return getFloat(MagicCardField.RATING);
	}

	@Override
	public String getArtist() {
		return getString(MagicCardField.ARTIST);
	}

	@Override
	public String getRulings() {
		return getString(MagicCardField.RULINGS);
	}

	@Override
	public MagicCard getBase() {
		return null;
	}

	@Override
	public String getText() {
		return getString(MagicCardField.TEXT);
	}

	@Override
	public String getLanguage() {
		return getString(MagicCardField.LANG);
	}

	@Override
	public boolean matches(ICardField left, TextValue right) {
		return matches(this, left, right);
	}

	public static boolean matches(IMagicCard card, ICardField left, TextValue right) {
		String value = String.valueOf(card.get(left));
		if (left == MagicCardField.TYPE && !right.regex) {
			return CardTypes.getInstance().hasType(card, right.getText());
		}
		return right.getPattern().matcher(value).find();
	}

	@Override
	public int getEnglishCardId() {
		return getInt(MagicCardField.ENID);
	}

	@Override
	public int getFlipId() {
		return getInt(MagicCardField.FLIPID);
	}

	@Override
	public int getUniqueCount() {
		return getInt(MagicCardField.UNIQUE_COUNT);
	}

	@Override
	public int getSide() {
		return getInt(MagicCardField.SIDE);
	}

	@Override
	public int getCollectorNumberId() {
		String num = getString(MagicCardField.COLLNUM);
		if (num == null)
			return 0;
		try {
			return Integer.parseInt(num);
		} catch (NumberFormatException e) {
			try {
				return Integer.parseInt(num.substring(0, num.length() - 1));
			} catch (Exception e1) {
				return 0;
			}
		}
	}

	@Override
	public LegalityMap getLegalityMap() {
		return (LegalityMap) get(MagicCardField.LEGALITY);
	}

	private static CardTypes MTYPES = CardTypes.getInstance();

	@Override
	public boolean isBasicLand() {
		if (isLand()) {
			if (MTYPES.hasType(this, CardTypes.TYPES.Type_Basic)) {
				return true;
			}
		}
		return false;
	}

	public boolean isLand() {
		String cost = getCost();
		if (cost != null && cost.length() > 0)
			return false;
		if (MTYPES.hasType(this, CardTypes.TYPES.Type_Land)) {
			return true;
		}
		return false;
	}

	@Override
	public Object accept(ICardVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public String getEnglishName() {
		int x = getEnglishCardId();
		if (x == 0)
			return getName();
		IMagicCard norm = db().getCard(x);
		if (norm == null)
			norm = this;
		return norm.getName();
	}

	@Override
	public Edition getEdition() {
		return ed().getEditionByNameAlways(getSet());
	}

	protected Editions ed() {
		return Editions.getInstance();
	}
}
