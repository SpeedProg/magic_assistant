package com.reflexit.magiccards.ui.views.analyzers;

import java.io.ByteArrayOutputStream;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.EventManager;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.core.xml.MyXMLStreamWriter;
import com.reflexit.magiccards.core.xml.XMLStreamException;

public class WizardsHtmlPage extends AbstractDeckPage {
	private static final String CARD_URI = "card://";
	private static final String CARDID = "cardId=";
	private Browser textBrowser;
	private IStructuredSelection selection;

	class MySelectionProvider extends EventManager implements ISelectionProvider {
		@Override
		public void setSelection(ISelection sel) {
			selection = (IStructuredSelection) sel;
			selectionChanged(new SelectionChangedEvent(this, selection));
		}

		private void selectionChanged(final SelectionChangedEvent event) {
			Object[] listeners = getListeners();
			for (Object listener : listeners) {
				ISelectionChangedListener lis = (ISelectionChangedListener) listener;
				try {
					lis.selectionChanged(event);
				} catch (Throwable t) {
					MagicLogger.log(t);
				}
			}
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			removeListenerObject(listener);
		}

		@Override
		public ISelection getSelection() {
			if (selection == null)
				return new StructuredSelection();
			return selection;
		}

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			addListenerObject(listener);
		}
	}

	private ISelectionProvider selProvider = new MySelectionProvider();

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		Composite area = getArea();
		this.textBrowser = new Browser(area, SWT.WRAP | SWT.INHERIT_DEFAULT);
		this.textBrowser.setFont(area.getFont());
		textBrowser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				String location = event.location;
				if (location.startsWith(CARD_URI)) {
					location = location.substring(CARD_URI.length());
					if (location.endsWith("/")) {
						location = location.substring(0, location.length() - 1);
					}
					String params[] = location.split("&");
					for (int i = 0; i < params.length; i++) {
						String string = params[i];
						if (string.startsWith(CARDID)) {
							event.doit = false;
							String value = string.substring(CARDID.length());
							int cardId = Integer.valueOf(value).intValue();
							IDbCardStore magicDBStore = DataManager.getCardHandler().getMagicDBStore();
							IMagicCard card = (IMagicCard) magicDBStore.getCard(cardId);
							if (card != null)
								selProvider.setSelection(new StructuredSelection(card));
						}
					}
					event.doit = false;
				}
			}
		});
		textBrowser.setLayoutData(new GridData(GridData.FILL_BOTH));
		return getArea();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return selProvider;
	}

	@Override
	public void activate() {
		super.activate();
		if (view == null)
			return;
		CardCollection deck = view.getCardCollection();
		store = getMainStore(deck);
		String text = getHtml();
		// System.err.println(text);
		this.textBrowser.setText(text);
	}

	private String getHtml() {
		try {
			ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
			MyXMLStreamWriter writer = new MyXMLStreamWriter(byteSt);
			writer.startEl("html");
			writer.startEl("body");
			header(writer);
			maindeck(writer);
			footer(writer);
			writer.endEl();
			writer.endEl();
			writer.close();
			return byteSt.toString();
		} catch (XMLStreamException e) {
			return "Error: " + e.getMessage();
		}
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
		w.endEl(); // style
		w.endEl(); // maindeskmiddle
		w.endEl(); // maindesk
	}

	public void maindeckCards(MyXMLStreamWriter w) throws XMLStreamException {
		if (view == null)
			return;
		CardCollection deck = view.getCardCollection();
		CardGroup group = CardStoreUtils.buildTypeGroups(store);
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
		ICardStore<IMagicCard> sbStore = getSideboardStore(deck);
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
	}

	private ICardStore<IMagicCard> getMainStore(CardCollection deck) {
		Location location = deck.getLocation().toMainDeck();
		CollectionsContainer parent = (CollectionsContainer) deck.getParent();
		CardCollection s = (CardCollection) parent.findChield(location);
		if (s != null) {
			s.open();
			return s.getStore();
		}
		return null;
	}

	private ICardStore<IMagicCard> getSideboardStore(CardCollection deck) {
		Location sideboardLoc = deck.getLocation().toSideboard();
		CollectionsContainer parent = (CollectionsContainer) deck.getParent();
		CardCollection s = (CardCollection) parent.findChield(sideboardLoc);
		if (s != null) {
			s.open();
			return s.getStore();
		}
		return null;
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
		w.el("heading", getCardStore().getName());
		w.endEl(); // main
		w.startEl("div", "class", "sub");
		w.data(getCardStore().getComment());
		w.endEli(); // sub
		w.endEl(); // style
		w.lineEl("br", "class", "clear");
		w.endEl(); // desktopmiddle
		w.endEl(); // desktop
	}
}
