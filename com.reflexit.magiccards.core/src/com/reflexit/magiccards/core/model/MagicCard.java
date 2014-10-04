package com.reflexit.magiccards.core.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.Languages.Language;
import com.reflexit.magiccards.core.sync.GatherHelper;

public class MagicCard extends AbstractMagicCard implements IMagicCard, ICardModifiable, IMagicCardPhysical {
	private int id;
	private String name;
	private String cost;
	private String type;
	private String power;
	private String toughness;
	private String edition;
	private String rarity;
	private String oracleText;
	private String artist;
	private float rating;
	private String lang;
	private String num;
	private String rulings;
	private String text;
	private transient String colorType = "land";
	private transient int cmc = 0;
	private int enId;
	private LinkedHashMap<String, Object> properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCost()
	 */
	@Override
	public String getCost() {
		if (cost == null)
			return "";
		return this.cost;
	}

	public void setCost(String cost) {
		this.cost = cost.intern();
		Colors cl = Colors.getInstance();
		colorType = cl.getColorType(this.cost);
		cmc = cl.getConvertedManaCost(this.cost);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCardId()
	 */
	@Override
	public int getCardId() {
		return this.id;
	}

	public void setCardId(int id) {
		this.id = id;
	}

	@Override
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
	@Override
	public String getName() {
		return this.name;
	}

	private final static Pattern mpartnamePattern = Pattern.compile("(.*)//(.*)\\s*\\((.*)\\)");

	public void setName(String name) {
		this.name = name;
		// Matcher matcher = mpartnamePattern.matcher(name);
		// if (matcher.matches()) {
		// String p1 = matcher.group(1).trim();
		// String p2 = matcher.group(2).trim();
		// String pCur = matcher.group(3);
		// setProperty(MagicCardField.PART, pCur);
		// String other = pCur.equals(p1) ? p2 : p1;
		// setProperty(MagicCardField.OTHER_PART, other);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getOracleText()
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public String getPower() {
		return this.power;
	}

	public void setPower(String power) {
		this.power = power == null ? "" : power.intern();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getToughness()
	 */
	@Override
	public String getToughness() {
		return this.toughness;
	}

	public void setToughness(String toughness) {
		this.toughness = toughness == null ? "" : toughness.intern();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getColorType()
	 */
	@Override
	public String getColorType() {
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
	@Override
	public int getCmc() {
		return this.cmc;
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
			return part == part2;
		} else {
			if (this.name != null)
				if (!this.name.equals(ma.name))
					return false;
			if (this.edition != null)
				return this.edition.equals(ma.edition);
			else
				return this.edition == ma.edition;
		}
	}

	@Override
	public String toString() {
		return this.id + ": " + this.name + " [" + this.edition + "]";
	}

	public Collection getHeaderNames() {
		ICardField[] values = MagicCardField.allNonTransientFields(false);
		ArrayList list = new ArrayList();
		for (ICardField magicCardField : values) {
			list.add(magicCardField.toString());
		}
		return list;
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		ICardField[] xfields = MagicCardField.allNonTransientFields(false);
		for (ICardField field : xfields) {
			list.add(get(field));
		}
		return list;
	}

	@Override
	public Object get(ICardField field) {
		MagicCardField mf = (MagicCardField) field;
		switch (mf) {
			case ID:
				return getCardId();
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
				return getCmc();
			case DBPRICE:
				return getDbPrice();
			case RATING:
				return (this.rating);
			case ARTIST:
				return (this.artist);
			case RULINGS:
				return (this.rulings);
			case LANG:
				return getLanguage();
			case COLLNUM:
				return (this.num);
			case TEXT:
				return getText();
			case ENID:
				return (this.enId);
			case PROPERTIES:
				return (this.properties);
			case FLIPID: {
				Object flipId = getProperty(MagicCardField.FLIPID);
				if (flipId == null)
					return 0;
				return flipId;
			}
			case OTHER_PART:
				return getProperty(MagicCardField.OTHER_PART);
			case PART:
				return getProperty(MagicCardField.PART);
			case SIDE:
				return getSide();
			case SET_CORE: {
				Edition ed = Editions.getInstance().getEditionByNameAlways(edition);
				return ed.getType();
			}
			case SET_BLOCK: {
				Edition ed = Editions.getInstance().getEditionByNameAlways(edition);
				return ed.getBlock();
			}
			case SET_RELEASE: {
				Edition ed = Editions.getInstance().getEditionByNameAlways(edition);
				return ed.getReleaseDate();
			}
			case EDITION_ABBR: {
				Edition ed = Editions.getInstance().getEditionByNameAlways(edition);
				return ed.getMainAbbreviation();
			}
			case UNIQUE_COUNT:
				return getUniqueCount();
			case IMAGE_URL:
				return getImageUrl();
			case LEGALITY:
				return getLegalityMap();
			case COLOR:
				return getCost();
			case COUNT:
				return getCount();
			case SIDEBOARD:
				return isSideboard();
			case LOCATION:
				return getLocation();
			case COUNT4:
				return getCount4();
			case HASHCODE:
				return System.identityHashCode(this);
			default:
				if (getRealCards() != null) {
					return getRealCards().get(field);
				} else {
					return null;
				}
		}
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

	public void setCommunityRating(float rating) {
		this.rating = rating;
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

	public void setRulings(String rulings) {
		this.rulings = rulings;
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
		if (collNumber == null || collNumber.trim().length() == 0)
			this.num = null;
		else
			this.num = collNumber.intern();
	}

	@Override
	public boolean set(ICardField field, Object value) {
		MagicCardField mf = (MagicCardField) field;
		switch (mf) {
			case ID:
				if (value instanceof Integer)
					setCardId((Integer) value);
				else
					setCardId(Integer.parseInt((String) value));
				break;
			case NAME:
				setName((String) value);
				break;
			case COST:
				setCost((String) value);
				break;
			case TYPE:
				setType((String) value);
				break;
			case POWER:
				setPower(value.toString());
				break;
			case TOUGHNESS:
				setToughness(value.toString());
				break;
			case ORACLE:
				setOracleText((String) value);
				break;
			case SET:
				setSet((String) value);
				break;
			case RARITY:
				setRarity((String) value);
				break;
			case CTYPE:
				throw new IllegalArgumentException("Not settable " + mf);
			case CMC:
				throw new IllegalArgumentException("Not settable " + mf);
			case DBPRICE:
				if (value instanceof Float)
					setDbPrice((Float) value);
				else
					setDbPrice(Float.parseFloat((String) value));
				break;
			case RATING:
				if (value instanceof Float)
					setCommunityRating((Float) value);
				else
					setCommunityRating(Float.parseFloat((String) value));
				break;
			case ARTIST:
				setArtist((String) value);
				break;
			case RULINGS:
				setRulings((String) value);
				break;
			case LANG:
				setLanguage((String) value);
				break;
			case COLLNUM:
				setCollNumber((String) value);
				break;
			case TEXT:
				setText((String) value);
				break;
			case ENID:
				if (value instanceof Integer)
					setEnglishCardId((Integer) value);
				else
					setEnglishCardId(Integer.parseInt((String) value));
				break;
			case PROPERTIES:
				if (value instanceof LinkedHashMap)
					properties = (LinkedHashMap) ((LinkedHashMap) value).clone();
				else
					setProperties((String) value);
				break;
			case FLIPID:
				setPropertyInteger(MagicCardField.FLIPID, value);
				break;
			case PART:
				setPropertyString(MagicCardField.PART, value);
				break;
			case OTHER_PART:
				setPropertyString(MagicCardField.OTHER_PART, value);
				break;
			case SIDE:
				setProperty(MagicCardField.SIDE, value);
				break;
			case IMAGE_URL:
				String x = getImageUrl();
				if (x != null && x.equals(value))
					break;
				setPropertyString(MagicCardField.IMAGE_URL, value);
				break;
			case LEGALITY:
				setProperty(MagicCardField.LEGALITY, value);
				break;
			case COLOR:
				throw new IllegalArgumentException("Not settable");
			default:
				return false;
		}
		return true;
	}

	private void setPropertyInteger(MagicCardField field, Object value) {
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

	private void setPropertyString(MagicCardField field, Object value) {
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
				obj.properties = (LinkedHashMap<String, Object>) this.properties.clone();
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
			for (ICardField field : fieldSet)
				setNonEmptyFromCard(field, card);
	}

	public void setNonEmptyFromCard(ICardField[] fieldSet, MagicCard card) {
		for (ICardField field : fieldSet)
			setNonEmptyFromCard(field, card);
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

	public Map<String, Object> getProperties() {
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
			String[] split = list.split(" *, *");
			for (int i = 0; i < split.length; i++) {
				String pair = split[i];
				String[] split2 = pair.split("=");
				if (split2.length == 1)
					setProperty(split2[0], "");
				else if (split2.length == 2)
					setProperty(split2[0], split2[1]);
			}
		}
	}

	public void setProperty(ICardField field, Object value) {
		setProperty1(field.name(), value);
	}

	public void setProperty(String key, Object value) {
		if (key == null)
			throw new NullPointerException();
		if (key.trim().isEmpty())
			throw new IllegalArgumentException();
		setProperty1(key.trim(), value);
	}

	private void setProperty1(String key, Object value) {
		if (value != null && !isEmptyValue(value)) {
			if (properties == null)
				properties = new LinkedHashMap<String, Object>(3);
			properties.put(key, value);
		} else if (properties != null) {
			properties.remove(key);
			if (properties.size() == 0)
				properties = null;
		}
	}

	public Object getProperty(ICardField field) {
		return getProperty(field.name());
	}

	public Object getProperty(String key) {
		if (properties == null)
			return null;
		if (key == null)
			throw new NullPointerException();
		return properties.get(key);
	}

	@Override
	public int getFlipId() {
		return getInt(MagicCardField.FLIPID);
	}

	public String getPart() {
		String part = (String) getProperty(MagicCardField.PART);
		return part;
	}

	@Override
	public int getSide() {
		String prop = (String) getProperty(MagicCardField.SIDE);
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
			try {
				return Integer.parseInt(prop);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		return 1;
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

	@Override
	public Location getLocation() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return Location.NO_WHERE;
		return (Location) realCards.get(MagicCardField.LOCATION);
	}

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

	public int syntesizeId() {
		MagicCard card = this;
		Edition ed = Editions.getInstance().getEditionByName(card.getSet());
		if (ed == null) {
			throw new IllegalStateException("Set is not registered for the card");
		}
		int sid = 1 << 31 | (ed.getId() & 0x7f) << 15 | card.getSide() << 10 | card.getCollectorNumberId();
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
		String set = getSet();
		Edition edition = Editions.getInstance().getEditionByName(set);
		if (edition == null)
			return LegalityMap.EMPTY;
		LegalityMap legalityMap = edition.getLegalityMap();
		if (legalityMap == null)
			return LegalityMap.EMPTY;
		if (legalityMap.isLegal(Format.STANDARD))
			return legalityMap;
		// check printings
		IMagicCard magicCard = DataManager.getMagicDBStore().getPrime(name);
		if (magicCard != null && magicCard != this) {
			LegalityMap candMap = magicCard.getLegalityMap();
			return legalityMap.merge(candMap);
		}
		return LegalityMap.EMPTY;
	}

	public void setLegalityMap(LegalityMap map) {
		setProperty(MagicCardField.LEGALITY, map);
	}
}
