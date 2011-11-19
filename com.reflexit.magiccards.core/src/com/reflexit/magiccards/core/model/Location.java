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

import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.model.nav.LocationPath;

/**
 * Location represents path to a decl/collection
 */
public class Location implements Comparable<Location> {
	private static final String XML_SUFFIX = ".xml";
	public static final String SIDEBOARD_SUFFIX = "-sideboard";
	public static final Location NO_WHERE = new Location();
	private final String location;

	@Override
	public String toString() {
		return location;
	}

	private Location() {
		this.location = "";
	}

	public Location(String loc) {
		this(loc, NO_WHERE);
	}

	private Location(String loc, Location parent) {
		if (loc == null || loc.length() == 0)
			throw new IllegalArgumentException(loc);
		if (parent != Location.NO_WHERE)
			loc = parent.toString() + "/" + loc;
		if (loc.endsWith(XML_SUFFIX))
			loc = loc.replaceAll(XML_SUFFIX + "$", "");
		this.location = loc.intern();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
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
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	public String getName() {
		return new Path(location).removeFileExtension().lastSegment();
	}

	public static Location createLocation(LocationPath path) {
		String str = path.toPortableString();
		if (str.length() == 0)
			return NO_WHERE;
		return new Location(str);
	}

	public static Location createLocation(File file, Location parent) {
		String basename = file.getName();
		return new Location(basename, parent);
	}

	public int compareTo(Location o) {
		return location.compareTo(o.location);
	}

	public boolean isSideboard() {
		return location.endsWith(SIDEBOARD_SUFFIX);
	}

	public Location toSideboard() {
		if (isSideboard())
			return this;
		return new Location(location + SIDEBOARD_SUFFIX);
	}

	public Location getParent() {
		if (this == NO_WHERE)
			return NO_WHERE;
		return new Location(new Path(location).removeLastSegments(1).toPortableString());
	}

	public String getPath() {
		return location;
	}

	public String getBaseFileName() {
		return getName() + XML_SUFFIX;
	}

	public Location toMainDeck() {
		if (!isSideboard())
			return this;
		return new Location(location.replaceAll(SIDEBOARD_SUFFIX + "$", ""));
	}

	public Location append(String name) {
		return new Location(name, this);
	}
}
