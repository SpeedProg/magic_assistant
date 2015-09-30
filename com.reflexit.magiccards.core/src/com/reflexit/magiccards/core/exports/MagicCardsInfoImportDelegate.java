package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.ParseMagicCardsInfoChecklist;
import com.reflexit.magiccards.core.sync.ParserHtmlHelper;
import com.reflexit.magiccards.core.sync.WebUtils;

public class MagicCardsInfoImportDelegate extends AbstractImportDelegate {
	private ParserHtmlHelper parser;

	@Override
	public void setReportType(ReportType reportType) {
		super.setReportType(reportType);
		reportType.setProperty("url_regex", "http://magiccards.info.*");
	}

	@Override
	public void init(ImportData result) {
		super.init(result);
		importData.setFields(
				new ICardField[] { MagicCardField.NAME, MagicCardField.TYPE, MagicCardField.COST, MagicCardField.SET,
						MagicCardField.LANG, MagicCardField.RARITY, MagicCardField.COLLNUM, MagicCardField.ARTIST });
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
		String property = importData.getProperty(ImportSource.URL.name());
		if (property == null)
			throw new IOException("URL is not defined");
		URL url = new URL(property);
		Map<String, String> map = WebUtils.splitQuery(url);
		String vparam = map.get("v");
		if (vparam == null)
			throw new IOException("Cannot parser this url, missing v param");
		if (!vparam.equals("list")) {
			property = property.replace("v=" + vparam, "v=list");
			url = new URL(property);
		}
		parser = new ParseMagicCardsInfoChecklist();
		parser.loadSingleUrl(url, handler);
	}
}
