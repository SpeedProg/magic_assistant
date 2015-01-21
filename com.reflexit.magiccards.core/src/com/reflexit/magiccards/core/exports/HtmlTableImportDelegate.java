package com.reflexit.magiccards.core.exports;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;

public class HtmlTableImportDelegate extends TableImportDelegate {
	private HashMap<String, String> setaliases = new HashMap<String, String>();

	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		try {
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(getStream()));
				String text = FileUtils.readFileAsString(importer);
				String[] tables = text.split("<table[> ]");
				if (tables.length <= 1)
					throw new MagicException("Tag <table> is not found");
				String tbodymain = null;
				if (tables.length >= 3) {
					for (String tbody : tables) {
						if (tbody.contains("CollectionTable")) { // TCGPlayer
							tbodymain = tbody;
							// reverse parses aliases map
							Map<String, String> setAliasesMap = ParseTcgPlayerPrices.getSetAliasesMap();
							for (String key : setAliasesMap.keySet()) {
								setaliases.put(setAliasesMap.get(key), key);
							}
						}
					}
					if (tbodymain == null)
						throw new MagicException("To many <table> tags are found");
				} else {
					tbodymain = tables[1];
				}
				loadTable(tbodymain);
			} catch (FileNotFoundException e) {
				throw e;
			} finally {
				if (importer != null)
					importer.close();
			}
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	protected ICardField getFieldByName(String hd) {
		hd = hd.toUpperCase();
		ICardField fieldByName = super.getFieldByName(hd);
		if (fieldByName == null) {
			if (hd.equals("P"))
				return MagicCardField.POWER;
			if (hd.equals("T"))
				return MagicCardField.TOUGHNESS;
			if (hd.equals("GAME"))
				return null;
			if (hd.equals("HAVE"))
				return MagicCardField.COUNT;
		}
		return fieldByName;
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.SET) {
			value = resolveSet(value);
		} else if (field == MagicCardField.NAME) {
			value = resolveName(value, card);
		}
		super.setFieldValue(card, field, i, value);
	}

	protected String resolveName(String value, MagicCardPhysical card) {
		if (value.endsWith(" - [Foil]")) {
			value = value.substring(0, value.length() - 9);
			card.setSpecialTag("foil");
		}
		return value;
	}

	protected String resolveSet(String value) {
		String string = setaliases.get(value);
		if (string != null)
			value = string;
		Editions ed = Editions.getInstance();
		Edition edition = ed.getEditionByName(value);
		if (edition != null)
			return edition.getName();
		edition = ed.getEditionByNameIgnoreCase(value);
		if (edition != null) {
			setaliases.put(value, edition.getName());
			return edition.getName();
		}
		edition = ed.getEditionByAbbr(value);
		if (edition != null)
			return edition.getName();
		return null;
	}

	protected void loadTable(String string) {
		List<String> lines = splitArray("tr", string);
		List<String> headers = null;
		for (String line : lines) {
			if (line.contains("<th")) {
				setHeaderFields(headers = purifyList(splitArray("th", line)));
			} else {
				List<String> tbody = splitArray("td", line);
				if (headers == null) {
					setHeaderFields(headers = purifyList(tbody));
					continue;
				}
				if (tbody.size() < headers.size()) {
					continue;
				}
				MagicCardPhysical card = createCard(purifyList(tbody));
				importCard(card);
			}
		}
	}

	private List<String> purifyList(List<String> tbody) {
		List<String> fields = new ArrayList<String>(tbody.size());
		for (String in : tbody) {
			fields.add(purifyItem(in));
		}
		return fields;
	}

	private String purifyItem(String in) {
		in = in.replaceAll("<[^>]*>", "");
		in = in.replace("&nbsp;", " ");
		in = in.replace("&amp;", "&");
		in = in.trim();
		return in;
	}

	protected List<String> splitArray(String tag, String text) {
		String[] rows = text.split("<" + tag + "[^>]*>");
		ArrayList<String> res = new ArrayList<String>();
		for (String r : rows) {
			int x = r.indexOf("</" + tag + ">");
			if (x < 0)
				continue;
			res.add(r.substring(0, x));
		}
		return res;
	}
}
