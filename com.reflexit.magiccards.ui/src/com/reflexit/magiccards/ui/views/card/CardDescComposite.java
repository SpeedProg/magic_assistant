/**
 * 
 */
package com.reflexit.magiccards.ui.views.card;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;

class CardDescComposite extends Composite {
	/**
	 * 
	 */
	private Image image;
	private Label imageControl;
	private final CardDescView cardDescView;
	private Browser textValue;
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
		panel.setLayout(new GridLayout(2, false));
		panel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		this.imageControl = new Label(panel, SWT.NONE);
		GridDataFactory.fillDefaults() //
		        .grab(true, false) //
		        .align(SWT.CENTER, SWT.BEGINNING)//
		        .span(2, 0) //
		        .hint(width, hight).applyTo(this.imageControl);
		this.textValue = new Browser(panel, SWT.WRAP | SWT.INHERIT_DEFAULT);
		this.textValue.setFont(panel.getFont());
		this.textValue.setBackground(panel.getBackground());
		GridDataFactory.fillDefaults()//
		        .align(SWT.FILL, SWT.FILL)//
		        .grab(true, true)//
		        .span(2, 0) //
		        .applyTo(this.textValue);
		createImages();
		this.powerProvider = new PowerColumn(MagicCardField.POWER, null, null);
		this.toughProvider = new PowerColumn(MagicCardField.TOUGHNESS, null, null);
		this.reload(IMagicCard.DEFAULT);
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
			this.image = this.cardNotFound;
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

	void reload(IMagicCard card) {
		try {
			this.card = card;
			if (card == IMagicCard.DEFAULT) {
				return;
			}
			String data = getCardDataHtml(card);
			String text = card.getOracleText();
			this.textValue.setText(SymbolConverter.wrapHtml(data + text, this));
			setImage(this.loadingImage);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		//		String img = "";
		//		try {
		//			String edition = card.getSet();
		//			String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		//			URL url = CardCache.createSetImageRemoteURL(editionAbbr, card.getRarity());
		//			img = url == null ? "" : "<img align=middle alt=\"\" height=12 src=\"" + url.toExternalForm() + "\"/>";
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		data += "<br/>" + card.getSet() + " (" + card.getRarity() + ") " + "<p/>";
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