package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.reflexit.magiccards.core.model.MagicCardFilter.TextValue;

public class MagicCardPhysical implements ICardModifiable, IMagicCardPhysical, ICard {
	private MagicCard card;
	private int count;
	private transient Location location;
	private boolean ownership;
	private HashMap<ICardField, Object> properties;

	public MagicCardPhysical(IMagicCard card, Location location, boolean virtual) {
		this(card, location);
		this.ownership = !virtual;
	}

	public MagicCardPhysical(IMagicCard card, Location location) {
		if (card instanceof MagicCard) {
			this.card = (MagicCard) card;
			this.count = 1;
			this.ownership = false;
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

	public IMagicCard cloneCard() {
		return (IMagicCard) clone();
	}

	public float getDbPrice() {
		return card.getDbPrice();
	}

	public float getCommunityRating() {
		return card.getCommunityRating();
	}

	public String getArtist() {
		return card.getArtist();
	}

	public String getRulings() {
		return card.getRulings();
	}

	public Collection getHeaderNames() {
		ArrayList list = new ArrayList();
		list.addAll(this.card.getHeaderNames());
		MagicCardFieldPhysical[] values = MagicCardFieldPhysical.values();
		for (MagicCardFieldPhysical magicCardField : values) {
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

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public float getPrice() {
		Float p = (Float) getProperty(MagicCardFieldPhysical.PRICE);
		if (p == null)
			return 0;
		return p;
	}

	public void setPrice(float price) {
		setProperty(MagicCardFieldPhysical.PRICE, price);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getComment()
	 */
	public String getComment() {
		return (String) getProperty(MagicCardFieldPhysical.COMMENT);
	}

	public void setComment(String comment) {
		if (comment == null || comment.trim().length() == 0)
			setProperty(MagicCardFieldPhysical.COMMENT, null);
		else
			setProperty(MagicCardFieldPhysical.COMMENT, comment.trim());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getLocation()
	 */
	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getCustom() {
		return (String) getProperty(MagicCardFieldPhysical.CUSTOM);
	}

	public void setCustom(String custom) {
		if (custom == null || custom.trim().length() == 0)
			setProperty(MagicCardFieldPhysical.CUSTOM, null);
		else
			setProperty(MagicCardFieldPhysical.CUSTOM, custom);
	}

	public int getCardId() {
		return this.card.getCardId();
	}

	public int getCmc() {
		return this.card.getCmc();
	}

	public String getColorType() {
		return this.card.getColorType();
	}

	public String getCost() {
		return this.card.getCost();
	}

	public String getSet() {
		return this.card.getSet();
	}

	public String getName() {
		return this.card.getName();
	}

	public String getOracleText() {
		return this.card.getOracleText();
	}

	public String getPower() {
		return this.card.getPower();
	}

	public String getRarity() {
		return this.card.getRarity();
	}

	public String getToughness() {
		return this.card.getToughness();
	}

	public String getType() {
		return this.card.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#isOwn()
	 */
	public boolean isOwn() {
		return this.ownership;
	}

	public void setOwn(boolean ownership) {
		if (count <= 0)
			throw new IllegalArgumentException("Cannot have 0 count for own cards");
		this.ownership = ownership;
	}

	@Override
	public int hashCode() {
		return this.card.hashCode();
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

	public boolean setObjectByField(ICardField field, String value) {
		if (field instanceof MagicCardFieldPhysical) {
			switch ((MagicCardFieldPhysical) field) {
				case COUNT:
					setCount(Integer.parseInt(value));
					break;
				case PRICE:
					setPrice(Float.parseFloat(value));
					break;
				case COMMENT:
					setComment(value);
					break;
				case LOCATION:
					setLocation(Location.valueOf(value));
					break;
				case CUSTOM:
					setCustom(value);
					break;
				case OWNERSHIP:
					setOwn(Boolean.parseBoolean(value));
					break;
				case SPECIAL:
					setSpecial(value);
					break;
				case FORTRADECOUNT:
					setForTrade(Integer.parseInt(value));
					break;
				case SIDEBOARD:
					return false; // not settable
				case OWN_COUNT:
					break; // calculated
				case OWN_UNIQUE:
					break; // calculated
				case ERROR:
					setError(value);
					break;
				default:
					throw new IllegalArgumentException("Not supported field?");
			}
			return true;
		} else if (field instanceof MagicCardField) {
			return card.setObjectByField(field, value);
		}
		return false;
	}

	public Object getObjectByField(ICardField field) {
		if (field instanceof MagicCardField) {
			return card.getObjectByField(field);
		} else if (field instanceof MagicCardFieldPhysical) {
			switch ((MagicCardFieldPhysical) field) {
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
				case ERROR:
					return getError();
				default:
					throw new IllegalArgumentException("Not supported field?");
			}
		}
		return null;
	}

	public Object getError() {
		return getProperty(MagicCardFieldPhysical.ERROR);
	}

	public void setError(Object value) {
		setProperty(MagicCardFieldPhysical.ERROR, value);
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
	public int getForTrade() {
		Integer f = (Integer) getProperty(MagicCardFieldPhysical.FORTRADECOUNT);
		if (f == null)
			return 0;
		return f;
	}

	public void setForTrade(int forTrade) {
		setProperty(MagicCardFieldPhysical.FORTRADECOUNT, forTrade);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCardPhysical#getSpecial()
	 */
	public String getSpecial() {
		String f = (String) getProperty(MagicCardFieldPhysical.SPECIAL);
		if (f == null)
			return "";
		return f;
	}

	public void setSpecial(String special) {
		if (special == null || special.trim().length() == 0) {
			setProperty(MagicCardFieldPhysical.SPECIAL, null);
			return;
		} else {
			String value = getSpecial();
			String tags[] = special.trim().split(",");
			boolean add = false;
			for (String tag : tags) {
				tag = tag.trim();
				if (tag.length() == 0)
					continue;
				if (tag.startsWith("+")) {
					tag = tag.substring(1);
					value = addTag(value, tag);
					add = true;
				} else if (tag.startsWith("-")) {
					tag = tag.substring(1);
					value = removeTag(value, tag);
					add = true;
				} else {
					if (add)
						value = addTag(value, tag);
					else {
						value = tag + ",";
						add = true;
					}
				}
			}
			if (value.endsWith(","))
				value = value.substring(0, value.length() - 1);
			setProperty(MagicCardFieldPhysical.SPECIAL, value);
		}
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
	public boolean isSideboard() {
		if (location == null)
			return false;
		return location.isSideboard();
	}

	public void setDbPrice(float dbprice) {
		getCard().setDbPrice(dbprice);
	}

	public MagicCard getBase() {
		return card;
	}

	public String getText() {
		return card.getText();
	}

	public String getLanguage() {
		return card.getLanguage();
	}

	public boolean matches(ICardField left, TextValue right) {
		String value = String.valueOf(getObjectByField(left));
		if (left == MagicCardField.TYPE && !right.regex) {
			return CardTypes.getInstance().hasType(this, right.getText());
		}
		return right.getPattern().matcher(value).find();
	}

	public int getEnglishCardId() {
		return card.getEnglishCardId();
	}

	public int getFlipId() {
		return card.getFlipId();
	}

	@Override
	public int getGathererId() {
		return card.getGathererId();
	}

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
	public int getOwnTotalAll() {
		return getBase().getOwnTotalAll();
	}

	public int getOwnUnique() {
		if (isOwn())
			return 1;
		return 0;
	}

	public int getUniqueCount() {
		return 1;
	}

	public boolean isPhysical() {
		return true;
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		ICardField[] xfields = MagicCardFieldPhysical.allNonTransientFields();
		for (ICardField field : xfields) {
			list.add(getObjectByField(field));
		}
		return list;
	}

	public int getSide() {
		return card.getSide();
	}

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
}
