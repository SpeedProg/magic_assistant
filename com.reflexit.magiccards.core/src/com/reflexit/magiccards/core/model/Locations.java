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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ISearchableProperty#getIdPrefix()
	 */
	public String getIdPrefix() {
		return FilterHelper.LOCATION;
	}

	public static Locations getInstance() {
		return instance;
	}

	public Collection getNames() {
		ModelRoot modelRoot = DataManager.getModelRoot();
		Map map = modelRoot.getLocationsMap();
		return new ArrayList(map.keySet());
	}

	public Collection getIds() {
		ModelRoot modelRoot = DataManager.getModelRoot();
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
		return FilterHelper.getPrefConstant(getIdPrefix(), name);
	}

	public String getNameById(String id1) {
		ModelRoot modelRoot = DataManager.getModelRoot();
		Map map = modelRoot.getLocationsMap();
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			Location loc = (Location) iterator.next();
			String id = getPrefConstant(loc);
			if (id1.equals(id))
				return loc.toString();
		}
		return null;
	}
}
