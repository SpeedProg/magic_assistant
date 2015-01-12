package com.reflexit.magiccards.core.model.xml;

import java.io.File;
import java.util.HashMap;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;

public class LocationResolver {
	private static LocationResolver instance;
	private HashMap<Location, File> locmap;

	private LocationResolver() {
		// private constructor
		locmap = new HashMap<Location, File>();
	}

	public static LocationResolver getInstance() {
		if (instance == null) {
			instance = new LocationResolver();
		}
		return instance;
	}

	public File getFile(Location location) {
		if (locmap.containsKey(location))
			return locmap.get(location);
		return new File(DataManager.getInstance().getRootDir(), location.getPath() + ".xml");
	}

	public void setFile(Location location, File file) {
		locmap.put(location, file);
	}
}
