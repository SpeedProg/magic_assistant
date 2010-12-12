package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.reflexit.magiccards.core.Activator;
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
		IPath path;
		try {
			path = DataManager.getProject().getLocation().append(location.getPath() + ".xml");
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}
		return path.toFile();
	}

	public void setFile(Location location, File file) {
		locmap.put(location, file);
	}
}
