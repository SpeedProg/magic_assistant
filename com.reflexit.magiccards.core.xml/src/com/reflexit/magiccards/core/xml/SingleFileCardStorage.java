package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryCardStorage;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SingleFileCardStorage extends MemoryCardStorage<IMagicCard> implements ILocatable, IStorageInfo {
	private transient static final String VIRTUAL = "virtual";
	private transient File file;
	private Location location;
	private String name;
	private String comment;
	private String type;
	private Properties properties = new Properties();

	SingleFileCardStorage(File file, Location location) {
		this(file, location, false);
	}

	SingleFileCardStorage(File file, Location location, boolean initialize) {
		this.file = file;
		this.location = location;
		if (location != null)
			this.name = location.getName();
		//System.err.println("Create sin store " + location + " 0x" + Integer.toHexString(System.identityHashCode(this)));
		if (initialize) {
			load();
		}
	}

	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		return location + " 0x" + Integer.toHexString(System.identityHashCode(this));
	}

	@Override
	protected synchronized void doLoad() {
		CardCollectionStoreObject obj = null;
		try {
			obj = CardCollectionStoreObject.initFromFile(this.file);
			loadFields(obj);
			updateLocations();
			updateDbRef();
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	protected void updateDbRef() {
		VirtualMultiFileCardStore db = null;
		for (Object object : this) {
			if (object instanceof MagicCardPhisical) {
				MagicCardPhisical mp = (MagicCardPhisical) object;
				if (mp.getCard().getType() == null) {
					if (db == null) {
						db = waitForDb();
					}
					Collection<IMagicCard> cards = db.getCards(mp.getCardId());
					if (cards.size() > 0) {
						MagicCard c = (MagicCard) cards.iterator().next();
						mp.setMagicCard(c);
					}
				}
			}
		}
	}

	protected VirtualMultiFileCardStore waitForDb() {
		VirtualMultiFileCardStore db;
		db = (VirtualMultiFileCardStore) DataManager.getCardHandler().getDatabaseHandler().getCardStore();
		int count = 20;
		while (db.size() < 10000 && count-- > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
		return db;
	}

	protected void updateLocations() {
		if (getLocation() == null)
			return;
		for (Object object : this) {
			if (object instanceof MagicCardPhisical) {
				MagicCardPhisical mp = (MagicCardPhisical) object;
				mp.setLocation(getLocation());
			}
		}
	}

	/**
	 * @param obj
	 */
	protected void loadFields(CardCollectionStoreObject obj) {
		if (obj.list != null)
			this.doSetList(obj.list);
		else
			this.doSetList(new ArrayList<IMagicCard>());
		if (getLocation() == null)
			this.location = new Location(obj.key);
		if (obj.name != null)
			this.name = obj.name;
		this.comment = obj.comment;
		this.properties = obj.properties;
		this.type = obj.type;
	}

	/**
	 * @param obj
	 */
	protected void storeFields(CardCollectionStoreObject obj) {
		obj.list = new ArrayList(this.getList());
		obj.key = getLocation().toString();
		obj.name = getName();
		obj.comment = getComment();
		obj.type = getType();
		obj.properties = properties;
	}

	@Override
	protected synchronized void doSave() throws FileNotFoundException {
		CardCollectionStoreObject obj = new CardCollectionStoreObject();
		obj.file = this.file;
		storeFields(obj);
		obj.save();
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(Location location) {
		doSetLocation(location);
		updateLocations();
		autoSave();
	}

	protected final void doSetLocation(Location location2) {
		this.location = location2;
	}

	@Override
	public boolean removeAll() {
		if (size() == 0)
			return false;
		clearCache();
		autoSave();
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isVirtual() {
		String vir = getProperty(VIRTUAL);
		if (vir == null)
			return true;
		return Boolean.valueOf(vir);
	}

	public void setName(String name) {
		doSetName(name);
		autoSave();
	}

	protected final void doSetName(String name) {
		this.name = name;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		doSetComment(comment);
		autoSave();
	}

	protected final void doSetComment(String comment) {
		this.comment = comment;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		doSetType(type);
		autoSave();
	}

	protected final void doSetType(String type) {
		this.type = type.intern();
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public void setVirtual(boolean value) {
		setProperty(VIRTUAL, String.valueOf(value));
	}
}
