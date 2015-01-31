package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

public class Locations implements ISearchableProperty {
	private Locations() {
	}

	static Locations instance = new Locations();

	@Override
	public String getIdPrefix() {
		return getFilterField().toString();
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.LOCATION;
	}

	public static Locations getInstance() {
		return instance;
	}

	@Override
	public Collection<String> getIds() {
		ModelRoot modelRoot = DataManager.getInstance().getModelRoot();
		Map map = modelRoot.getLocationsMap();
		ArrayList<String> list = new ArrayList<String>();
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			Location loc = (Location) iterator.next();
			String id = getPrefConstant(loc);
			list.add(id);
		}
		return list;
	}

	public String getPrefConstant(Location loc) {
		return getPrefConstant(loc.toString());
	}

	public String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	@Override
	public String getNameById(String id1) {
		Location loc = findLocation(id1);
		if (loc != null)
			return loc.toString();
		return null;
	}

	public boolean isSideboard(String id) {
		return id.endsWith(Location.SIDEBOARD_SUFFIX);
	}

	public Location findLocation(String locId) {
		ModelRoot modelRoot = DataManager.getInstance().getModelRoot();
		Map map = modelRoot.getLocationsMap();
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			Location loc = (Location) iterator.next();
			String id = getPrefConstant(loc);
			if (locId.equals(id))
				return loc;
		}
		return null;
	}
}
