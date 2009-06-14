package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryCardStorage;
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SingleFileCardStorage extends MemoryCardStorage<IMagicCard> implements ILocatable, IStorageInfo {
	protected transient File file;
	protected String location;
	protected String name;
	protected String comment;
	protected String type;
	protected Properties properties = new Properties();

	SingleFileCardStorage(File file, String location) {
		this(file, location, false);
	}

	SingleFileCardStorage(File file, String location, boolean initialize) {
		this.file = file;
		this.location = location;
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
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	void updateLocations() {
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
			this.setList(obj.list);
		else
			this.setList(new ArrayList<IMagicCard>());
		if (obj.key != null)
			setLocation(obj.key);
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
		obj.list = (ArrayList) this.getList();
		obj.key = getLocation();
		obj.name = getName();
		obj.comment = getComment();
		obj.type = getType();
		obj.properties = properties;
	}

	@Override
	public void save() {
		try {
			doSave();
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	@Override
	protected synchronized void doSave() throws FileNotFoundException {
		CardCollectionStoreObject obj = new CardCollectionStoreObject();
		obj.file = this.file;
		storeFields(obj);
		obj.save();
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
		autoSave();
	}

	@Override
	public boolean removeAll() {
		if (size() == 0)
			return false;
		clearCache();
		setNeedToSave(true);
		autoSave();
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		autoSave();
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		autoSave();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		autoSave();
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}
}
