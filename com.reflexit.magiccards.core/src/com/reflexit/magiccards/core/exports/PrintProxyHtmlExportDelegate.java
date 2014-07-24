package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.xml.MyXMLStreamWriter;
import com.reflexit.magiccards.core.xml.XMLStreamException;

public class PrintProxyHtmlExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public static final String CARD_URI = "card://";
	public static final String CARDID = "cardId=";

	@Override
	public void export(ICoreProgressMonitor monitor) throws InvocationTargetException {
		monitor.beginTask("Exporting " + getName(), 100);
		try {
			if (store.getCardStore().size() > 0) {
				IMagicCard card = store.getCardStore().iterator().next();
				if (card instanceof ILocatable) {
					location = ((ILocatable) card).getLocation();
					if (location != null)
						location = location.toMainDeck();
				}
			}
			try {
				MyXMLStreamWriter w = new MyXMLStreamWriter(stream);
				writeHtml(w);
				w.close();
			} catch (XMLStreamException e) {
				throw new InvocationTargetException(e);
			}
		} finally {
			monitor.done();
		}
	}

	private void writeHtml(MyXMLStreamWriter w) throws XMLStreamException {
		/*-
		<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
		"http://www.w3.org/TR/html4/loose.dtd">

		<html lang="en">
		<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Print Proxies</title>
		<style type="text/css">
		<!--
		body, html, img, a {margin: 0; padding: 0; border: none;}
		#back {display: none;padding: 5px; background-color: yellow;font-size: 150%;}
		@media screen { #back {display: block; position: absolute; right: 0; top: 0;} }
		-->
		</style>
		</head>
		<body>
		...
		</body>
		</html>
		 */
		w.writeDirect("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		w.startEl("html", "xmlns", "http://www.w3.org/1999/xhtml");
		w.startEl("head");
		w.el("title", "Print Proxies");
		w.lineEl("meta", "http-equiv", "Content-Type", "content", "text/html; charset=utf-8");
		w.endEl();
		w.startEl("body");
		body(w);
		w.endEl();
		w.endEl();
	}

	public void body(MyXMLStreamWriter w) throws XMLStreamException {
		// list
		list(w, getCardStore());
	}

	private ICardStore<IMagicCard> getMainStore(Location loc) {
		return DataManager.getCardStore(loc.toMainDeck());
	}

	private ICardStore<IMagicCard> getSideboardStore(Location loc) {
		return DataManager.getCardStore(loc.toSideboard());
	}

	private void list(MyXMLStreamWriter w, Iterable<IMagicCard> flat) throws XMLStreamException {
		/*-
		<img src="http://magiccards.info/scans/en/m14/102.jpg" alt="Proxy" height="319" width="222" style="margin: 0 1px 1px 0;"></a>
		
		 */
		for (IMagicCard card : flat) {
			w.nl();
			if (card instanceof ICardCountable) {
				int count = ((ICardCountable) card).getCount();
				for (int i = 0; i < count; i++) {
					cardImage(w, card);
				}
			} else {
				cardImage(w, card);
			}
			// w.lineEl("br");
		}
	}

	private void cardImage(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		String url = "";
		try {
			url = CardCache.getImageURL(card).toExternalForm();
		} catch (MalformedURLException e) {
			// oki
		}
		w.lineEl("img", "src", url, "alt", card.getName(), "style", "margin: 0 1px 1px 0;");
	}

	private ICardStore<IMagicCard> getCardStore() {
		return store.getCardStore();
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}

	@Override
	public boolean isMultipleLocationSupported() {
		return true;
	}

	@Override
	public boolean isSideboardSupported() {
		return true;
	}
}
