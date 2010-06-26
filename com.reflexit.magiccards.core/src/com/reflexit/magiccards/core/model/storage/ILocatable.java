package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.model.Location;

public interface ILocatable {
	void setLocation(Location location);

	Location getLocation();
}
