package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryCardStorage;

public class SingleFileCardStorage extends MemoryCardStorage<IMagicCard> implements IStorageInfo {
	private static final transient String VIRTUAL = "virtual";
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
		if (location != null)
			this.name = location.getName();
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
			autoSave();
		}
	}

	protected final void doSetLocation(Location location) {
		this.location = location;
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
			return false;
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
		} catch (IOException e) {
			MagicLogger.log(e);
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
}