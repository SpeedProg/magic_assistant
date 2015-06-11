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
public class ShandalarDeckDckImportDelegate extends AbstractImportDelegate {
	private DeckParser parser;

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
				new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		importData.setFields(new ICardField[] { MagicCardField.NAME, MagicCardField.COUNT,
				MagicCardField.SET });
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
					card.getBase().setSet(set);
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

	@Override
	public String getExample() {
		return ""
				+ ";Azaar - Lichlord\n"
				+ ";Black\n"
				+ ";Coyote Tex\n"
				+ ";Coyote Tex@AOL.Com\n"
				+ ";September 19, 1996\n"
				+ ";7\n"
				+ ";4th Edition\n"
				+ ";comments\n"
				+ "\n"
				+ ".239	22	Swamp\n"
				+ ".55	4	Dark Ritual\n"
				+ ".230	1	Sol Ring\n"
				+ ".166	1	Mox Jet\n"
				+ ".17	1	Black Lotus\n"
				+ ".18	1	Black Vise\n"
				+ ".162	1	Mind Twist\n"
				+ ".68	4	Drain Life\n"
				+ ".117	4	Hypnotic Specter\n"
				+ ".220	4	Sengir Vampire\n"
				+ ".534	3	Tetravus\n"
				+ ".151	2	Lord of the Pit\n"
				+ ".661	2	Greed\n"
				+ ".286	3	Will-O'-The-Wisp\n"
				+ ".100	1	Gloom\n"
				+ ".336	3	Bog Imp\n"
				+ ".113	4	Howl from Beyond\n";
	}
}
