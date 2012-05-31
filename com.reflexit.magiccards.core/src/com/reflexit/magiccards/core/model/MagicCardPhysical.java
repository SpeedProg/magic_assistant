package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.model.MagicCardFilter.TextValue;

public class MagicCardPhysical implements IMagicCard, ICardCountable, ICardModifiable {
	private MagicCard card;
	private int count;
	private float price;
	private String comment;
	private transient Location location;
	private String custom;
	private boolean ownership;
	private int forTrade;
	private String special;

	public MagicCardPhysical(IMagicCard card, Location location) {
		if (card instanceof MagicCard) {
			this.card = (MagicCard) card;
			this.count = 1;
			this.ownership = false;
			this.forTrade = 0;
			this.special = null;
		} else if (card instanceof MagicCardPhysical) {
			MagicCardPhysical phi = (MagicCardPhysical) card;
			this.card = phi.getCard();
			this.count = phi.getCount();
			this.comment = phi.getComment();
			this.custom = phi.getCustom();
			this.price = phi.getPrice();
			this.ownership = phi.ownership;
			this.forTrade = phi.forTrade;
			this.special = phi.special;
		}
		if (location == null)
			this.location = Location.NO_WHERE;
		this.location = location;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
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
		return this.price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getComment() {
		if (this.comment == null)
			return "";
		return this.comment;
	}

	public void setComment(String comment) {
		if (comment == null || comment.trim().length() == 0)
			this.comment = null;
		else
			this.comment = comment.trim();
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getCustom() {
		return this.custom;
	}

	public void setCustom(String cutom) {
		this.custom = cutom;
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

	public boolean isOwn() {
		return this.ownership;
	}

	public void setOwn(boolean ownership) {
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
		if (obj instanceof IMagicCard)
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
		if (Math.abs(phi1.price - phi2.price) >= 0.01)
			return false;
		if (!eqNull(phi1.getCustom(), phi2.getCustom()))
			return false;
		if (!eqNull(phi1.getComment(), phi2.getComment()))
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
		boolean is = card.setObjectByField(field, value);
		if (is == true)
			return true;
		if (!(field instanceof MagicCardFieldPhysical))
			return false;
		MagicCardFieldPhysical pfield = (MagicCardFieldPhysical) field;
		switch (pfield) {
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
			setLocation(new Location(value));
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
		default:
			return false;
		}
		return true;
	}

	public Object getObjectByField(ICardField field) {
		Object x = card.getObjectByField(field);
		if (x != null)
			return x;
		if (!(field instanceof MagicCardFieldPhysical))
			return null;
		MagicCardFieldPhysical pfield = (MagicCardFieldPhysical) field;
		switch (pfield) {
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
		}
		return null;
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

	public int getForTrade() {
		return forTrade;
	}

	public void setForTrade(int forSale) {
		this.forTrade = forSale;
	}

	public String getSpecial() {
		if (this.special == null)
			return "";
		return special;
	}

	public void setSpecial(String special) {
		if (special == null || special.trim().length() == 0) {
			this.special = null;
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
						addTag(value, tag);
					else
						value = tag + ",";
				}
			}
			this.special = value;
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

	public boolean isSideboard() {
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
		return card.matches(left, right);
	}

	public int getEnglishCardId() {
		return card.getEnglishCardId();
	}

	public int getFlipId() {
		return card.getFlipId();
	}
}
