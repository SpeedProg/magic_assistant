package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * 
 * @author Alena
 */
public class MagicWorkstationDeckImportDelegate extends AbstractImportDelegate {
	@Override
	public ReportType getType() {
		return ReportType.createReportType("mwdeck", "Magic Workstation Deck");
	}

	@Override
	protected void doRun(ICoreProgressMonitor monitor) throws IOException {
		fixEditions();
		runDeckImport(monitor);
	}

	static void fixEditions() {
		Editions editions = Editions.getInstance();
		editions.addAbbreviation("Limited Edition Beta", "B");
		editions.addAbbreviation("Limited Edition Alpha", "A");
		editions.addAbbreviation("Torment", "TO");
		editions.addAbbreviation("Judgment", "JU");
		editions.addAbbreviation("Urza's Legacy", "UL");
		editions.addAbbreviation("Urza's Saga", "US");
		editions.addAbbreviation("Urza's Destiny", "UD");
		editions.addAbbreviation("Stronghold", "SH");
		editions.addAbbreviation("Onslaught", "ON");
		editions.addAbbreviation("Scourge", "SC");
		editions.addAbbreviation("Mirrodin", "MR");
		editions.addAbbreviation("Fifth Dawn", "FD");
		// editions.addAbbreviation("Anthologies", "AT");
	}

	/*-
	 // Deck file for Magic Workstation (http://www.magicworkstation.com)

	// Lands
	3 [TE] Ancient Tomb
	4 [EX] City of Traitors
	13 [US] Mountain (1)

	// Creatures
	4 [UD] Covetous Dragon
	1 [US] Karn, Silver Golem
	3 [UD] Masticore

	// Spells
	4 [TE] Cursed Scroll
	4 [6E] Fire Diamond
	4 [UL] Grim Monolith
	2 [US] Mishra's Helix
	4 [US] Temporal Aperture
	4 [UD] Thran Dynamo
	4 [US] Voltaic Key
	4 [US] Wildfire
	2 [US] Worn Powerstone

	// Sideboard
	SB: 1 [US] Mishra's Helix
	SB: 2 [TE] Boil
	SB: 3 [6E] Earthquake
	SB: 1 [US] Phyrexian Processor
	SB: 2 [UL] Rack and Ruin
	SB: 2 [EX] Shattering Pulse
	SB: 4 [EX] Spellshock

	 */
	public void runDeckImport(ICoreProgressMonitor monitor) throws IOException {
		DeckParser parser = new DeckParser(getStream());
		parser.addPattern(Pattern.compile("^\\s*(\\d+) \\[(.*)\\] ([^(]*)"), //
				new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.SET, MagicCardField.NAME });
		do {
			line++;
			try {
				MagicCardPhysical card = createDefaultCard();
				card = parser.readLine(card);
				previewResult.setFields(parser.getCurrentFields());
				if (card == null)
					break;
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
}
