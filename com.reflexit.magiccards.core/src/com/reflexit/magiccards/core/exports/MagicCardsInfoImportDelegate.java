package com.reflexit.magiccards.core.exports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.ParseMagicCardsInfoChecklist;
import com.reflexit.magiccards.core.sync.ParserHtmlHelper;

public class MagicCardsInfoImportDelegate extends AbstractImportDelegate {
	private ParseMagicCardsInfoChecklist parser;

	@Override
	public void init(InputStream st, ImportData result) {
		super.init(st, result);
		parser = new ParseMagicCardsInfoChecklist();
		importData.setFields(new ICardField[] { MagicCardField.NAME, MagicCardField.TYPE, MagicCardField.COST,
				MagicCardField.RARITY, MagicCardField.ARTIST, MagicCardField.SET });
	}

	/**
	 * <th><b>Card name</b></th>
	 * <th><b>Type</b></th>
	 * <th><b>Mana</b></th>
	 * <th><b>Rarity</b></th>
	 * <th><b>Artist</b></th>
	 * <th><b>Edition</b></th>
	 */
	@Override
	protected void doRun(ICoreProgressMonitor monitor) throws IOException {
		ParserHtmlHelper.StashLoadHandler handler = new ParserHtmlHelper.StashLoadHandler() {
			@Override
			public void handleCard(MagicCard card) {
				super.handleCard(card);
				int id = card.syntesizeId();
				card.setCardId(id);
				importData.add(card);
			}
		};
		parser.processFromReader(new BufferedReader(new InputStreamReader(getStream(), FileUtils.CHARSET_UTF_8)),
				handler);

	}
}
