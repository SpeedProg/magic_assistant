package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

public class MagicCardPhisical implements IMagicCard, ICardCountable {
	private MagicCard card;
	private int count;
	private float price;
	private String comment;
	private String location;
	private String condition;
	private boolean ownership;
	public static final int INDEX_COUNT = 11;
	public static final int INDEX_LOCATION = 14;
	public static final int INDEX_OWNERSHIP = 16;

	public MagicCardPhisical(IMagicCard card) {
		if (card instanceof MagicCard) {
			this.card = (MagicCard) card;
			this.count = 1;
		} else if (card instanceof MagicCardPhisical) {
			MagicCardPhisical phi = (MagicCardPhisical) card;
			this.card = phi.getCard();
			this.count = 1;
		}
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		list.addAll(this.card.getValues());
		list.add(new Integer(this.count));
		list.add(new Float(this.price));
		list.add(this.comment);
		list.add(this.location);
		list.add(this.condition);
		list.add(new Boolean(this.ownership));
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
		this.condition = fields[i + 5];
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

	public String getCondition() {
		return this.condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
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
}
