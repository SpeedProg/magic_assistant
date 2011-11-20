package com.reflexit.magiccards.core.model.nav;

import java.io.File;

public class LocationPath {
	private final String SEP = "/";
	private String path;

	public LocationPath(String path) {
		super();
		this.path = path;
	}

	public LocationPath addTrailingSeparator() {
		return new LocationPath(path + SEP);
	}

	public boolean hasTrailingSeparator() {
		return path.endsWith(SEP);
	}

	public LocationPath append(String end) {
		if (hasTrailingSeparator())
			return new LocationPath(path + end);
		else
			return new LocationPath(path + SEP + end);
	}

	public String getFileExtension() {
		String lastSegment = new File(path).getName();
		int index = lastSegment.lastIndexOf('.');
		if (index == -1) {
			return "";
		}
		return lastSegment.substring(index + 1);
	}

	public boolean isAbsolute() {
		return path.startsWith(SEP);
	}

	public File toFile() {
		return new File(path);
	}

	@Override
	public String toString() {
		return path.toString();
	}

	public String lastSegment() {
		return toFile().getName();
	}

	public String toPortableString() {
		return path;
	}

	public String[] splitTop() {
		String top = path;
		while (top.startsWith(SEP)) {
			top = top.substring(1);
		}
		int k = top.indexOf(SEP);
		String rest = "";
		if (k > 0) {
			rest = top.substring(k + 1);
			top = top.substring(0, k);
		}
		return new String[] { top, rest };
	}

	public boolean isEmpty() {
		return path.isEmpty();
	}

	public boolean isRoot() {
		String top = path;
		while (top.startsWith(SEP)) {
			top = top.substring(1);
		}
		if (top.isEmpty())
			return true;
		return false;
	}
}
