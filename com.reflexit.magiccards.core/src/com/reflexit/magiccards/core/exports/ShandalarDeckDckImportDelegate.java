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
public class ShandalarDeckDckImportDelegate extends AbstractImportDelegate {
	private DeckParser parser;

	@Override
	public ReportType getType() {
		return ReportType.createReportType("dck", "Shandalar .dck");
	}

	/*-
	;Azaar - Lichlord
	;Black
	;Coyote Tex
	;Coyote Tex@AOL.Com
	;September 19, 1996
	;7
	;4th Edition
	;comments

	.239	22	Swamp
	.55	4	Dark Ritual
	.230	1	Sol Ring
	.166	1	Mox Jet
	.17	1	Black Lotus
	.18	1	Black Vise
	.162	1	Mind Twist
	.68	4	Drain Life
	.117	4	Hypnotic Specter
	.220	4	Sengir Vampire
	.534	3	Tetravus
	.151	2	Lord of the Pit
	.661	2	Greed
	.286	3	Will-O'-The-Wisp
	.100	1	Gloom
	.336	3	Bog Imp
	.113	4	Howl from Beyond
	 */
	@Override
	protected void doRun(ICoreProgressMonitor monitor) throws IOException {
		parser = new DeckParser(getStream(), this);
		parser.addPattern(Pattern.compile("^\\.\\d+\\s*(\\d+)\\s+(.*)"), //
				new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME });
		importResult.setFields(new ICardField[] { MagicCardField.NAME, MagicCardFieldPhysical.COUNT, MagicCardField.SET });
		lineNum = 0;
		String set = "";
		// read header
		do {
			lineNum++;
			try {
				String sline = parser.readLine();
				if (sline == null)
					break;
				if (sline.startsWith(";")) {
					if (lineNum == 7) {
						set = sline.substring(1).trim();
					}
				} else if (sline.startsWith(".")) {
					MagicCardPhysical card = createDefaultCard();
					// card.getBase().setSet(set);
					if (parser.parseLine(card, sline)) {
						importCard(card);
					}
				}
				monitor.worked(1);
			} catch (IOException e) {
				throw e;
			}
		} while (true);
		parser.close();
	}
}
