/**
 * 
 */
package com.reflexit.magiccards.ui.views.card;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;

class CardDescComposite extends Composite {
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

	public CardDescComposite(CardDescView cardDescView, Composite parent, int style) {
		super(parent, style);
		// UI
		this.cardDescView = cardDescView;
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
			this.textBrowser = new Browser(details, SWT.WRAP | SWT.INHERIT_DEFAULT);
			this.textBrowser.setFont(panel.getFont());
			swapVisibility(textBrowser, textBackup);
		} catch (Exception e) {
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
			String data = getCardDataHtml(card);
			String text = card.getText();
			String rulings = getCardRulingsHtml(card);
			this.textBrowser.setText(SymbolConverter.wrapHtml(data + text + rulings, this));
			swapVisibility(textBrowser, textBackup);
		} catch (Exception e) {
			if (logOnce == false) {
				MagicUIActivator.log(e);
				logOnce = true;
			}
			String data = getCardDataText(card);
			String text = card.getText();
			text = text.replaceAll("<br>", "\n");
			this.textBackup.setText(data + text);
			swapVisibility(textBackup, textBrowser);
		}
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
		String data = card.getName() + "\n" + card.getType();
		if (pt.length() > 0) {
			data += "\n" + pt;
		} else {
			data += "\n";
		}
		data += "\n" + card.getSet() + " (" + card.getRarity() + ") " + "\n";
		return data;
	}

	private String getCardDataHtml(IMagicCard card) {
		String pt = "";
		if (card.getToughness() != null && card.getToughness().length() > 0) {
			pt = powerProvider.getText(card) + "/" + toughProvider.getText(card);
		}
		String data = card.getName() + "<br/>" + card.getType();
		if (pt.length() > 0) {
			data += "<br/>" + pt;
		} else {
			data += "<br/>";
		}
		data += "<br/>" + card.getSet() + " (" + card.getRarity() + ") " + "<p/>";
		return data;
	}

	private String getCardRulingsHtml(IMagicCard card) {
		if (card.getRulings() == null || card.getRulings().length() == 0) {
			return "";
		}
		String data = "<p>Rulings:<ul>";
		String rulings[] = card.getRulings().split("\\n");
		for (String ruling : rulings) {
			data += "<li>" + ruling + "</li>";
		}
		data += "</ul></p>";
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