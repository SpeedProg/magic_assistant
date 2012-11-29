package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * 
 * @author Alena
 */
public class ManaDeckDckImportDelegate extends AbstractImportDelegate {
	private DeckParser parser;

	@Override
	public ReportType getType() {
		return ReportType.createReportType("dck", "Mana Deck .dck");
	}

	/*-
	 // Mana Deck format
	#DCK#
	UG Madness
	#DECK#
	2 City of Brass
	11 Island
	9 Forest
	4 Wild Mongrel
	4 Basking Rootwalla
	4 Aquamoeba
	4 Arrogant Wurm
	3 Wonder
	3 Roar of the Wurm
	3 Deep Analysis
	4 Circular Logic
	4 Careful Study
	2 Unsummon
	1 Ray of Revelation
	2 Quiet Speculation
	#SIDEBOARD#
	2 Ray of Revelation
	2 Gigapede
	2 Counterspell
	2 Turbulent Dreams
	2 Upheaval
	2 Ravenous Baloth
	2 Merfolk Looter
	1 Krosan Reclamation

	 */
	public synchronized void runDeckImport(ICoreProgressMonitor monitor) throws IOException {
		parser = new DeckParser(getStream(), this);
		parser.addPattern(Pattern.compile("^\\s*(\\d+)\\s+([^(]*)"), //
				new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME });
		parser.addPattern(Pattern.compile("^#SIDEBOARD#"), //
				"SIDEBOARD");
		do {
			line++;
			try {
				MagicCardPhysical card = createDefaultCard();
				card = parser.readLine(card);
				previewResult.setFields(parser.getCurrentFields());
				if (card == null)
					break;
				if (card.getCardId() == 0 && card.getName() == null)
					continue;
				importCard(card);
				if (previewMode && line >= 10)
					break;
				monitor.worked(1);
			} catch (IOException e) {
				throw e;
			}
		} while (true);
		parser.close();
	}

	@Override
	public synchronized void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if ("SIDEBOARD".equals(parser.state)) {
			card.setLocation(getLocation().toSideboard());
		}
		super.setFieldValue(card, field, i, value);
	}

	@Override
	protected void doRun(ICoreProgressMonitor monitor) throws IOException {
		runDeckImport(monitor);
	}
}
