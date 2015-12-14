package com.reflexit.magiccards.core.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.model.abs.ICardVisitor;
import com.reflexit.magiccards.core.model.expr.TextValue;

public class MagicCardPhysical extends AbstractMagicCard implements ICardModifiable, IMagicCardPhysical,
		ICard {
	private MagicCard card;
	private int count;
	private transient Location location;
	private boolean ownership;
	private Date date;
	private HashMap<ICardField, Object> properties;
	private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
			Locale.US);

	public MagicCardPhysical(IMagicCard card, Location location, boolean virtual) {
		this(card, location);
		if (card == null)
			throw new NullPointerException();
		this.ownership = !virtual;
	}

	public MagicCardPhysical(IMagicCard card, Location location) {
		if (card instanceof ICardGroup) {
			IMagicCard card1 = (IMagicCard) ((ICardGroup) card).getFirstCard();
			if (card1 != null)
				card = card1;
		}
		if (card instanceof MagicCard) {
			this.card = (MagicCard) card;
			this.count = 1;
			this.ownership = false;
			setDate(new Date(System.currentTimeMillis()));
		} else if (card instanceof MagicCardPhysical) {
			MagicCardPhysical phi = (MagicCardPhysical) card;
			this.card = phi.getCard();
			this.count = phi.getCount();
			this.ownership = phi.ownership;
			this.date = phi.getDate();
			this.properties = (HashMap<ICardField, Object>) (phi.properties == null ? null : phi.properties
					.clone());
		}
		this.location = location;
		if (this.card == null)
			throw new NullPointerException();
	}

	@Override
	public Object clone() {
		try {
			MagicCardPhysical obj = (MagicCardPhysical) super.clone();
			if (obj.properties != null)
				obj.properties = (HashMap<ICardField, Object>) this.properties.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public IMagicCard cloneCard() {
		return (IMagicCard) clone();
	}

	@Override
	public float getDbPrice() {
		return card.getDbPrice();
	}

	@Override
	public float getCommunityRating() {
		return card.getCommunityRating();
	}

	@Override
	public String getArtist() {
		return card.getArtist();
	}

	@Override
	public String getRulings() {
		return card.getRulings();
	}

	public MagicCard getCard() {
		return this.card;
	}

	public void setMagicCard(MagicCard card) {
		this.card = card;
		if (this.card == null)
			throw new NullPointerException();
	}

	@Override
	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public float getPrice() {
		Float p = (Float) getProperty(MagicCardField.PRICE);
		if (p == null)
			return 0;
		return p;
	}

	public void setPrice(float price) {
		setProperty(MagicCardField.PRICE, price);
	}

	@Override
	public String getComment() {
		return (String) getProperty(MagicCardField.COMMENT);
	}

	public void setComment(String comment) {
		if (comment == null || comment.length() == 0)
			setProperty(MagicCardField.COMMENT, null);
		else
			setProperty(MagicCardField.COMMENT, comment.trim());
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(Location location) {
		this.location = location;
	}

	public String getCustom() {
		return (String) getProperty(MagicCardField.CUSTOM);
	}

	public void setCustom(String custom) {
		if (custom == null || custom.trim().length() == 0)
			setProperty(MagicCardField.CUSTOM, null);
		else
			setProperty(MagicCardField.CUSTOM, custom);
	}

	@Override
	public int getCardId() {
		return this.card.getCardId();
	}

	@Override
	public int getCmc() {
		return this.card.getCmc();
	}

	@Override
	public String getColorType() {
		return this.card.getColorType();
	}

	@Override
	public String getCost() {
		return this.card.getCost();
	}

	@Override
	public String getSet() {
		return this.card.getSet();
	}

	@Override
	public String getName() {
		return this.card.getName();
	}

	@Override
	public String getOracleText() {
		return this.card.getOracleText();
	}

	@Override
	public String getPower() {
		return this.card.getPower();
	}

	@Override
	public String getRarity() {
		return this.card.getRarity();
	}

	@Override
	public String getToughness() {
		return this.card.getToughness();
	}

	@Override
	public String getType() {
		return this.card.getType();
	}

	@Override
	public boolean isOwn() {
		return this.ownership;
	}

	public void setOwn(boolean ownership) {
		this.ownership = ownership;
	}

	@Override
	public int hashCode() {
		int hash = System.identityHashCode(this);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof MagicCardPhysical) {
			MagicCardPhysical phi = (MagicCardPhysical) obj;
			if (this.getCount() != phi.getCount())
				return false;
			if (!this.matching(phi))
				return false;
			if (!eqNull(this.getLocation(), phi.getLocation()))
				return false;
			return true;
		}
		if (obj instanceof MagicCard)
			return this.card.equals(obj);
		return false;
	}

	/**
	 * Kind of equals but ignores count and location
	 *
	 * @param phi2
	 * @return
	 */
	public boolean matching(MagicCardPhysical phi2) {
		MagicCardPhysical phi1 = this;
		if (!phi1.card.equals(phi2.card))
			return false;
		if (phi1.isOwn() != phi2.isOwn())
			return false;
		if (!eqNull(phi1.getCustom(), phi2.getCustom()))
			return false;
		if (!eqNull(phi1.getComment(), phi2.getComment()))
			return false;
		if (!eqNull(phi1.getSpecial(), phi2.getSpecial()))
			return false;
		if (!eqNull(phi1.getPrice(), phi2.getPrice()))
			return false;
		return true;
	}

	public static boolean eqNull(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	@Override
	public String toString() {
		return this.card.toString() + " x " + this.count;
	}

	@Override
	public boolean set(ICardField field, Object value) {
		((MagicCardField) field).setM(this, value);
		return true;
	}

	@Override
	public Object get(ICardField field) {
		return ((MagicCardField) field).get(this);
	}

	public int getCreatureCount() {
		if (card.getPower() == null)
			return 0;
		if (!card.getPower().isEmpty()) {
			return card.getCount();
		}
		return 0;
	}

	public int getCount4() {
		int c = getOwnCount();
		if (c > 4)
			return 4;
		return c;
	}

	public Object getError() {
		return getProperty(MagicCardField.ERROR);
	}

	public void setError(Object value) {
		setProperty(MagicCardField.ERROR, value);
	}

	public void setArtist(String artist) {
		card.setArtist(artist);
	}

	@Override
	public String getSpecial() {
		String f = (String) getProperty(MagicCardField.SPECIAL);
		if (f == null)
			return "";
		return f;
	}

	public String getSpecialTagValue(String key) {
		return SpecialTags.getInstance().getSpecialValue(getSpecial(), key);
	}

	@Override
	public boolean isSpecialTag(String key) {
		String str = SpecialTags.getInstance().getSpecialValue(getSpecial(), key);
		return Boolean.valueOf(str);
	}

	public boolean isSpecialTag(MagicCardField field) {
		return isSpecialTag(field.specialTag());
	}

	public void setSpecialTag(String special) {
		SpecialTags.getInstance().modifySpecial(this, special);
	}

	public void setSpecialTag(MagicCardField field) {
		setSpecialTag(field.specialTag());
	}

	public void removeSpecialTag(MagicCardField field) {
		removeSpecialTag(field.specialTag());
	}

	public void removeSpecialTag(String tag) {
		SpecialTags.getInstance().modifySpecial(this, "-" + tag);
	}

	public void setSpecial(String special) {
		if (special == null || special.trim().length() == 0) {
			setProperty(MagicCardField.SPECIAL, null);
			return;
		}
		setProperty(MagicCardField.SPECIAL, special);
	}

	@Override
	public boolean isSideboard() {
		if (location == null)
			return false;
		return location.isSideboard();
	}

	public void setDbPrice(float dbprice) {
		getCard().setDbPrice(dbprice);
	}

	@Override
	public MagicCard getBase() {
		return card;
	}

	@Override
	public String getText() {
		return card.getText();
	}

	@Override
	public String getLanguage() {
		return card.getLanguage();
	}

	@Override
	public boolean matches(ICardField left, TextValue right) {
		String value = String.valueOf(get(left));
		if (left == MagicCardField.TYPE && !right.regex) {
			return CardTypes.getInstance().hasType(this,
					right.getText());
		}
		return right.getPattern().matcher(value).find();
	}

	@Override
	public int getEnglishCardId() {
		return card.getEnglishCardId();
	}

	@Override
	public int getFlipId() {
		return card.getFlipId();
	}

	@Override
	public int getGathererId() {
		return card.getGathererId();
	}

	@Override
	public int getOwnCount() {
		if (isOwn())
			return getCount();
		return 0;
	}

	/**
	 * This card total in all collections (same set)
	 *
	 * @return
	 */
	public int getOwnTotal() {
		return getBase().getOwnCount();
	}

	/**
	 * This card total ignoring set
	 *
	 * @return
	 */
	@Override
	public int getOwnTotalAll() {
		return getBase().getOwnTotalAll();
	}

	@Override
	public int getOwnUnique() {
		if (isOwn())
			return 1;
		return 0;
	}

	@Override
	public int getUniqueCount() {
		return 1;
	}

	@Override
	public boolean isPhysical() {
		return true;
	}

	@Override
	public int getSide() {
		return card.getSide();
	}

	@Override
	public int getCollectorNumberId() {
		return card.getCollectorNumberId();
	}

	public void setProperty(ICardField key, Object value) {
		if (key == null)
			throw new NullPointerException();
		if (properties == null) {
			if (value == null)
				return;
			properties = new HashMap<ICardField, Object>(3);
		}
		if (value == null) {
			properties.remove(key);
			if (properties.size() == 0) {
				properties = null;
			}
		} else
			properties.put(key, value);
	}

	public Object getProperty(ICardField key) {
		if (properties == null)
			return null;
		if (key == null)
			throw new NullPointerException();
		return properties.get(key);
	}

	public Map<ICardField, Object> getProperties() {
		return properties;
	}

	@Override
	public LegalityMap getLegalityMap() {
		return getBase().getLegalityMap();
	}

	@Override
	public boolean isBasicLand() {
		return getBase().isBasicLand();
	}

	@Override
	public Object accept(ICardVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int getForTrade() {
		Integer f = (Integer) getProperty(MagicCardField.FORTRADECOUNT);
		if (f == null) {
			boolean forTrade = isSpecialTag(MagicCardField.FORTRADECOUNT);
			if (forTrade)
				return getCount();
			return 0;
		} else
			return f;
	}

	public MagicCardPhysical tradeSplit(int count, int fcount) {
		MagicCardPhysical mcp = this;
		mcp.setProperty(MagicCardField.FORTRADECOUNT, null);
		mcp.removeSpecialTag(MagicCardField.FORTRADECOUNT); // remove forTrade tag if it was set
		count = Math.max(0, count);
		fcount = Math.max(0, fcount);
		// trade count is 0 or less, so card is not for trade
		if (fcount == 0) {
			mcp.setCount(count);
			return null;
		}
		MagicLogger.log("Migration activated for '" + mcp + "' trade count " + fcount);
		// trade count is bigger or equal count, so all pile is for trade
		if (fcount >= count) {
			mcp.setCount(fcount);
			mcp.setSpecialTag(MagicCardField.FORTRADECOUNT);
			MagicLogger.log("** Card '" + mcp + "' is tagged with fortrade with count of " + fcount);
			return null;
		}
		// trade count is less then count but bigger then 0
		mcp.setCount(count - fcount); // adjust count for only count not for trade
		MagicLogger.log("** Card '" + mcp + "' is tagged with NOT fortrade with count of " + mcp.getCount());
		// now deal with copy
		MagicCardPhysical mct = (MagicCardPhysical) mcp.cloneCard();
		mct.setCount(fcount);
		mct.setSpecialTag(MagicCardField.FORTRADECOUNT);
		MagicLogger.log("** Card '" + mct + "' is tagged with fortrade with count of " + mct.getCount());
		return mct;
	}

	public boolean isMigrated() {
		if (getProperty(MagicCardField.FORTRADECOUNT) != null)
			return false;
		return true;
	}

	@Override
	public String getEnglishName() {
		return card.getEnglishName();
	}

	public void setDate(String string) {
		synchronized (DATE_PARSER) {
			Date dd;
			try {
				dd = DATE_PARSER.parse(string);
			} catch (ParseException e) {
				dd = null;
				MagicLogger.log("Cannot parse date " + string);
			}
			setDate(dd);
		}
	}
}
