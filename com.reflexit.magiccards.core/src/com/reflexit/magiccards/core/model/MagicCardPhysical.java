package com.reflexit.magiccards.core.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.expr.TextValue;

public class MagicCardPhysical extends AbstractMagicCard implements ICardModifiable, IMagicCardPhysical, ICard {
	private MagicCard card;
	private int count;
	private transient Location location;
	private boolean ownership;
	private Date date;
	private HashMap<ICardField, Object> properties;
	private SimpleDateFormat DATE_PARSER = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

	public MagicCardPhysical(IMagicCard card, Location location, boolean virtual) {
		this(card, location);
		this.ownership = !virtual;
	}

	public MagicCardPhysical(IMagicCard card, Location location) {
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
			setComment(phi.getComment());
			setCustom(phi.getCustom());
			setPrice(phi.getPrice());
			setForTrade(phi.getForTrade());
			setSpecial(phi.getSpecial());
			setDate(phi.getDate());
		}
		this.location = location;
	}

	@Override
	public Object clone() {
		try {
			MagicCardPhysical obj = (MagicCardPhysical) super.clone();
			if (this.properties != null)
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

	public Collection getHeaderNames() {
		ArrayList list = new ArrayList();
		list.addAll(this.card.getHeaderNames());
		MagicCardField[] values = MagicCardField.values();
		for (MagicCardField magicCardField : values) {
			list.add(magicCardField.toString());
		}
		return list;
	}

	public MagicCard getCard() {
		return this.card;
	}

	public void setMagicCard(MagicCard card) {
		this.card = card;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getComment()
	 */
	@Override
	public String getComment() {
		return (String) getProperty(MagicCardField.COMMENT);
	}

	public void setComment(String comment) {
		if (comment == null || comment.trim().length() == 0)
			setProperty(MagicCardField.COMMENT, null);
		else
			setProperty(MagicCardField.COMMENT, comment.trim());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getLocation()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#isOwn()
	 */
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
		switch ((MagicCardField) field) {
			case COUNT:
				if (value instanceof Integer)
					setCount((Integer) value);
				else
					setCount(Integer.parseInt((String) value));
				break;
			case PRICE:
				if (value instanceof Float)
					setPrice((Float) value);
				else
					setPrice(Float.parseFloat((String) value));
				break;
			case COMMENT:
				setComment((String) value);
				break;
			case LOCATION:
				if (value instanceof Location)
					setLocation((Location) value);
				else
					setLocation(Location.valueOf((String) value));
				break;
			case CUSTOM:
				setCustom((String) value);
				break;
			case OWNERSHIP:
				if (value instanceof Boolean)
					setOwn((Boolean) value);
				else
					setOwn(Boolean.parseBoolean((String) value));
				break;
			case SPECIAL:
				setSpecial((String) value);
				break;
			case FORTRADECOUNT:
				if (value instanceof Integer)
					setForTrade((Integer) value);
				else
					setForTrade(Integer.parseInt((String) value));
				break;
			case SIDEBOARD:
				return false; // not settable
			case OWN_COUNT:
				break; // calculated
			case OWN_UNIQUE:
				break; // calculated
			case DATE: {
				if (value instanceof String) {
					Date dd;
					try {
						dd = DATE_PARSER.parse((String) value);
					} catch (ParseException e) {
						dd = null;
						MagicLogger.log("Cannot parse date " + value);
					}
					setDate(dd);
				} else {
					setDate((Date) value);
				}
				break;
			}
			case ERROR:
				setError(value);
				break;
			default:
				return card.set(field, value);
		}
		return false;
	}

	@Override
	public Object get(ICardField field) {
		switch ((MagicCardField) field) {
			case COUNT:
				return getCount();
			case PRICE:
				return getPrice();
			case COMMENT:
				return getComment();
			case LOCATION:
				return getLocation();
			case CUSTOM:
				return getCustom();
			case OWNERSHIP:
				return isOwn();
			case FORTRADECOUNT:
				return getForTrade();
			case SPECIAL:
				return getSpecial();
			case SIDEBOARD:
				return isSideboard();
			case OWN_COUNT:
				return getOwnCount();
			case OWN_UNIQUE:
				return getOwnUnique();
			case COUNT4:
				return getCount4();
			case CREATURE_COUNT:
				return getCreatureCount();
			case ERROR:
				return getError();
			case PERCENT_COMPLETE: {
				int c = getOwnCount();
				if (c > 0)
					return 100f;
				else
					return 0f;
			}
			case PERCENT4_COMPLETE: {
				int c = getCount4();
				return (float) c * 100 / 4;
			}
			case DATE:
				return getDate();
			case HASHCODE:
				return System.identityHashCode(this);
			default:
				return card.get(field);
		}
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
		int c = card.getOwnCount();
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

	public void setCommunityRating(float parseFloat) {
		card.setCommunityRating(parseFloat);
	}

	public void setArtist(String artist) {
		card.setArtist(artist);
	}

	public void setRulings(String rulings) {
		card.setRulings(rulings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getForTrade()
	 */
	@Override
	public int getForTrade() {
		Integer f = (Integer) getProperty(MagicCardField.FORTRADECOUNT);
		if (f == null)
			return 0;
		return f;
	}

	public void setForTrade(int forTrade) {
		setProperty(MagicCardField.FORTRADECOUNT, forTrade);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getSpecial()
	 */
	@Override
	public String getSpecial() {
		String f = (String) getProperty(MagicCardField.SPECIAL);
		if (f == null)
			return "";
		return f;
	}


	public String getSpecial(String key) {
		return SpecialTags.getInstance().getSpecialValue(getSpecial(), key);
	}

	public void setSpecialTag(String special) {
		SpecialTags.getInstance().modifySpecial(this, special);
	}

	public void setSpecial(String special) {
		if (special == null || special.trim().length() == 0) {
			setProperty(MagicCardField.SPECIAL, null);
			return;
		}
		setProperty(MagicCardField.SPECIAL, special);
	}

	protected String addTag(String value, String tag) {
		if (!containsTag(value, tag)) {
			value += tag + ",";
		}
		return value;
	}

	protected String removeTag(String value, String a) {
		String res = "";
		String tags[] = value.split(",");
		for (String tag : tags) {
			if (!tag.equals(a))
				res = res + tag + ",";
		}
		return res;
	}

	private boolean containsTag(String value, String a) {
		String tags[] = value.split(",");
		for (String tag : tags) {
			if (tag.equals(a))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#isSideboard()
	 */
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
			return CardTypes.getInstance().hasType(this, right.getText());
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

	public Collection getValues() {
		ArrayList list = new ArrayList();
		ICardField[] xfields = MagicCardField.allNonTransientFields(true);
		for (ICardField field : xfields) {
			list.add(get(field));
		}
		return list;
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

}
