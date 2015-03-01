package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.xml.MyXMLStreamWriter;
import com.reflexit.magiccards.core.xml.XMLStreamException;

public class CompactPlusHtmlExportDelegate extends CompactHtmlExportDelegate {
	protected boolean showImage;
	protected boolean showCost;

	public CompactPlusHtmlExportDelegate() {
	}

	@Override
	public void init(OutputStream st, boolean header, IFilteredCardStore cards) {
		super.init(st, header, cards);
		showImage = false;
		showCost = false;
		for (ICardField field : columns) {
			if (field == MagicCardField.SET || field == MagicCardField.RARITY) {
				showImage = true;
			} else if (field == MagicCardField.COST || field == MagicCardField.COLOR) {
				showCost = true;
			}
		}
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return true;
	}

	@Override
	protected void printCardElement(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		w.nl();
		printCount(w, card);
		printSetAndRarityImage(w, card);
		printNameAndLink(w, card);
		printCost(w, card);
	}

	protected void printCost(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		if (showCost) {
			String str = card.getCost();
			w.write(HtmlTableExportDelegate.replaceSymbolsWithLinksOnline(str));
		}
	}

	protected void printSetAndRarityImage(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		if (showImage) {
			try {
				URL seturl = CardCache.createSetImageURL(card, false);
				w.ela("img", "", "src", seturl.toExternalForm(),
						"width", "32",
						"height", "16",
						"valign", "center");
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
