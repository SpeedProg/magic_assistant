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

	public float getDbPrice() {
		return card.getDbPrice();
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
		}
		// TODO Auto-generated method stub
		return null;
	}

	public void setDbPrice(Float price2) {
		card.setDbPrice(price2);
	}
}
