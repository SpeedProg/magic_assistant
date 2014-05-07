package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * 
 * @author Alena
 */
public class ManaDeckDckImportDelegate extends AbstractImportDelegate {
	private DeckParser parser;
	private boolean sideboard;

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
		importResult.setFields(new ICardField[] { MagicCardField.NAME, MagicCardField.COUNT, MagicCardField.SET,
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