package com.reflexit.magiccards.core.seller;

import gnu.trove.map.TIntFloatMap;

import java.util.Properties;

public interface IPriceProviderStore {
	public abstract String getName();

	public abstract TIntFloatMap getPriceMap();

	public abstract Properties getProperties();
}