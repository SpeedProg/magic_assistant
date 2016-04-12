package com.reflexit.magiccards.ui.preferences;

import com.reflexit.magiccards.core.CorePreferenceConstants;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants extends CorePreferenceConstants {
	private static final String PREFIX = MagicUIActivator.PLUGIN_ID;
	public static final String GATHERER_SITE = PREFIX + ".gathererSite";
	public static final String GATHERER_UPDATE = PREFIX + ".gathererUpdate";
	// public static final String MDBVIEW_COLS = PREFIX + ".columns.mdb";
	// public static final String LIBVIEW_COLS = PREFIX + ".columns.lib";
	// public static final String DECKVIEW_COLS = PREFIX + ".columns.deck";
	public static final String GATHERER_UPDATE_SET = PREFIX + ".set";
	public static final String GATHERER_UPDATE_LAND = PREFIX + ".land";
	public static final String GATHERER_UPDATE_LANGUAGE = PREFIX + ".lang";
	public static final String GATHERER_UPDATE_PRINT = PREFIX + ".printings";
	public static final String GATHERER_UPDATE_SPECIAL = PREFIX + ".special";
	public static final String CACHE_IMAGES = PREFIX + ".cacheImages";
	public static final String LOAD_IMAGES = PREFIX + ".loadImages";
	public static final String LOAD_RULINGS = PREFIX + ".loadRulings";
	public static final String LOAD_EXTRAS = PREFIX + ".loadExtra";
	public static final String LOAD_PRINTINGS = PREFIX + ".loadPrintings";
	public static final String PRICE_PROVIDER = PREFIX + ".priceProvider";
	public static final String SHOW_GRID = PREFIX + ".grid";
	public static final String LOCAL_SHOW_QUICKFILTER = "quickfilter";
	public static final String LOCAL_COLUMNS = "columnlayout";
	public static final String CHECK_FOR_UPDATES = "updates";
	public static final String CHECK_FOR_CARDS = "cardUpdates";
	public static final String OWNED_COPY = PREFIX + ".nomycopy";
	public static final String CURRENCY = PREFIX + ".currency";
	public static final String WORK_OFFLINE = PREFIX + ".offline";
	public static final String LAST_SELECTION = PREFIX + ".cardselection";
	public static final String GROUP_FIELD = FilterField.GROUP_FIELD.toString();
	public static final String SORT_ORDER = PREFIX + ".sortorder";
	public static final String PRESENTATION_VIEW = PREFIX + ".viewas";
}
