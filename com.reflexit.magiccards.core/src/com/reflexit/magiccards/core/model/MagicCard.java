package com.reflexit.magiccards.core.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.MagicCardFilter.TextValue;
import com.reflexit.magiccards.core.sync.GatherHelper;

public class MagicCard implements IMagicCard, ICardModifiable, IMagicCardPhysical {
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
	private float dbprice;
	private float rating;
	private String lang;
	private String num;
	private String rulings;
	private String text;
	private transient String colorType = "land";
	private transient int cmc = 0;
	private int enId;
	private LinkedHashMap<String, String> properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getCost()
	 */
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

	private final static Pattern mpartnamePattern = Pattern.compile("(.*)//(.*)\\s*\\((.*)\\)");

	public void setName(String name) {
		this.name = name;
		Matcher matcher = mpartnamePattern.matcher(name);
		if (matcher.matches()) {
			String p1 = matcher.group(1).trim();
			String p2 = matcher.group(2).trim();
			String pCur = matcher.group(3);
			setProperty(MagicCardField.PART, pCur);
			String other = pCur.equals(p1) ? p2 : p1;
			setProperty(MagicCardField.OTHER_PART, other);
		}
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
		else {
			try {
				t = Float.parseFloat(str);
			} catch (NumberFormatException e) {
				// if (str.contains("*"))
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
			// part is the other distinguisher of a card, used in split cards and flip cards
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
		if (field instanceof MagicCardField) {
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
					return getLanguage();
				case COLLNUM:
					return (this.num);
				case TEXT:
					return getText();
				case ENID:
					return (this.enId);
				case PROPERTIES:
					return (this.properties);
				case FLIPID:
					return getFlipId();
				case OTHER_PART:
					return getProperty(MagicCardField.OTHER_PART);
				case PART:
					return getPart();
				case SIDE:
					return getSide();
				case SET_CORE:
					if (edition == null)
						return null;
					if (edition.equals("*"))
						return "*";
					return Editions.getInstance().getEditionByName(edition).getType();
				case SET_BLOCK:
					if (edition == null)
						return null;
					if (edition.equals("*"))
						return "*";
					return Editions.getInstance().getEditionByName(edition).getBlock();
				case UNIQUE_COUNT:
					return getUniqueCount();
				case IMAGE_URL:
					return getImageUrl();
				case LEGALITY:
					return getProperty(MagicCardField.LEGALITY);
				default:
					break;
			}
		} else if (field instanceof MagicCardFieldPhysical && getRealCards() != null) {
			return getRealCards().getObjectByField(field);
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
		if (lang == null || lang.equals("English") || lang.length() == 0)
			this.lang = null;
		else
			this.lang = lang.intern();
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
				throw new IllegalArgumentException("Not settable");
			case CMC:
				throw new IllegalArgumentException("Not settable");
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
				setProperty(MagicCardField.FLIPID, value);
				break;
			case PART:
				setProperty(MagicCardField.PART, value);
				break;
			case OTHER_PART:
				setProperty(MagicCardField.OTHER_PART, value);
				break;
			case SIDE:
				setProperty(MagicCardField.SIDE, value);
				break;
			case IMAGE_URL:
				String x = getImageUrl();
				if (x != null && x.equals(value))
					break;
				setProperty(MagicCardField.IMAGE_URL, value);
				break;
			case LEGALITY:
				setProperty(MagicCardField.LEGALITY, value);
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public Object clone() {
		try {
			MagicCard obj = (MagicCard) super.clone();
			if (this.properties != null)
				obj.properties = (LinkedHashMap<String, String>) this.properties.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public MagicCard cloneCard() {
		return (MagicCard) clone();
	}

	public void copyFrom(IMagicCard card) {
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
		if (text == null)
			text = oracleText;
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
		return right.getPattern().matcher(value).find();
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
				if (split2.length < 2)
					continue;
				setProperty(split2[0], split2[1]);
			}
		}
	}

	public void setProperty(ICardField field, String value) {
		setProperty(field.name(), value);
	}

	public void setProperty(String key, String value) {
		if (key == null)
			throw new NullPointerException();
		key = key.trim();
		if (properties == null)
			properties = new LinkedHashMap<String, String>(3);
		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
	}

	public String getProperty(ICardField field) {
		return getProperty(field.name());
	}

	public String getProperty(String key) {
		if (properties == null)
			return null;
		if (key == null)
			throw new NullPointerException();
		return properties.get(key);
	}

	public int getFlipId() {
		String fid = getProperty(MagicCardField.FLIPID);
		if (fid == null || fid.length() == 0)
			return 0;
		return Integer.valueOf(fid);
	}

	public String getPart() {
		String part = getProperty(MagicCardField.PART);
		return part;
	}

	public int getSide() {
		String prop = getProperty(MagicCardField.SIDE);
		if (prop == null) {
			String part = getProperty(MagicCardField.PART);
			if (part == null) {
				return 0;
			} else if (part.startsWith("@")) {
				return 1;
			} else if (name.startsWith(part)) {
				return 0;
			} else {
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

	public int getOwnCount() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return 0;
		return realCards.getOwnCount();
	}

	public int getOwnTotalAll() {
		List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(getName());
		int sum = 0;
		for (IMagicCard card : cards) {
			if (card instanceof MagicCard)
				sum += ((MagicCard) card).getOwnCount();
		}
		return sum;
	}

	public int getOwnUnique() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return 0;
		return realCards.getOwnCount();
	}

	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public int getCount() {
		return 1;
		// if (realcards == null)
		// return 1;
		// return realcards.getCount();
	}

	public String getComment() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return null;
		return (String) realCards.getObjectByField(MagicCardFieldPhysical.COMMENT);
	}

	public Location getLocation() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return Location.NO_WHERE;
		return (Location) realCards.getObjectByField(MagicCardFieldPhysical.LOCATION);
	}

	public boolean isOwn() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return false;
		return (Boolean) realCards.getObjectByField(MagicCardFieldPhysical.OWNERSHIP);
	}

	public int getForTrade() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return 0;
		return (Integer) realCards.getObjectByField(MagicCardFieldPhysical.FORTRADECOUNT);
	}

	public float getPrice() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return 0;
		return realCards.getPrice();
	}

	public String getSpecial() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return null;
		return (String) realCards.getObjectByField(MagicCardFieldPhysical.SPECIAL);
	}

	public boolean isSideboard() {
		CardGroup realCards = getRealCards();
		if (realCards == null)
			return false;
		return (Boolean) realCards.getObjectByField(MagicCardFieldPhysical.SIDEBOARD);
	}

	public int getUniqueCount() {
		return 1;
	}

	public boolean isPhysical() {
		return false;
	}

	@Override
	public int getGathererId() {
		if (id > 0)
			return id;
		if (id < 0 && (id & (1 << 30)) != 0)
			return -id;
		return 0;
	}

	@Override
	public int getCollectorNumberId() {
		if (num == null)
			return 0;
		try {
			return Integer.parseInt(num);
		} catch (NumberFormatException e) {
			try {
				return Integer.parseInt(num.substring(0, num.length() - 1));
			} catch (Exception e1) {
				return 0;
			}
		}
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
		return DataManager.getRealCards(this);
	}

	public String getImageUrl() {
		String x = getProperty(MagicCardField.IMAGE_URL);
		if (x != null)
			return x;
		int gathererId = getGathererId();
		if (gathererId != 0) {
			URL url;
			try {
				url = GatherHelper.createImageURL(gathererId, null);
			} catch (MalformedURLException e) {
				return null;
			}
			return url.toExternalForm();
		}
		return null;
	}

	public LegalityMap getLegalityMap() {
		String value = getProperty(MagicCardField.LEGALITY);
		if (value == null)
			return null;
		LegalityMap map = LegalityMap.valueOf(value);
		return map;
	}
}
