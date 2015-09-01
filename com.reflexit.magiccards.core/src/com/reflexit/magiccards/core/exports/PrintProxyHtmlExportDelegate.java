package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
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
		body, html, img, a {margin: 0; padding: 0; border: none;}
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
		w.startEl("style", "text", "text/css");
		w.nl();
		w.data("body, html, img, div, a {margin: 0; padding: 0; border: none;}");
		w.endEl();
		w.lineEl("meta", "http-equiv", "Content-Type", "content", "text/html; charset=utf-8");
		w.endEl();
		w.startEl("body");
		body(w);
		w.endEl();
		w.endEl();
	}

	public void body(MyXMLStreamWriter w) throws XMLStreamException {
		// list
		list(w, store);
	}

	private void list(MyXMLStreamWriter w, Iterable<IMagicCard> flat) throws XMLStreamException {
		w.startEl("div", "style", "font-size:0px;");
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
		w.endEl();
	}

	private void cardImage(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		/*-
		<img src="http://magiccards.info/scans/en/m14/102.jpg" alt="Proxy" />		
		 */
		String url = "";
		try {
			url = CardCache.getImageURL(card).toExternalForm();
		} catch (MalformedURLException e) {
			// oki
		}
		w.lineEl("img", "src", url, "alt", card.getName());
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
