package com.reflexit.magiccards.core.legality;

import com.reflexit.magiccards.core.NotNull;

public class Format {
	public static final Format STANDARD = new ConstructedFormat("Standard");
	public static final Format MODERN = new ConstructedFormat("Modern");
	@NotNull
	private final String name;

	public Format(String name) {
		this.name = name.intern();
	}

	@Override
	public int hashCode() {
		return 23 + name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Format other = (Format) obj;
		if (!name.equals(other.name))
			return false;
		return true;
	}

	public int getMainDeckCount() {
		return -1;
	}

	public String name() {
		return name;
	}
}
