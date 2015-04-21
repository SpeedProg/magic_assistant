package com.reflexit.magiccards.core.model.nav;

import java.io.File;

public class LocationPath {
	private final String SEP = "/";
	private final String path;
	public static final LocationPath ROOT = new LocationPath("") {
		@Override
		public LocationPath append(String end) {
			return new LocationPath(end);
		}
	};

	public LocationPath(String path) {
		super();
		this.path = path;
	}

	public LocationPath append(String end) {
		return new LocationPath(path + SEP + end);
	}

	public String getFileExtensionWithDot() {
		String lastSegment = lastSegment();
		int index = lastSegment.lastIndexOf('.');
		if (index == -1) {
			return "";
		}
		return lastSegment.substring(index);
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

	public String getName() {
		return lastSegment();
	}

	/**
	 * Name without extension
	 *
	 * @return
	 */
	public String getBaseName() {
		String lastSegment = lastSegment();
		int index = lastSegment.lastIndexOf('.');
		if (index == -1) {
			return lastSegment;
		}
		return lastSegment.substring(0, index);
	}

	/**
	 * Path without extension
	 *
	 * @return
	 */
	public String getId() {
		int sepIndex = path.lastIndexOf(SEP);
		int index = path.lastIndexOf('.');
		if (sepIndex > index) {
			return path;
		}
		if (index == -1) {
			return path;
		}
		return path.substring(0, index);
	}

	public String toPortableString() {
		return path;
	}

	public String getHead() {
		String top = path;
		while (top.startsWith(SEP)) {
			top = top.substring(1);
		}
		int k = top.indexOf(SEP);
		if (k > 0) {
			top = top.substring(0, k);
		}
		return top;
	}

	public LocationPath getTail() {
		String top = path;
		while (top.startsWith(SEP)) {
			top = top.substring(1);
		}
		int k = top.indexOf(SEP);
		String rest = "";
		if (k > 0) {
			rest = top.substring(k + 1);
		}
		return new LocationPath(rest);
	}

	public boolean isEmpty() {
		return path.length() == 0;
	}

	public boolean isRoot() {
		String top = path;
		while (top.startsWith(SEP)) {
			top = top.substring(1);
		}
		if (top.length() == 0)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof LocationPath)) return false;
		LocationPath other = (LocationPath) obj;
		if (path.equals(other.path)) return true;
		if (getId().equals(other.getId())) return true;
		return false;
	}
}
