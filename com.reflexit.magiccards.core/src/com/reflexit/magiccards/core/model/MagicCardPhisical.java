package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

public class MagicCardPhisical implements IMagicCard, ICardCountable {
	private MagicCard card;
	private int count;
	private float price;
	private String comment;
	private String location;
	private String custom;
	private boolean ownership;
	public static final int INDEX_COUNT = 11;
	public static final int INDEX_PRICE = 12;
	public static final int INDEX_COMMENT = 13;
	public static final int INDEX_LOCATION = 14;
	public static final int INDEX_CUSTOM = 15;
	public static final int INDEX_OWNERSHIP = 16;

	public MagicCardPhisical(IMagicCard card) {
		if (card instanceof MagicCard) {
			this.card = (MagicCard) card;
			this.count = 1;
			this.ownership = false;
		} else if (card instanceof MagicCardPhisical) {
			MagicCardPhisical phi = (MagicCardPhisical) card;
			this.card = phi.getCard();
			this.count = phi.getCount();
			this.comment = phi.getComment();
			this.custom = phi.getCustom();
			this.price = phi.getPrice();
			this.ownership = phi.ownership;
			this.location = phi.location;
		}
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		list.addAll(this.card.getValues());
		list.add(new Integer(this.count));
		list.add(new Float(this.price));
		list.add(this.comment);
		list.add(this.location);
		list.add(this.custom);
		list.add(new Boolean(this.ownership));
		return list;
	}

	public Collection getHeaderNames() {
		ArrayList list = new ArrayList();
		list.addAll(this.card.getHeaderNames());
		list.add("count");
		list.add("price");
		list.add("comment");
		list.add("location");
		list.add("custom");
		list.add("ownership");
		return list;
	}

	public void setValues(String[] fields) {
		this.card = new MagicCard();
		this.card.setValues(fields);
		int i = INDEX_COUNT;
		this.card.setCardId(Integer.parseInt(fields[i + 0]));
		this.count = Integer.parseInt(fields[i + 1]);
		this.price = Float.parseFloat(fields[i + 2]);
		this.comment = fields[i + 3];
		this.location = fields[i + 4];
		this.custom = fields[i + 5];
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
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCustom() {
		return this.custom;
	}

	public void setCustom(String cutom) {
		this.custom = cutom;
	}

	public String getByIndex(int columnIndex) {
		Object el = getObjectByIndex(columnIndex);
		if (el != null)
			return el.toString();
		return null;
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

	public String getEdition() {
		return this.card.getEdition();
	}

	public String getName() {
		return this.card.getName();
	}

	public Object getObjectByIndex(int i) {
		return ((ArrayList) getValues()).get(i);
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
		if (obj instanceof MagicCardPhisical) {
			MagicCardPhisical phi = (MagicCardPhisical) obj;
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
	 * Kind of equals by ignores count and location
	 * @param phi2
	 * @return
	 */
	public boolean matching(MagicCardPhisical phi2) {
		MagicCardPhisical phi1 = this;
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

	public static boolean eqNull(String a, String b) {
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
}
