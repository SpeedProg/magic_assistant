package com.reflexit.magiccards.core.seller;

import java.net.URL;

import com.reflexit.magiccards.core.sync.IStoreUpdator;

public interface IPriceProvider extends IStoreUpdator {
	String getName();

	URL getURL();
}
