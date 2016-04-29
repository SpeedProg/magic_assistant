/**
 * 
 */
package com.reflexit.magiccards.ui.views.card;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.sync.GatherHelper;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.utils.StoredSelectionProvider;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;

class CardDescComposite extends Composite {
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
	private StoredSelectionProvider selectionProvider = new StoredSelectionProvider();

	public CardDescComposite(CardDescView cardDescView1, Composite parent, int style) {
		super(parent, style | SWT.INHERIT_DEFAULT);
		// UI
		this.cardDescView = cardDescView1;
		Composite panel = this;
		panel.setLayout(new GridLayout());
		this.imageControl = new Label(panel, SWT.INHERIT_DEFAULT);
		GridDataFactory.fillDefaults() //
				.grab(true, false) //
				.align(SWT.CENTER, SWT.BEGINNING)//
				.hint(width + 2, hight + 2).applyTo(this.imageControl);
		createImages();
		this.powerProvider = new PowerColumn(MagicCardField.POWER, null, null);
		this.toughProvider = new PowerColumn(MagicCardField.TOUGHNESS, null, null);
		details = new Composite(panel, SWT.INHERIT_DEFAULT);
		GridDataFactory.fillDefaults()//
				.align(SWT.FILL, SWT.FILL)//
				.grab(true, true)//
				.applyTo(details);
		details.setLayout(new StackLayout());
		this.textBackup = new Text(details, SWT.WRAP | SWT.INHERIT_DEFAULT);
		// textBackup.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		try {
			// if (true)
			// throw new SWTError();
			this.textBrowser = new Browser(details, SWT.WRAP | SWT.INHERIT_FORCE);
			textBrowser.addLocationListener(new LocationAdapter() {
				@Override
				public void changing(LocationEvent event) {
					String location = event.location;
					if (location.equals("about:blank"))
						return;
					try {
						int cardId = GatherHelper.extractCardIdFromURL(new URL(location));
						if (cardId != 0) {
							event.doit = false;
							ICardStore<IMagicCard> magicDBStore = DataManager.getCardHandler().getMagicDBStore();
							IMagicCard card2 = magicDBStore.getCard(cardId);
							if (card2 != null) {
								cardDescView.setSelection(new StructuredSelection(card2));
							}
						}
					} catch (MalformedURLException e) {
						MagicLogger.log(e);
					}
				}
			});
			swapVisibility(textBrowser, textBackup);
		} catch (Throwable e) {
			textBrowser = null;
			MagicUIActivator.log(e);
			swapVisibility(textBackup, textBrowser);
		}
	}

	private void createImages() {
		int border = 10;
		{
			Image im = ImageCreator.createTransparentImage(width - 2 * border, hight - 2 * border);
			GC gc = new GC(im);
			gc.setForeground(getForeground());
			gc.drawText("Loading...", 10, 10, true);
			gc.dispose();
			this.loadingImage = ImageCreator.drawBorder(im, border);
		}
		{
			Image im = ImageCreator.createTransparentImage(width - 2 * border, hight - 2 * border);
			GC gc = new GC(im);
			gc.setForeground(getForeground());
			gc.drawText("Can't find image", 10, 10, true);
			gc.dispose();
			this.cardNotFound = ImageCreator.drawBorder(im, border);
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
		if (imageControl.isDisposed())
			return;
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

	public void setCard(IMagicCard card) {
		this.card = card;
		if (card == IMagicCard.DEFAULT || card == null) {
			selectionProvider.setSelection(new StructuredSelection());
		} else {
			selectionProvider.setSelection(new StructuredSelection(card));
		}
	}

	@Override
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		imageControl.setMenu(menu);
		textBrowser.setMenu(menu);
		textBackup.setMenu(menu);
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
				this.textBrowser.setText(SymbolConverter.wrapHtml(links + data + text + oracle + rulings, textBrowser));
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
		if (flipId != 0) {
			if (flipId == card.getCardId()) {
				MagicLogger.log("Same flip id for " + card.getCardId());
				flipId = -flipId;
			}
			links = "<a href=\"" + GatherHelper.createImageDetailURL(flipId) + "\">Flip</a><br><br>";
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

	public IPostSelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
}