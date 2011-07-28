package com.reflexit.magiccards.core.seller;

import java.net.URL;


public interface IPriceProvider extends IStoreUpdator {
	String getName();

	URL getURL();
}
