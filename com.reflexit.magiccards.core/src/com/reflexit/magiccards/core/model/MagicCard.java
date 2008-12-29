package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.Activator;

public class MagicCard implements IMagicCard {
	int id;
	String name;
	String cost;
	String type;
	String power;
	String toughness;
	String edition;
	String rarity;
	String oracleText;
	transient String colorType = null;
	transient int cmc = -1;
	transient float fpower = -STAR_POWER;
	transient float ftoughness = -STAR_POWER;

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCost()
	 */
	public String getCost() {
		return this.cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCardId()
	 */
	public int getCardId() {
		return this.id;
	}

	public void setCardId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getName()
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getOracleText()
	 */
	public String getOracleText() {
		return this.oracleText;
	}

	public void setOracleText(String oracleText) {
		this.oracleText = oracleText;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getRarity()
	 */
	public String getRarity() {
		return this.rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getEdition()
	 */
	public String getEdition() {
		return this.edition;
	}

	public void setEdition(String setAbbr) {
		this.edition = setAbbr;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getType()
	 */
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setId(String match) {
		int i = Integer.parseInt(match);
		setCardId(i);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getPower()
	 */
	public String getPower() {
		return this.power;
	}

	public void setPower(String power) {
		this.power = power;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getToughness()
	 */
	public String getToughness() {
		return this.toughness;
	}

	public void setToughness(String toughness) {
		this.toughness = toughness;
	}

	public static float convertFloat(String str) {
		float t;
		if (str == null || str.length() == 0)
			t = NOT_APPLICABLE_POWER;
		else if (str.equals("*")) {
			t = STAR_POWER;
		} else if (str.equals("1+*")) {
			t = STAR_POWER + 1;
		} else if (str.equals("2+*")) {
			t = STAR_POWER + 2;
		} else if (str.equals("*{^2}")) {
			t = STAR_POWER + 3;
		} else {
			if (str.contains("/"))
				str = str.replaceAll("\\Q{1/2}", ".5");
			try {
				t = Float.parseFloat(str);
			} catch (NumberFormatException e) {
				Activator.log(e);
				t = STAR_POWER;
			}
		}
		return t;
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		list.add(new Integer(this.id));
		list.add(this.name);
		list.add(this.cost);
		list.add(this.type);
		list.add(this.power);
		list.add(this.toughness);
		list.add(this.oracleText);
		list.add(this.edition);
		list.add(this.rarity);
		list.add(getColorType());
		list.add(new Integer(getCmc()));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getColorType()
	 */
	public String getColorType() {
		if (this.colorType == null)
			setExtraFields();
		return this.colorType;
	}

	public void setColorType(String colorType) {
		this.colorType = colorType;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCmc()
	 */
	public int getCmc() {
		if (this.cmc == -1)
			setExtraFields();
		return this.cmc;
	}

	public void setCmc(int cmc) {
		this.cmc = cmc;
	}

	public void setCmc(String cmc) {
		setCmc(Integer.parseInt(cmc));
	}

	public void setValues(String[] fields) {
		setId(fields[0]);
		setName(fields[1]);
		setCost(fields[2]);
		setType(fields[3]);
		setPower(fields[4]);
		setToughness(fields[5]);
		setOracleText(fields[6]);
		setEdition(fields[7]);
		setRarity(fields[8]);
		if (fields.length > 9)
			setColorType(fields[9]);
		if (fields.length > 10)
			setCmc(fields[10]);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getByIndex(int)
	 */
	public String getByIndex(int i) {
		Object elem = getObjectByIndex(i);
		if (elem == null)
			return null;
		return elem.toString();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getObjectByIndex(int)
	 */
	public Object getObjectByIndex(int i) {
		return ((ArrayList) getValues()).get(i);
	}

	@Override
	public int hashCode() {
		if (this.id != 0)
			return this.id;
		return this.name != null ? this.name.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MagicCard))
			return false;
		if (obj == this)
			return true;
		MagicCard ma = (MagicCard) obj;
		if (this.id != 0)
			return this.id == ma.id;
		if (this.name != null)
			return this.name.equals(ma.name);
		return false;
	}

	@Override
	public String toString() {
		return this.id + ": " + this.name;
	}

	public void setExtraFields() {
		try {
			this.cost = this.cost == null ? "" : this.cost.trim();
			setColorType(Colors.getInstance().getColorType(this.cost));
			setCmc(Colors.getInstance().getConvertedManaCost(this.cost));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Collection getHeaderNames() {
		MagicCardField[] values = MagicCardField.values();
		ArrayList list = new ArrayList();
		for (MagicCardField magicCardField : values) {
			list.add(magicCardField.toString());
		}
		return list;
	}

	public Object getObjectByField(ICardField field) {
		if (!(field instanceof MagicCardField))
			return null;
		MagicCardField mf = (MagicCardField) field;
		switch (mf) {
		case ID:
			return Integer.valueOf(getCardId());
		case NAME:
			return (this.name);
		case COST:
			return (this.cost);
		case TYPE:
			return (this.type);
		case POWER:
			return (this.power);
		case TOUGHNESS:
			return (this.toughness);
		case ORACLE:
			return (this.oracleText);
		case EDITION:
			return (this.edition);
		case RARITY:
			return (this.rarity);
		case CTYPE:
			return (getColorType());
		case CMC:
			return (new Integer(getCmc()));
		default:
			break;
		}
		return null;
	}
}
