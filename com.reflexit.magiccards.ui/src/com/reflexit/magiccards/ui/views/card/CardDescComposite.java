/**
 * 
 */
package com.reflexit.magiccards.ui.views.card;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;

class CardDescComposite extends Composite {
	private static final String CARD_URI = "card://";
	private static final String MULTIVERSEID = "multiverseid=";
	private static final String OTHER_PART = "opart=";
	/**
	 * 
	 */
	private Image image;
	private Label imageControl;
	private final CardDescView cardDescView;
	private Browser textBrowser;
	private Text textBackup;
	private Composite details;
	private IMagicCard card;
	private Image loadingImage;
	private Image cardNotFound;
	private PowerColumn powerProvider;
	private PowerColumn toughProvider;
	int width = 223, hight = 310;

	public CardDescComposite(CardDescView cardDescView1, Composite parent, int style) {
		super(parent, style);
		// UI
		this.cardDescView = cardDescView1;
		Composite panel = this;
		panel.setFont(parent.getFont());
		panel.setLayout(new GridLayout());
		panel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		this.imageControl = new Label(panel, SWT.NONE);
		GridDataFactory.fillDefaults() //
				.grab(true, false) //
				.align(SWT.BEGINNING, SWT.BEGINNING)//
				.hint(width, hight).applyTo(this.imageControl);
		createImages();
		this.powerProvider = new PowerColumn(MagicCardField.POWER, null, null);
		this.toughProvider = new PowerColumn(MagicCardField.TOUGHNESS, null, null);
		details = new Composite(panel, SWT.INHERIT_DEFAULT);
		details.setBackground(panel.getBackground());
		GridDataFactory.fillDefaults()//
				.align(SWT.FILL, SWT.FILL)//
				.grab(true, true)//
				.applyTo(details);
		details.setLayout(new StackLayout());
		this.textBackup = new Text(details, SWT.WRAP);
		this.textBackup.setFont(panel.getFont());
		try {
			// if (true)
			// throw new SWTError();
			this.textBrowser = new Browser(details, SWT.WRAP | SWT.INHERIT_DEFAULT);
			this.textBrowser.setFont(panel.getFont());
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
						String part = null;
						int cardId = 0;
						for (int i = 0; i < params.length; i++) {
							String string = params[i];
							if (string.startsWith(MULTIVERSEID)) {
								event.doit = false;
								String value = string.substring(MULTIVERSEID.length());
								cardId = Integer.valueOf(value).intValue();
							}
							if (string.startsWith(OTHER_PART)) {
								event.doit = false;
								part = string.substring(OTHER_PART.length());
							}
						}
						if (cardId == 0)
							cardId = card.getCardId();
						String opart = (String) card.get(MagicCardField.PART);
						ICardStore magicDBStore = DataManager.getCardHandler().getMagicDBStore();
						Collection<IMagicCard> cards = magicDBStore.getCards(cardId);
						IMagicCard card2 = null;
						if (part != null) {
							for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
								IMagicCard card3 = (IMagicCard) iterator.next();
								if (part.equals(((MagicCard) card3).getPart())) {
									card2 = card3;
									break;
								}
							}
							if (card2 == null) {
								// card is not in DB
								card2 = card.cloneCard();
								((ICardModifiable) card2).set(MagicCardField.PART, part);
								((ICardModifiable) card2).set(MagicCardField.OTHER_PART, opart);
								if (!part.contains("@")) {
									((ICardModifiable) card2).set(MagicCardField.NAME,
											card2.getName().replaceAll("\\Q(" + opart + ")", "(" + part + ")"));
								}
							}
						} else {
							if (cards.size() > 0)
								card2 = cards.iterator().next();
						}
						cardDescView.setSelection(new StructuredSelection(card2));
					}
				}
			});
			swapVisibility(textBrowser, textBackup);
		} catch (Throwable e) {
			MagicUIActivator.log(e);
			swapVisibility(textBackup, textBrowser);
		}
	}

	private void createImages() {
		int border = 10;
		{
			Image im = new Image(getDisplay(), width - 2 * border, hight - 2 * border);
			GC gc = new GC(im);
			gc.drawText("Loading...", 10, 10);
			gc.dispose();
			this.loadingImage = drawBorder(im, border);
		}
		{
			Image im = new Image(getDisplay(), width - 2 * border, hight - 2 * border);
			GC gc = new GC(im);
			gc.drawText("Can't find image", 10, 10);
			gc.dispose();
			this.cardNotFound = drawBorder(im, border);
		}
	}

	public void setImage(IMagicCard card, Image remoteImage) {
		if (card == this.card) {
			setImage(remoteImage);
		}
	}

	public void setImageNotFound(IMagicCard card, Throwable e) {
		if (card == this.card) {
			this.image = ImageCreator.getInstance().createCardNotFoundImage(card);
			this.imageControl.setImage(this.image);
		}
	}

	private void setImage(Image remoteImage) {
		if (this.image != null && this.image != this.loadingImage && this.image != this.cardNotFound) {
			this.image.dispose();
			this.image = null;
		}
		if (remoteImage == null)
			return;
		this.image = remoteImage;
		this.imageControl.setImage(this.image);
		GridData ld = (GridData) this.imageControl.getLayoutData();
		ld.minimumWidth = this.image.getBounds().width + 1;
		ld.minimumHeight = this.image.getBounds().height + 1;
		ld.widthHint = ld.minimumWidth;
		ld.heightHint = ld.minimumHeight;
		this.layout(true, true);
	}

	private boolean logOnce = false;

	void reload(IMagicCard card) {
		setCard(card);
		setLoadingImage(card);
		setText(card);
	}

	public void setCard(IMagicCard card) {
		this.card = card;
	}

	protected void setLoadingImage(IMagicCard card) {
		if (card == IMagicCard.DEFAULT) {
			return;
		}
		try {
			setImage(this.loadingImage);
		} catch (RuntimeException e) {
			MagicUIActivator.log(e);
		}
	}

	public void setText(IMagicCard card) {
		if (card == IMagicCard.DEFAULT) {
			return;
		}
		try {
			if (textBrowser != null) {
				String data = getCardDataHtml(card);
				String text = getText(card);
				String links = getLinks(card);
				String oracle = getOracle(card, text);
				String rulings = getCardRulingsHtml(card);
				this.textBrowser.setText(SymbolConverter.wrapHtml(links + data + text + oracle + rulings, this));
				swapVisibility(textBrowser, textBackup);
				return;
			}
		} catch (Exception e) {
			if (logOnce == false) {
				MagicUIActivator.log(e);
				logOnce = true;
			}
		}
		String data = getCardDataText(card);
		String text = getText(card);
		text = text.replaceAll("<br>", "\n");
		this.textBackup.setText(data + text);
		swapVisibility(textBackup, textBrowser);
	}

	protected String getText(IMagicCard card) {
		String text = card.getText();
		if (text == null || text.length() == 0)
			text = card.getOracleText();
		if (text == null || text.length() == 0)
			text = "";
		return text;
	}

	protected String getOracle(IMagicCard card, String text) {
		String oracle = card.getOracleText();
		if (text != null && text.length() != 0 && !text.equals(oracle)) {
			oracle = "<br><br>Oracle:<br>" + oracle;
		} else {
			oracle = "";
		}
		return oracle;
	}

	protected String getLinks(IMagicCard card) {
		String links = "";
		int flipId = card.getFlipId();
		String part = (String) card.get(MagicCardField.OTHER_PART);
		if (part != null && !part.contains("@")) {
			links += "<a href=\"" + CARD_URI + OTHER_PART + part + ((flipId != 0) ? "&" + MULTIVERSEID + flipId : "")
					+ "\">Other split part</a><br><br>";
		} else if (flipId != 0) {
			if (flipId == card.getCardId() && part != null && part.contains("@")) {
				links = "<a href=\"" + CARD_URI + MULTIVERSEID + flipId + "&" + OTHER_PART + part + "\">Flip</a><br><br>";
			} else
				links = "<a href=\"" + CARD_URI + MULTIVERSEID + flipId + "\">Flip</a><br><br>";
		}
		return links;
	}

	private void swapVisibility(Control con1, Control con2) {
		((StackLayout) details.getLayout()).topControl = con1;
		details.layout(true, true);
		redraw();
	}

	private String getCardDataText(IMagicCard card) {
		String pt = "";
		if (card.getToughness() != null && card.getToughness().length() > 0) {
			pt = powerProvider.getText(card) + "/" + toughProvider.getText(card);
		}
		String data = card.getName() + "\n" + getType(card);
		if (pt.length() > 0) {
			data += "\n" + pt;
		} else {
			data += "\n";
		}
		String num = getCollectorNumber(card);
		data += "\n" + card.getSet() + " (" + getRarity(card) + ") " + num + "\n";
		return data;
	}

	public String getRarity(IMagicCard card) {
		return card.getRarity() == null ? "Unknown Rarity" : card.getRarity();
	}

	public String getType(IMagicCard card) {
		return card.getType() == null ? "Unknown Type" : card.getType();
	}

	private String getCardDataHtml(IMagicCard card) {
		String text = getCardDataText(card);
		text = text.replaceAll("\n", "<br/>\n");
		return text + "<p/>";
	}

	public String getCollectorNumber(IMagicCard card) {
		String num = (String) card.get(MagicCardField.COLLNUM);
		if (num != null)
			num = "[" + num + "]";
		return num;
	}

	private String getCardRulingsHtml(IMagicCard card) {
		String srulings = card.getRulings();
		if (srulings == null || srulings.length() == 0) {
			return "";
		}
		String data = "<p>Rulings:<ul>";
		String rulings[] = srulings.split("\\n");
		for (String ruling : rulings) {
			data += "<li>" + ruling + "</li>";
		}
		data += "</ul><p/>";
		return data;
	}

	private Image drawBorder(Image remoteImage, int border) {
		Rectangle bounds = remoteImage.getBounds();
		Image full = new Image(getDisplay(), bounds.width + border * 2, bounds.height + border * 2);
		GC gc = new GC(full);
		gc.setBackground(getBackground());
		gc.fillRectangle(0, 0, bounds.width + border * 2, bounds.height + border * 2);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.fillRoundRectangle(0, 0, bounds.width + border * 2, bounds.height + border * 2, border * 2, border * 2);
		gc.drawImage(remoteImage, border, border);
		gc.dispose();
		return full;
	}

	@Override
	public void dispose() {
		if (this.image != null) {
			this.image.dispose();
		}
		this.image = null;
		super.dispose();
	}

	public IMagicCard getCard() {
		return this.card;
	}
}