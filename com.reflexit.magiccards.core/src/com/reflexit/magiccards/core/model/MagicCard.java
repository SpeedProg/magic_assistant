package com.reflexit.magiccards.core.model;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Languages.Language;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.sync.GatherHelper;

public class MagicCard extends AbstractMagicCard {
	private int id;
	private int enId;
	private String name;
	private String cost;
	private String type;
	private String power;
	private String toughness;
	private String edition;
	private String rarity;
	private String oracleText;
	private String artist;
	private String lang;
	private String num;
	private String rulings;
	private String text;
	private float rating;
	private transient String colorType = "land";
	private transient int cmc = 0;
	private LinkedHashMap<ICardField, Object> properties;

	public MagicCard() {
		// do nothing
	}

	@Override
	public String getCost() {
		if (cost == null)
			return "";
		return cost;
	}

	public synchronized void setCost(String cost) {
		if (cost == null)
			throw new NullPointerException();
		this.cost = cost;
		colorType = null;
		cmc = Colors.getInstance().getConvertedManaCost(this.cost);
	}

	@Override
	public int getCardId() {
		return this.id;
	}

	public void setCardId(int id) {
		this.id = id;
	}

	public void setId(String id) {
		this.id = Integer.parseInt(id);
	}

	@Override
	public int getEnglishCardId() {
		return this.enId;
	}

	public void setEnglishCardId(int id) {
		this.enId = id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getOracleText() {
		return this.oracleText;
	}

	public void setOracleText(String oracleText) {
		this.oracleText = oracleText;
	}

	@Override
	public String getRarity() {
		return this.rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	@Override
	public String getSet() {
		return this.edition;
	}

	public void setSet(String setName) {
		this.edition = setName;
	}

	@Override
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getPower() {
		return this.power;
	}

	public void setPower(String power) {
		this.power = power;
	}

	@Override
	public String getToughness() {
		return this.toughness;
	}

	public void setToughness(String toughness) {
		this.toughness = toughness;
	}

	@Override
	public synchronized String getColorType() {
		if (colorType == null) {
			if (cost == null || cost.isEmpty()) {
				if (!isLand()) {
					return "colorless";
				}
			}
			colorType = Colors.getInstance().getColorType(cost);
		}
		return this.colorType;
	}

	@Override
	public int getCmc() {
		return this.cmc;
	}

	@Override
	public int hashCode() {
		if (this.id != 0)
			return this.id;
		return this.name != null ? this.name.hashCode() : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MagicCard))
			return false;
		if (obj == this)
			return true;
		MagicCard ma = (MagicCard) obj;
		if (this.id != 0) {
			if (this.id != ma.id)
				return false;
			if (this.properties == null && ma.properties == null) {
				return true;
			}
			// part is the other distinguisher of a card, used in split cards
			// and flip cards
			String part = this.getPart();
			String part2 = ma.getPart();
			if (part != null)
				return part.equals(part2);
			return part2 == null;
		} else {
			if (this.name != null) {
				if (!this.name.equals(ma.name))
					return false;
			} else if (ma.name != null)
				return false;
			if (this.edition != null)
				return this.edition.equals(ma.edition);
			return ma.edition == null;
		}
	}

	@Override
	public String toString() {
		return this.id + ": " + this.name + " [" + this.edition + "]";
	}

	@Override
	public Object get(ICardField field) {
		return ((MagicCardField) field).get(this);
	}

	@Override
	public float getDbPrice() {
		return DataManager.getDBPriceStore().getDbPrice(this);
	}

	public void setDbPrice(float dbprice) {
		DataManager.getDBPriceStore().setDbPrice(this, dbprice);
	}

	@Override
	public float getCommunityRating() {
		return rating;
	}

	@Override
	public String getArtist() {
		return this.artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	@Override
	public String getRulings() {
		return this.rulings;
	}

	@Override
	public String getLanguage() {
		return lang;
	}

	public void setLanguage(String lang) {
		if (lang == null || lang.equals("English") || lang.length() == 0)
			this.lang = null;
		else {
			Language l = Languages.Language.fromName(lang);
			if (l != null) {
				this.lang = l.getLang();
			} else {
				l = Languages.Language.fromLocale(lang);
				if (l != null)
					this.lang = l.getLang();
				else
					throw new MagicException("Unknown language: " + lang);
			}
		}
	}

	public String getCollNumber() {
		if (num == null)
			return "";
		return num;
	}

	public void setCollNumber(String collNumber) {
		this.num = collNumber;
	}

	@Override
	public boolean set(ICardField field, Object value) {
		MagicCardField mf = (MagicCardField) field;
		mf.setM(this, value);
		return true;
	}

	void setPropertyInteger(MagicCardField field, Object value) {
		if (value instanceof Integer) {
			Integer v = (Integer) value;
			if (v.intValue() == 0) {
				setProperty(field, null);
			} else {
				setProperty(field, v);
			}
		} else if (value != null) {
			setProperty(field, Integer.parseInt(value.toString()));
		} else {
			setProperty(field, null);
		}
	}

	void setPropertyString(MagicCardField field, Object value) {
		if (value != null) {
			String str = value.toString();
			if (!str.isEmpty()) {
				setProperty(field, str);
				return;
			}
		}
		setProperty(field, null);
	}

	@Override
	public Object clone() {
		try {
			MagicCard obj = (MagicCard) super.clone();
			if (this.properties != null)
				obj.properties = (LinkedHashMap) this.properties.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public MagicCard cloneCard() {
		return (MagicCard) clone();
	}

	/**
	 * Copy all fields which have default values in this card from given card
	 *
	 * @param card
	 */
	public void setEmptyFromCard(IMagicCard card) {
		for (ICardField field : MagicCardField.allNonTransientFields(false)) {
			Object value = get(field);
			if (isEmptyValue(value))
				setNonEmptyFromCard(field, card);
		}
	}

	public void setNonEmptyFromCard(MagicCard card) {
		setNonEmptyFromCard((Set<ICardField>) null, card);
	}

	public void setNonEmptyFromCard(Set<ICardField> fieldSet, MagicCard card) {
		if (fieldSet == null || fieldSet.isEmpty()) {
			setNonEmptyFromCard(MagicCardField.allNonTransientFields(false), card);
		} else
			for (ICardField field : fieldSet) {
				if (field instanceof MagicCardField && !((MagicCardField) field).isPhysical()) {
					setNonEmptyFromCard(field, card);
				}
			}
	}

	public void setNonEmptyFromCard(ICardField[] fieldSet, MagicCard card) {
		for (ICardField field : fieldSet) {
			if (field instanceof MagicCardField && !((MagicCardField) field).isPhysical()) {
				setNonEmptyFromCard(field, card);
			}
		}
	}

	public boolean setNonEmptyFromCard(ICardField field, IMagicCard card) {
		return setNonEmpty(field, card.get(field));
	}

	public boolean setNonEmpty(ICardField field, Object value) {
		if (!isEmptyValue(value))
			return set(field, value);
		return false;
	}

	public boolean setIfEmpty(ICardField field, Object value) {
		if (isEmptyValue(get(field)))
			return set(field, value);
		return false;
	}

	public boolean isEmptyValue(Object value) {
		if (value == null)
			return true;
		if (value instanceof Number) {
			if (((Number) value).intValue() == 0)
				return true;
		} else if (value instanceof String) {
			if (((String) value).length() == 0)
				return true;
		} else if (value instanceof LegalityMap) {
			if (((LegalityMap) value).isEmpty())
				return true;
		} else {
			String string = value.toString();
			if (string.length() == 0)
				return true;
		}
		return false;
	}

	@Override
	public MagicCard getBase() {
		return this;
	}

	@Override
	public String getText() {
		if (text == null)
			text = oracleText;
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setCollNumber(int cnum) {
		if (cnum != 0)
			this.num = String.valueOf(cnum);
		else
			this.num = null;
	}

	void setProperties(LinkedHashMap<ICardField, Object> properties) {
		this.properties = properties;
	}

	public Map<ICardField, Object> getProperties() {
		return properties;
	}

	public void setProperties(String list1) {
		String list = list1;
		if (list == null || list.length() == 0)
			properties = null;
		else {
			if (!list.startsWith("{"))
				throw new IllegalArgumentException();
			list = list.substring(1, list.length() - 1);
			String[] split = list.split(",");
			for (int i = 0; i < split.length; i++) {
				String pair = split[i];
				String[] split2 = pair.trim().split("=");
				if (split2.length == 1)
					setProperty(split2[0], "true");
				else if (split2.length == 2)
					setProperty(split2[0], split2[1]);
			}
		}
	}

	public void setProperty(ICardField field, Object value) {
		if (value != null && !isEmptyValue(value)) {
			if (properties == null)
				properties = new LinkedHashMap<ICardField, Object>(3);
			properties.put(field, value);
		} else if (properties != null) {
			properties.remove(field);
			if (properties.size() == 0)
				properties = null;
		}
	}

	private void setProperty(String key, Object value) {
		if (key == null)
			throw new NullPointerException();
		String keyTrim = key.trim();
		if (keyTrim.isEmpty())
			throw new IllegalArgumentException();
		ICardField field = MagicCardField.fieldByName(keyTrim);
		if (field == null) {
			MagicLogger.log("Unknown property " + keyTrim);
			return;
		}
		set(field, value);
	}

	public Object getProperty(ICardField field) {
		if (properties == null)
			return null;
		if (field == null)
			throw new NullPointerException();
		return properties.get(field);
	}

	@Override
	public int getFlipId() {
		Object flipId = getProperty(MagicCardField.FLIPID);
		if (flipId == null)
			return 0;
		return (Integer) flipId;
	}

	public String getPart() {
		String part = (String) getProperty(MagicCardField.PART);
		return part;
	}

	@Override
	public int getSide() {
		Integer prop = (Integer) getProperty(MagicCardField.SIDE);
		if (prop == null) {
			String colNum = getCollNumber();
			if (colNum.endsWith("a"))
				return 0;
			else if (colNum.endsWith("b"))
				return 1;
			String part = (String) getProperty(MagicCardField.PART);
			if (part == null) {
				return 0;
			} else if (part.startsWith("@")) {
				return 1;
			}
		} else {
			return prop;
		}
		return 0;
	}

	public Collection<MagicCardPhysical> getPhysicalCards() {
		CardGroup rc = getRealCards();
		if (rc == null)
			return Collections.emptySet();
		return (Collection<MagicCardPhysical>) rc.getChildrenList();
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCount() {
		return 1; // block of the set
	}

	public int getCount4() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return 0;
		int c = realCards.getOwnCount();
		if (c > 4)
			return 4;
		return c;
	}

	public int getCreatureCount() {
		if (getPower() == null)
			return 0;
		if (!getPower().isEmpty()) {
			return 1;
		}
		return 0;
	}

	@Override
	public Location getLocation() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return Location.NO_WHERE;
		return (Location) realCards.get(MagicCardField.LOCATION);
	} // block of the set

	@Override
	public boolean isSideboard() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return false;
		return realCards.getBoolean(MagicCardField.SIDEBOARD);
	}

	@Override
	public int getUniqueCount() {
		return 1;
	}

	@Override
	public boolean isPhysical() {
		return false;
	}

	/**
	 * create syntetic id Local db bitset [t2][s15][l4][v1][i10]
	 *
	 * @return
	 */
	public int syntesizeId() {
		Edition ed = getEdition();
		if (ed.isUnknown()) {
			throw new IllegalStateException("Set is not registered for the card");
		}
		int sid = 1 << 31 | (ed.getId() & 0x7f) << 15 | getSide() << 10 | getCollectorNumberId();
		return sid;
	}

	public CardGroup getRealCards() {
		return DataManager.getInstance().getRealCards(this);
	}

	public String getImageUrl() {
		String x = (String) getProperty(MagicCardField.IMAGE_URL);
		if (x != null)
			return x;
		return getDefaultImageUrl();
	}

	public String getDefaultImageUrl() {
		int gathererId = getGathererId();
		if (gathererId != 0) {
			URL url = GatherHelper.createImageURL(gathererId);
			return url.toExternalForm();
		}
		return null;
	}

	@Override
	public LegalityMap getLegalityMap() {
		Object value = getProperty(MagicCardField.LEGALITY);
		if (value != null) {
			try {
				return LegalityMap.valueOf(value);
			} catch (IllegalArgumentException e) {
				MagicLogger.log("Invalid legality value " + value);
			}
		}
		LegalityMap map = induceLegality();
		setLegalityMap(map);
		return map;
	}

	private LegalityMap induceLegality() {
		if (isBasicLand())
			return LegalityMap.createFromLegal(Format.STANDARD.name());
		Edition edition = getEdition();
		if (edition.isUnknown())
			return LegalityMap.EMPTY;
		LegalityMap legalityMap = edition.getLegalityMap();
		if (legalityMap == null)
			return LegalityMap.EMPTY;
		if (legalityMap.isLegal(Format.STANDARD))
			return legalityMap;
		// check printings
		IMagicCard prime = db().getPrime(name);
		if (prime != null && prime != this) {
			LegalityMap candMap = prime.getLegalityMap();
			return legalityMap.merge(candMap);
		}
		return legalityMap;
	}

	public void setLegalityMap(LegalityMap map) {
		setProperty(MagicCardField.LEGALITY, map);
	}

	/**
	 * @return normalized id of the card, i.e. english card id of the same card
	 *         in set
	 */
	public int getNormId() {
		int x = getEnglishCardId();
		if (x == 0)
			return getCardId();
		return x;
	}

	/**
	 * @return id of the prime card of the english version of this card
	 */
	public int getPrimeId() {
		int x = getNormId();
		IMagicCard norm = db().getCard(x);
		if (norm == null)
			norm = this;
		IMagicCard prime = db().getPrime(norm.getName());
		if (prime == null)
			prime = norm;
		return prime.getCardId();
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

	public void setRating(float rating) {
		this.rating = rating;
	}

	void setRulings(String rulings) {
		this.rulings = rulings;
	}

	void setImageUrl(String value) {
		if (value == null || value.isEmpty())
			return;
		String x = getDefaultImageUrl();
		if (value.equals(x))
			return;
		setPropertyString(MagicCardField.IMAGE_URL, value);
	}

	void setFlipId(int value) {
		setPropertyInteger(MagicCardField.FLIPID, value);
	}

	public float getRating() {
		return rating;
	}

	public String getEnglishType() {
		int x = getEnglishCardId();
		if (x == 0)
			return getType();
		IMagicCard norm = db().getCard(x);
		if (norm == null)
			norm = this;
		return norm.getType();
	}
}
