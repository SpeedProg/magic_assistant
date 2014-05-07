package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.xml.MyXMLStreamWriter;
import com.reflexit.magiccards.core.xml.XMLStreamException;

public class WizardsHtmlExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public static final String CARD_URI = "card://";
	public static final String CARDID = "cardId=";

	@Override
	public void export(ICoreProgressMonitor monitor) throws InvocationTargetException {
		monitor.beginTask("Exporting " + getName(), 100);
		try {
			if (store.getCardStore().size() > 0) {
				IMagicCard card = (IMagicCard) store.getCardStore().iterator().next();
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
		w.lineEl("link", "href", "http://www.wizards.com/magic/legacy.css", "type", "text/css", "rel", "stylesheet");
		w.lineEl("meta", "http-equiv", "Content-Type", "content", "text/html; charset=utf-8");
		w.endEl();
		w.startEl("body", "style", "overflow:auto;");
		header(w);
		maindeck(w);
		footer(w);
		w.endEl();
		w.endEl();
	}

	private void footer(MyXMLStreamWriter w) throws XMLStreamException {
		w.endEl(); // deck
		w.endEl(); // content
	}

	private void maindeck(MyXMLStreamWriter w) throws XMLStreamException {
		/*-
		<div class="maindeck">
		<div class="maindeckmiddle">
		<div style="position: relative;">
		  <table class="cardgroup">
		    <tbody><tr>
		      <td align="center" colspan="2">
		        <p class="decktitle">Main Deck</p>
		        <p class="cardcount">60 cards
									</p>
		      </td>
		      <td align="center" valign="top" style="width:230px">
		    </td></tr>
		 */
		w.startEl("div", "class", "maindeck");
		w.startEl("div", "class", "maindeckmiddle");
		w.startEl("div", "style", "position: relative;");
		w.startEl("table", "class", "cardgroup");
		w.startEl("tbody");
		w.startEl("tr");
		w.startEl("td", "align", "center", "colspan", "2");
		w.ela("p", "Main Deck", "class", "decktitle");
		w.ela("p", ((ICardCountable) getCardStore()).getCount() + " cards", "class", "cardcount");
		w.endEl(); // td
		w.startEl("td", "align", "center", "valign", "top", "style", "width:230px");
		w.endEl(); // td
		w.endEl(); // tr
		// cards
		w.startEl("tr");
		maindeckCards(w);
		w.endEl();
		// endcards
		w.endEl(); // tbody
		w.endEl(); // table
		w.lineEl("br");
		w.endEl(); // style
		w.endEl(); // maindeskmiddle
		w.endEl(); // maindesk
		w.lineEl("br", "clear", "all");
	}

	public void maindeckCards(MyXMLStreamWriter w) throws XMLStreamException {
		CardGroup group = CardStoreUtils.buildTypeGroups(getCardStore());
		CardGroup top = (CardGroup) group.getChildAtIndex(0);
		CardGroup land = (CardGroup) top.getChildAtIndex(0);
		w.startEl("td", "valign", "top", "width", "185");
		// list
		listWithTotals(w, land, "lands");
		CardGroup spell = (CardGroup) top.getChildAtIndex(1);
		CardGroup creature = (CardGroup) spell.getChildAtIndex(1);
		listWithTotals(w, creature, " creatures");
		w.endEl(); // td
		w.startEl("td", "valign", "top", "width", "185");
		CardGroup other = (CardGroup) spell.getChildAtIndex(0);
		listWithTotals(w, other, "other spells");
		// sideboard
		ICardStore<IMagicCard> sbStore = getSideboardStore(location);
		if (sbStore != null) {
			// <div class="decktitle" style="padding-bottom:8px;"><b><i>Sideboard</i></b></div>
			w.startEl("div", "class", "decktitle", "style", "padding-bottom:8px;");
			w.startEl("b");
			w.el("i", "Sideboard");
			w.endEl(); // b
			w.endEl(); // div
			list(w, sbStore);
			totals(w, ((ICardCountable) sbStore).getCount() + " sideboad cards");
		}
		w.endEl(); // td
		w.startEl("td", "valign", "top", "width", "185");
		w.endEl(); // td
	}

	private ICardStore<IMagicCard> getMainStore(Location loc) {
		return DataManager.getCardStore(loc.toMainDeck());
	}

	private ICardStore<IMagicCard> getSideboardStore(Location loc) {
		return DataManager.getCardStore(loc.toSideboard());
	}

	public void listWithTotals(MyXMLStreamWriter w, CardGroup group, String type) throws XMLStreamException {
		list(w, group.expand());
		totals(w, group.getCount() + " " + type);
	}

	private void totals(MyXMLStreamWriter w, String totals) throws XMLStreamException {
		/*-
		<hr size="1" width="50%" align="left" class="decktotals">
		<span class="decktotals">26 lands</span><br><br>
		 */
		w.lineEl("hr", "size", "1", "width", "50%", "align", "left", "class", "decltotals");
		w.ela("span", totals, "class", "decltotals");
		w.lineEl("br");
		w.lineEl("br");
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
			w.nl();
			if (card instanceof ICardCountable) {
				w.data(((ICardCountable) card).getCount() + " ");
				w.ela("a", card.getName(), "href", CARD_URI + CARDID + card.getCardId());
			}
			w.lineEl("br");
		}
	}

	private void header(MyXMLStreamWriter w) throws XMLStreamException {
		/*-
		<div id="content">
		<div class="deck">
		<div class="decktop">
		<div class="decktopmiddle">
		<div style="float:left">
		  <div class="main">
		    <heading>Divine</heading>
		  </div>
		  <div class="sub">Duel Decks: Divine vs. Demonic</div>
		</div>
		<br class="clear">
		</div>
		</div>
		 */
		w.startEl("div", "id", "content");
		w.startEl("div", "class", "deck");
		// top
		w.startEl("div", "class", "decktop");
		w.startEl("div", "class", "decktopmiddle");
		w.startEl("div", "style", "float:left");
		w.startEl("div", "class", "main");
		w.el("heading", getName());
		w.endEl(); // main
		w.startEl("div", "class", "sub");
		w.data(getCardStore().getComment());
		w.endEli(); // sub
		w.endEl(); // style
		w.lineEl("br", "class", "clear");
		w.endEl(); // desktopmiddle
		w.endEl(); // desktop
	}

	private ICardStore<IMagicCard> getCardStore() {
		ICardStore<IMagicCard> cardStore = DataManager.getCardStore(location);
		if (cardStore != null)
			return cardStore;
		return store.getCardStore();
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}

	@Override
	public boolean isMultipleLocationSupported() {
		return false;
	}
}
