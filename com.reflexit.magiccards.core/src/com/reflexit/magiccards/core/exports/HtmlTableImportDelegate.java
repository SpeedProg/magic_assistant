package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;
import com.reflexit.magiccards.core.sync.ParserHtmlHelper;

public class HtmlTableImportDelegate extends TableImportDelegate {
	private HashMap<String, String> setaliases = new HashMap<String, String>();
	{ // reverse parses aliases map
		Map<String, String> setAliasesMap = ParseTcgPlayerPrices.getSetAliasesMap();
		for (String key : setAliasesMap.keySet()) {
			setaliases.put(setAliasesMap.get(key), key);
		}
	}

	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		doRun(FileUtils.readStreamAsStringAndClose(getStream()));
	}

	protected void doRun(String text) {
		headers = null;
		importData.clear();
		text = text.replace("<TABLE", "<table");
		text = text.replace("<TR", "<tr");
		text = text.replace("<TD", "<td");
		text = text.replace("<TH", "<th");
		text = text.replace("TR>", "tr>");
		text = text.replace("TD>", "td>");
		text = text.replace("TH>", "th>");
		String[] tables = text.split("<table[> ]");
		if (tables.length <= 1)
			throw new MagicException("Tag <table> is not found");
		for (String tbody : tables) {
			if (tbody.contains("CollectionTable")) { // TCGPlayer
				loadTable(tbody);
				return;
			}
		}
		for (String tbodymain : tables) {
			loadTable(tbodymain);
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
			if (hd.equals("SET NAME"))
				return MagicCardField.SET;
			//			if (hd.equals("MID"))
			//				return MagicCardField.DBPRICE;
		}
		return fieldByName;
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.SET) {
			value = resolveSet(value);
		} else if (field == MagicCardField.NAME) {
			value = resolveName(value, card);
		} else if (field == MagicCardField.DBPRICE) {
			value = value.replace("$", "");
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

	private List<String> headers = null;

	protected void loadTable(String string) {
		List<String> lines = splitArray("tr", string);
		for (String line : lines) {
			if (line.contains("<th")) {
				setHeaderFields(headers = purifyList(splitArray("th", line)));
				//System.err.println("th -> " + headers);
			} else {
				List<String> tbody = splitArray("td", line);
				if (headers == null) {
					try {
						setHeaderFields(headers = purifyList(tbody));
					} catch (MagicException e) {
						headers = null;
					}
					//System.err.println("td -> " + headers);
					continue;
				}
				if (tbody.size() != headers.size()) {
					//System.err.println(tbody.size() + " != " + headers.size());
					continue;
				}
				MagicCardPhysical card = createCard(purifyList(tbody));
				importCard(card);
				//System.err.println(TextPrinter.values(card, Arrays.asList(MagicCardField.allNonTransientFields(true))));
				//System.err.println(tbody);
			}
		}
	}

	private List<String> purifyList(List<String> tbody) {
		return ParserHtmlHelper.purifyList(tbody);
	}

	protected List<String> splitArray(String tag, String text) {
		return ParserHtmlHelper.splitArray(tag, text);
	}

}
