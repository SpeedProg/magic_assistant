package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.GatherHelper;
import com.reflexit.magiccards.core.xml.MyXMLStreamWriter;
import com.reflexit.magiccards.core.xml.XMLStreamException;

public class CompactHtmlExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public static final String CARD_URI = "http://";
	public static final String CARDID = "multiverseid=";
	private static final String COLWIDTH = "40%";

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

	@Override
	public String getName() {
		return getMainCardStore().getName();
	}

	private void writeHtml(MyXMLStreamWriter w) throws XMLStreamException {
		/*-
		 <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
		"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

		<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
		<link href="http://www.wizards.com/magic/legacy.css" type="text/css" rel="stylesheet">
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		</head>
		 */
		w.writeDirect("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		w.startEl("html", "xmlns", "http://www.w3.org/1999/xhtml");
		w.startEl("head");
		w.lineEl("link", "href", "magicexport.css", "type", "text/css", "rel", "stylesheet");
		w.lineEl("meta", "http-equiv", "Content-Type", "content", "text/html; charset=utf-8");
		w.endEl(); // head
		w.startEl("body", "style", "overflow:auto;");
		header(w);
		maindeck(w);
		footer(w);
		w.endEl(); // body
		w.endEl(); // html
	}

	private void maindeck(MyXMLStreamWriter w) throws XMLStreamException {
		w.startEl("div", "class", "maindeck");
		w.startEl("table", "class", "cardgroup", "cellpadding", "10");
		w.startEl("tbody");
		// cards
		w.startEl("tr");
		maindeckCards(w);
		w.endEl();
		sideboardcards(w);
		// endcards
		w.endEl(); // tbody
		w.endEl(); // table
		w.lineEl("br");
		w.endEl(); // maindesk
		w.lineEl("br", "clear", "all");
	}

	public void maindeckCards(MyXMLStreamWriter w) throws XMLStreamException {
		ICardStore<IMagicCard> mainStore = filterByLocation(store, true);
		CardGroup group = CardStoreUtils.buildTypeGroups(mainStore);
		CardGroup top = (CardGroup) group.getChildAtIndex(0);
		w.startEl("td", "valign", "top", "width", COLWIDTH);
		// list
		CardGroup creature = (CardGroup) top.getChildAtIndex(2);
		listWithTotals(w, creature, "creatures");
		w.endEl(); // td
		w.startEl("td", "valign", "top", "width", COLWIDTH);
		CardGroup other = (CardGroup) top.getChildAtIndex(1);
		listWithTotals(w, other, "other spells");
		CardGroup land = (CardGroup) top.getChildAtIndex(0);
		listWithTotals(w, land, "lands");
		w.endEl(); // td
		// image column
		w.startEl("td", "valign", "top", "width", "185");
		w.lineEl("img", "src", "", "id", "card_pic", "style",
				"max-height: 223px; max-width: 310px; text-align: center; vertical-align: middle;", "alt", "");
		w.endEl(); // td
	}

	public void sideboardcards(MyXMLStreamWriter w) throws XMLStreamException {
		// sideboard
		ICardStore<IMagicCard> sbStore = getSideboardStore();
		if (sbStore != null && sbStore.size() > 0) {
			int count = ((ICardCountable) sbStore).getCount();
			// split sideboard in half
			int i = 0;
			int half = sbStore.size() - sbStore.size() / 2;
			ArrayList<IMagicCard> cards1 = new ArrayList<IMagicCard>();
			ArrayList<IMagicCard> cards2 = new ArrayList<IMagicCard>();
			for (IMagicCard card : sbStore) {
				if (i < half)
					cards1.add(card);
				else
					cards2.add(card);
				i++;
			}
			w.startEl("tr");
			w.startEl("td");
			totals(w, "Sideboard (" + count + ")");
			w.endEl();
			w.endEl();
			w.startEl("tr");
			w.startEl("td", "valign", "top", "width", COLWIDTH);
			list(w, cards1);
			w.endEl();// td
			w.startEl("td", "valign", "top", "width", COLWIDTH);
			list(w, cards2);
			w.endEl();// td
			w.endEl();
		}
	}

	private ICardStore<IMagicCard> filterByLocation(IFilteredCardStore<IMagicCard> store, boolean mainDeck) {
		MemoryCardStore<IMagicCard> store2 = new MemoryCardStore<IMagicCard>();
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof MagicCardPhysical) {
				Location loc = ((MagicCardPhysical) card).getLocation();
				if (mainDeck == !loc.isSideboard()) {
					store2.add(card);
				}
			}
		}
		return store2;
	}

	private ICardStore<IMagicCard> getMainCardStore() {
		ICardStore<IMagicCard> cardStore = DataManager.getInstance().getCardStore(location.toMainDeck());
		if (cardStore != null)
			return cardStore;
		return store.getCardStore();
	}

	private ICardStore<IMagicCard> getMainStore() {
		return filterByLocation(store, true);
	}

	private ICardStore<IMagicCard> getSideboardStore() {
		return filterByLocation(store, false);
	}

	public void listWithTotals(MyXMLStreamWriter w, CardGroup group, String type) throws XMLStreamException {
		totals(w, type + " (" + group.getCount() + ")");
		list(w, group.expand());
		w.lineEl("br");
	}

	private String cap1(String str) {
		if (str.isEmpty()) return "";
		return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
	}

	private void totals(MyXMLStreamWriter w, String totals) throws XMLStreamException {
		/*-
		<hr size="1" width="50%" align="left" class="decktotals">
		<span class="decktotals">26 lands</span><br><br>
		 */
		w.startEl("b");
		w.ela("i", cap1(totals), "class", "decltotals");
		w.endEl();
		w.lineEl("hr", "size", "1", "width", "50%", "align", "left", "class", "decltotals");
	}

	private void list(MyXMLStreamWriter w, Iterable<IMagicCard> flat) throws XMLStreamException {
		/*-
		24&nbsp;
		<a class="nodec" onmouseover="ChangeBigCard(1, this)" keyname="name" keyvalue="Plains" onclick="autoCardWindow(this)" href="javascript:void()">
		Plains</a><br>
		2&nbsp;
		<a class="nodec" onmouseover="ChangeBigCard(1, this)" keyname="name" keyvalue="Secluded_Steppe" onclick="autoCardWindow(this)" href="javascript:void()">
		Secluded Steppe</a>
		<br>

		 */
		for (IMagicCard card : flat) {
			printCardElement(w, card);
			w.lineEl("br");
		}
	}

	protected void printCardElement(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		w.nl();
		printCount(w, card);
		printNameAndLink(w, card);
	}

	protected void printCount(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		if (card instanceof ICardCountable) {
			w.data(((ICardCountable) card).getCount() + " ");
		}
	}

	protected void printNameAndLink(MyXMLStreamWriter w, IMagicCard card) throws XMLStreamException {
		String cardDetailUrl = GatherHelper.createImageDetailURL(card.getCardId()).toString();
		URL imageUrl = GatherHelper.createImageURL(card.getCardId());
		w.ela("a", cardLine(card), "href", cardDetailUrl, "onmouseover",
				"document.images.card_pic.src='" + imageUrl.toExternalForm() + "'");
	}

	protected String cardLine(IMagicCard card) {
		return card.getName();
	}

	private void header(MyXMLStreamWriter w) throws XMLStreamException {
		w.el("h3", cap1(getName()) + " (" + ((ICardCountable) getMainStore()).getCount() + ")");
		w.startEl("p", "class", "sub");
		w.data(getComment());
		w.endEl(); // sub title
	}

	private void footer(MyXMLStreamWriter w) throws XMLStreamException {
	}

	private String getComment() {
		return getMainCardStore().getComment();
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
