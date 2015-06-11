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
public class MagicWorkstationDeckImportDelegate extends AbstractImportDelegate {
	@Override
	protected void doRun(ICoreProgressMonitor monitor) throws IOException {
		runDeckImport(monitor);
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
	@Override
	public String getExample() {
		return ""
				+ "// Deck file for Magic Workstation (http://www.magicworkstation.com)\n"
				+ "\n"
				+ "// Lands\n"
				+ "3 [TE] Ancient Tomb\n"
				+ "4 [EX] City of Traitors\n"
				+ "13 [US] Mountain (1)\n"
				+ "\n"
				+ "// Creatures\n"
				+ "4 [UD] Covetous Dragon\n"
				+ "1 [US] Karn, Silver Golem\n"
				+ "3 [UD] Masticore\n"
				+ "\n"
				+ "// Spells\n"
				+ "4 [TE] Cursed Scroll\n"
				+ "4 [6E] Fire Diamond\n"
				+ "4 [UL] Grim Monolith\n"
				+ "2 [US] Mishra's Helix\n"
				+ "4 [US] Temporal Aperture\n"
				+ "4 [UD] Thran Dynamo\n"
				+ "4 [US] Voltaic Key\n"
				+ "4 [US] Wildfire\n"
				+ "2 [US] Worn Powerstone\n"
				+ "\n"
				+ "// Sideboard\n"
				+ "SB: 1 [US] Mishra's Helix\n"
				+ "SB: 2 [TE] Boil\n"
				+ "SB: 3 [6E] Earthquake\n"
				+ "SB: 1 [US] Phyrexian Processor\n"
				+ "SB: 2 [UL] Rack and Ruin\n"
				+ "SB: 2 [EX] Shattering Pulse\n"
				+ "SB: 4 [EX] Spellshock\n";
	}

	public void runDeckImport(ICoreProgressMonitor monitor) throws IOException {
		DeckParser parser = new DeckParser(getStream(), this);
		try {
			parser.addPattern(
					Pattern.compile("^\\s*(\\d+) \\[(.*)\\] ([^(]*)"), //
					new ICardField[] { MagicCardField.COUNT, MagicCardField.EDITION_ABBR, MagicCardField.NAME });
			parser.addPattern(Pattern.compile("^\\s*(\\d+)\\s+([^(]*)"), //
					new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
			parser.addPattern(Pattern.compile("^(SB): \\s*(\\d+) \\[(.*)\\] ([^(]*)"), //
					new ICardField[] { MagicCardField.SIDEBOARD, MagicCardField.COUNT,
							MagicCardField.EDITION_ABBR,
							MagicCardField.NAME });
			parser.addPattern(Pattern.compile("^(SB): \\s*(\\d+)\\s+([^(]*)"), //
					new ICardField[] { MagicCardField.SIDEBOARD, MagicCardField.COUNT, MagicCardField.NAME });
			importData.setFields(new ICardField[] { MagicCardField.NAME, MagicCardField.COUNT,
					MagicCardField.SET,
					MagicCardField.SIDEBOARD });
			do {
				lineNum++;
				try {
					MagicCardPhysical card = createDefaultCard();
					card = parser.readLine(card);
					if (card == null)
						break;
					importCard(card);
					monitor.worked(1);
				} catch (IOException e) {
					throw e;
				}
			} while (true);
		} finally {
			parser.close();
		}
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.SIDEBOARD) {
			if (value.equals("SB")) {
				card.setLocation(getSideboardLocation());
			}
		} else if (field == MagicCardField.SET) {
			super.setFieldValue(card, MagicCardField.EDITION_ABBR, i, value);
		} else {
			super.setFieldValue(card, field, i, value);
		}
	}
}
