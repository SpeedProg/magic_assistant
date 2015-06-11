package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 *
 * @author Alena
 */
public class ManaDeckDckImportDelegate extends AbstractImportDelegate {
	private DeckParser parser;
	private boolean sideboard;

	@Override
	public String getExample() {
		return "#DCK#\n" +
				"UG Madness\n" +
				"#DECK#\n" +
				"2 City of Brass\n" +
				"11 Island\n" +
				"9 Forest\n" +
				"4 Wild Mongrel\n" +
				"4 Basking Rootwalla\n" +
				"4 Aquamoeba\n" +
				"4 Arrogant Wurm\n" +
				"3 Wonder\n" +
				"3 Roar of the Wurm\n" +
				"3 Deep Analysis\n" +
				"4 Circular Logic\n" +
				"4 Careful Study\n" +
				"2 Unsummon\n" +
				"1 Ray of Revelation\n" +
				"2 Quiet Speculation\n" +
				"#SIDEBOARD#\n" +
				"2 Ray of Revelation\n" +
				"2 Gigapede\n" +
				"2 Counterspell\n" +
				"2 Turbulent Dreams\n" +
				"2 Upheaval\n" +
				"2 Ravenous Baloth\n" +
				"2 Merfolk Looter\n" +
				"1 Krosan Reclamation";
	}

	@Override
	public synchronized void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (sideboard) {
			card.setLocation(getSideboardLocation());
		}
		super.setFieldValue(card, field, i, value);
	}

	@Override
	protected void doRun(ICoreProgressMonitor monitor) throws IOException {
		parser = new DeckParser(getStream(), this) {
			@Override
			public boolean parseLine(MagicCardPhysical res, String sline) {
				if (sline.equals("#SIDEBOARD#")) {
					sideboard = true;
					return false;
				}
				return super.parseLine(res, sline);
			}
		};
		parser.addPattern(Pattern.compile("^\\s*(\\d+)\\s+([^(]*)"), //
				new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		importData.setFields(new ICardField[] { MagicCardField.NAME, MagicCardField.COUNT,
				MagicCardField.SET,
				MagicCardField.SIDEBOARD });
		sideboard = false;
		parseText(monitor);
		parser.close();
	}

	public void parseText(ICoreProgressMonitor monitor) throws IOException {
		lineNum = 1;
		do {
			try {
				String sline = parser.readLine();
				if (sline == null)
					break;
				MagicCardPhysical card = createDefaultCard();
				boolean found = parser.parseLine(card, sline);
				if (found) {
					importCard(card);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				lineNum++;
				monitor.worked(1);
			}
		} while (true);
	}
}
