/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model;

import java.io.File;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.LocationPath;

/**
 * Location represents path to a decl/collection
 */
public class Location implements Comparable<Location> {
	private static final String XML_SUFFIX = ".xml";
	public static final String SIDEBOARD_SUFFIX = "-sideboard";
	public static final Location NO_WHERE = new Location();
	private static final String SEP = "/";
	private final String path;

	private Location() {
		this.path = "";
	}

	private Location(String loc) {
		if (loc == null || loc.isEmpty())
			throw new IllegalArgumentException(loc);
		this.path = loc.intern();
	}

	private Location(Location parent, String loc) {
		if (loc == null || loc.isEmpty())
			throw new IllegalArgumentException(loc);
		if (parent != Location.NO_WHERE)
			loc = parent.getPath() + SEP + loc;
		this.path = loc.intern();
	}

	private static String trimSep(String loc) {
		while (loc.endsWith(SEP)) {
			loc = loc.substring(0, loc.length() - 1);
		}
		while (loc.startsWith(SEP)) {
			loc = loc.substring(1);
		}
		if (loc.endsWith(XML_SUFFIX))
			loc = loc.substring(0, loc.length() - XML_SUFFIX.length());
		return loc;
	}

	@Override
	public String toString() {
		return path;
	}

	public static Location fromCard(IMagicCard card) {
		if (card instanceof MagicCardPhysical) {
			return ((MagicCardPhysical) card).getLocation();
		} else if (card instanceof MagicCard) {
			String set = card.getSet();
			return createLocationFromSet(set);
		}
		return Location.NO_WHERE;
	}

	public static Location createLocationFromSet(String set) {
		return new Location(set.replaceAll("[\\W]", "_"));
	}

	public static Location createLocation(String loc) {
		return valueOf(loc);
	}

	public static Location valueOf(String str) {
		if (str == null)
			return NO_WHERE;
		str = trimSep(str);
		if (str.isEmpty())
			return NO_WHERE;
		return new Location(str);
	}

	public String getName() {
		int index = path.lastIndexOf(SEP);
		if (index == -1) {
			return path;
		}
		return path.substring(index + 1, path.length());
	}

	public static Location createLocation(LocationPath path) {
		return Location.valueOf(path.toPortableString());
	}

	public static Location createLocation(File file) {
		String string = file.getPath();
		string = string.replace('\\', '/');
		String basename = trimSep(string);
		return new Location(basename);
	}

	public static Location createLocation(File file, Location parent) {
		String basename = trimSep(file.getName());
		return new Location(parent, basename);
	}

	@Override
	public int compareTo(Location o) {
		return path.compareTo(o.path);
	}

	public boolean isSideboard() {
		return path.endsWith(SIDEBOARD_SUFFIX);
	}

	public Location toSideboard() {
		if (isSideboard())
			return this;
		return new Location(path + SIDEBOARD_SUFFIX);
	}

	public Location getParent() {
		if (this == NO_WHERE)
			return NO_WHERE;
		String parent = new File(path).getParent();
		if (parent == null || parent.isEmpty() || parent.equals(SEP))
			return NO_WHERE;
		return new Location(parent);
	}

	public String getPath() {
		return path;
	}

	public File getFile() {
		return new File(DataManager.getInstance().getRootDir(), path + XML_SUFFIX);
	}

	public String getBaseFileName() {
		return getName() + XML_SUFFIX;
	}

	public Location toMainDeck() {
		if (!isSideboard())
			return this;
		return new Location(path.replaceAll(SIDEBOARD_SUFFIX + "$", ""));
	}

	public Location append(String name) {
		return new Location(this, trimSep(name));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Location))
			return false;
		Location other = (Location) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
