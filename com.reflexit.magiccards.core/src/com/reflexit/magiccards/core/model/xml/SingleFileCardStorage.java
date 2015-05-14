package com.reflexit.magiccards.core.model.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryCardStorage;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.magiccards.core.xml.StringCache;

public class SingleFileCardStorage extends MemoryCardStorage<IMagicCard> implements IStorageInfo {
	private static final transient String VIRTUAL = "virtual";
	private static final transient String READ_ONLY = "readonly";
	protected transient File file;
	protected Location location;
	protected String name;
	protected String comment;
	protected String type;
	protected Properties properties = new Properties();

	public SingleFileCardStorage() {
		super();
	}

	public SingleFileCardStorage(File file, Location location) {
		this(file, location, false);
	}

	public SingleFileCardStorage(File file, Location location, boolean initialize) {
		this.file = file;
		this.location = location;
		if (location != null) {
			this.name = location.getName();
		}
		// System.err.println("Create sin store " + location + " 0x" +
		// Integer.toHexString(System.identityHashCode(this)));
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

	protected void updateLocations() {
		if (getLocation() == null)
			return;
		for (Object object : this) {
			if (object instanceof MagicCardPhysical) {
				MagicCardPhysical mp = (MagicCardPhysical) object;
				mp.setLocation(getLocation());
			}
		}
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(Location location) {
		if (this.location != null && !this.location.equals(location)) {
			doSetLocation(location);
			updateLocations();
			this.file = LocationResolver.getInstance().getFile(location);
			this.name = location.getName();
			autoSave();
		}
	}

	protected final void doSetLocation(Location location) {
		this.location = location;
	}

	@Override
	public boolean removeAll() {
		accessCheck();
		if (size() == 0)
			return false;
		clearCache();
		autoSave();
		return true;
	}

	@Override
	public boolean add(IMagicCard card) {
		accessCheck();
		return super.add(card);
	}

	@Override
	public boolean remove(IMagicCard card) {
		accessCheck();
		return super.remove(card);
	}

	protected void accessCheck() {
		if (isReadOnly())
			throw new MagicException("Read Only");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isVirtual() {
		return Boolean.valueOf(getProperty(VIRTUAL));
	}

	public void setName(String name) {
		throw new UnsupportedOperationException();
		//		doSetName(name);
		//		autoSave();
	}

	protected final void doSetName(String name) {
		this.name = name;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		accessCheck();
		doSetComment(comment);
		autoSave();
	}

	protected final void doSetComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		accessCheck();
		if (doSetType(type))
			autoSave();
	}

	protected final boolean doSetType(String type) {
		String x = StringCache.intern(type);
		if (this.type == x) return false;
		this.type = x;
		return true;
	}

	@Override
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
		if (isReadOnly() && !key.equals(READ_ONLY))
			throw new MagicException("Read Only");
		Object old = properties.setProperty(key, value);
		if (old != null && old.equals(value)) return;
		autoSave();
	}

	@Override
	public void setVirtual(boolean value) {
		accessCheck();
		setProperty(VIRTUAL, String.valueOf(value));
	}

	@Override
	public void setReadOnly(boolean value) {
		setProperty(READ_ONLY, String.valueOf(value));
	}

	@Override
	protected synchronized void doSave() throws IOException {
		CardCollectionStoreObject obj = new CardCollectionStoreObject();
		obj.file = this.file;
		storeFields(obj);
		obj.save();
	}

	@Override
	protected synchronized void doLoad() {
		CardCollectionStoreObject obj = null;
		try {
			// System.err.println("Loading " + file);
			obj = CardCollectionStoreObject.initFromFile(this.file);
			loadFields(obj);
			updateLocations();
			// DataManager.reconcileAdd(this);
		} catch (MagicException e) {
			throw e;
		} catch (Exception e) {
			throw new MagicException(e);
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
			this.location = Location.valueOf(obj.key);
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
		obj.list = new ArrayList<IMagicCard>(this.getList());
		obj.key = getLocation().toString();
		obj.name = getName();
		obj.comment = getComment();
		obj.type = getType();
		obj.properties = properties;
	}

	@Override
	public boolean isReadOnly() {
		return Boolean.valueOf(getProperty(READ_ONLY));
	}
}