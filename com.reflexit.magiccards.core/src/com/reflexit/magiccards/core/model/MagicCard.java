package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.MagicCardFilter.TextValue;

public class MagicCard implements IMagicCard, Cloneable, ICardModifiable {
	int id;
	String name;
	String cost;
	String type;
	String power;
	String toughness;
	String edition;
	String rarity;
	String oracleText;
	String artist;
	float dbprice;
	float rating;
	String lang;
	String num;
	String rulings;
	String text;
	transient String colorType = null;
	transient int cmc = -1;
	int enId;
	LinkedHashMap<String, String> properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCost()
	 */
	public String getCost() {
		return this.cost;
	}

	public void setCost(String cost) {
		this.cost = cost.intern();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCardId()
	 */
	public int getCardId() {
		return this.id;
	}

	public void setCardId(int id) {
		this.id = id;
	}

	public int getEnglishCardId() {
		return this.enId;
	}

	public void setEnglishCardId(int id) {
		this.enId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getName()
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getOracleText()
	 */
	public String getOracleText() {
		return this.oracleText;
	}

	public void setOracleText(String oracleText) {
		this.oracleText = oracleText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getRarity()
	 */
	public String getRarity() {
		return this.rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity.intern();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getEdition()
	 */
	public String getSet() {
		return this.edition;
	}

	public void setSet(String setName) {
		this.edition = setName.intern();
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getPower()
	 */
	public String getPower() {
		return this.power;
	}

	public void setPower(String power) {
		this.power = power.intern();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getToughness()
	 */
	public String getToughness() {
		return this.toughness;
	}

	public void setToughness(String toughness) {
		this.toughness = toughness.intern();
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

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCmc()
	 */
	public int getCmc() {
		if (this.colorType == null)
			setExtraFields();
		return this.cmc;
	}

	public void setCmc(int cmc) {
		this.cmc = cmc;
	}

	public void setCmc(String cmc) {
		setCmc(Integer.parseInt(cmc));
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

	public synchronized void setExtraFields() {
		try {
			this.cost = this.cost == null ? "" : this.cost.trim();
			setColorType(Colors.getInstance().getColorType(this.cost));
			setCmc(Colors.getInstance().getConvertedManaCost(this.cost));
			if (text == null)
				text = oracleText;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Collection getHeaderNames() {
		ICardField[] values = MagicCardField.allNonTransientFields();
		ArrayList list = new ArrayList();
		for (ICardField magicCardField : values) {
			list.add(magicCardField.toString());
		}
		return list;
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		ICardField[] xfields = MagicCardField.allNonTransientFields();
		for (ICardField field : xfields) {
			list.add(getObjectByField(field));
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
		case SET:
			return (this.edition);
		case RARITY:
			return (this.rarity);
		case CTYPE:
			return (getColorType());
		case CMC:
			return (Integer.valueOf(getCmc()));
		case DBPRICE:
			return (this.dbprice);
		case RATING:
			return (this.rating);
		case ARTIST:
			return (this.artist);
		case RULINGS:
			return (this.rulings);
		case LANG:
			return (this.lang);
		case COLLNUM:
			return (this.num);
		case TEXT:
			return (this.text);
		case ENID:
			return (this.enId);
		case PROPERTIES:
			return (this.properties);
		case FLIPID:
			return getProperty(MagicCardField.FLIPID.name());
		default:
			break;
		}
		return null;
	}

	public float getDbPrice() {
		return dbprice;
	}

	public void setDbPrice(float dbprice) {
		this.dbprice = dbprice;
	}

	public float getCommunityRating() {
		return rating;
	}

	public void setCommunityRating(float rating) {
		this.rating = rating;
	}

	public String getArtist() {
		return this.artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getRulings() {
		return this.rulings;
	}

	public void setRulings(String rulings) {
		this.rulings = rulings;
	}

	public String getLanguage() {
		return lang;
	}

	public void setLanguage(String lang) {
		this.lang = lang;
	}

	public String getCollNumber() {
		if (num == null)
			return "";
		return num;
	}

	public void setCollNumber(String collNumber) {
		if (collNumber == null || collNumber.trim().length() == 0)
			this.num = null;
		else
			this.num = collNumber;
	}

	public boolean setObjectByField(ICardField field, String value) {
		if (!(field instanceof MagicCardField))
			return false;
		MagicCardField mf = (MagicCardField) field;
		switch (mf) {
		case ID:
			setCardId(Integer.parseInt(value));
			break;
		case NAME:
			setName(value);
			break;
		case COST:
			setCost(value);
			break;
		case TYPE:
			setType(value);
			break;
		case POWER:
			setPower(value);
			break;
		case TOUGHNESS:
			setToughness(value);
			break;
		case ORACLE:
			setOracleText(value);
			break;
		case SET:
			setSet(value);
			break;
		case RARITY:
			setRarity(value);
			break;
		case CTYPE:
			setColorType(value);
			break;
		case CMC:
			setCmc(Integer.parseInt(value));
			break;
		case DBPRICE:
			setDbPrice(Float.parseFloat(value));
			break;
		case RATING:
			setCommunityRating(Float.parseFloat(value));
			break;
		case ARTIST:
			setArtist(value);
			break;
		case RULINGS:
			setRulings(value);
			break;
		case LANG:
			setLanguage(value);
			break;
		case COLLNUM:
			setCollNumber(value);
			break;
		case TEXT:
			setText(value);
			break;
		case ENID:
			setEnglishCardId(Integer.parseInt(value));
			break;
		case PROPERTIES:
			setProperties(value);
			break;
		case FLIPID:
			setProperty(MagicCardField.FLIPID.name(), value);
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public MagicCard cloneCard() {
		return (MagicCard) clone();
	}

	public void updateFrom(IMagicCard card) {
		ICardField[] fields = MagicCardField.allNonTransientFields();
		for (int i = 0; i < fields.length; i++) {
			ICardField field = fields[i];
			Object value = card.getObjectByField(field);
			if (value != null) {
				String string = value.toString();
				if (value instanceof Number) {
					if ((Float.valueOf(string) != 0))
						this.setObjectByField(field, string);
				} else if (string.length() > 0)
					this.setObjectByField(field, string);
			}
		}
	}

	public MagicCard getBase() {
		return this;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean matches(ICardField left, TextValue right) {
		String value = String.valueOf(getObjectByField(left));
		if (left == MagicCardField.TYPE && !right.regex) {
			return CardTypes.getInstance().hasType(this, right.getText());
		}
		return right.toPattern().matcher(value).find();
	}

	public void setCollNumber(int cnum) {
		if (cnum != 0)
			this.num = String.valueOf(cnum);
		else
			this.num = null;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(String list) {
		if (list == null || list.length() == 0)
			properties = null;
		else {
			if (!list.startsWith("{"))
				throw new IllegalArgumentException();
			list = list.substring(1, list.length() - 1);
			String[] split = list.split(",");
			for (int i = 0; i < split.length; i++) {
				String pair = split[i];
				String[] split2 = pair.split("=");
				setProperty(split2[0], split2[1]);
			}
		}
	}

	public void setProperty(String key, String value) {
		if (key == null)
			throw new NullPointerException();
		if (properties == null)
			properties = new LinkedHashMap<String, String>(3);
		properties.put(key, value);
	}

	public String getProperty(String key) {
		if (properties == null)
			return null;
		if (key == null)
			throw new NullPointerException();
		return properties.get(key);
	}

	public int getFlipId() {
		String fid = getProperty(MagicCardField.FLIPID.name());
		if (fid == null || fid.length() == 0)
			return 0;
		return Integer.valueOf(fid);
	}
}
