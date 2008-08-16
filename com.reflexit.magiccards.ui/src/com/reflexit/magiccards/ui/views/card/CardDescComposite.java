/**
 * 
 */
package com.reflexit.magiccards.ui.views.card;

import org.eclipse.jface.dialogs.MessageDialog;
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
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.MagicCardLabelProvider;

class CardDescComposite extends Composite {
	/**
	 * 
	 */
	private Image image;
	private Label imageControl;
	private final CardDescView cardDescView;
	private Label nameValue;
	private Label typeValue;
	private Label powerValue;
	private Browser textValue;
	private Label rarityValue;
	private Label setValue;
	private MagicCardLabelProvider provider;
	private IMagicCard card;
	private Image loadingImage;
	private Image cardNotFound;

	public CardDescComposite(CardDescView cardDescView, Composite parent, int style) {
		super(parent, style);
		this.provider = new MagicCardLabelProvider();
		// UI
		this.cardDescView = cardDescView;
		Composite panel = this;
		panel.setFont(parent.getFont());
		panel.setLayout(new GridLayout(2, false));
		this.imageControl = new Label(panel, SWT.NONE);
		GridDataFactory.fillDefaults() //
		        .grab(true, false) //
		        .align(SWT.CENTER, SWT.BEGINNING)//
		        .span(2, 0) //
		        .hint(200, 285).applyTo(this.imageControl);
		GridDataFactory labelLayout = GridDataFactory.fillDefaults() //
		        .align(SWT.BEGINNING, SWT.CENTER);
		GridDataFactory valueLayout = GridDataFactory.fillDefaults() //
		        .align(SWT.FILL, SWT.CENTER)//
		        .grab(true, false);
		this.nameValue = createLabeledField("Name", panel, labelLayout, valueLayout);
		this.typeValue = createLabeledField("Type", panel, labelLayout, valueLayout);
		this.setValue = createLabeledField("Set", panel, labelLayout, valueLayout);
		this.rarityValue = createLabeledField("Rarity", panel, labelLayout, valueLayout);
		this.powerValue = createLabeledField("P/T", panel, labelLayout, valueLayout);
		this.textValue = new Browser(panel, SWT.WRAP | SWT.INHERIT_DEFAULT);
		this.textValue.setFont(panel.getFont());
		this.textValue.setBackground(panel.getBackground());
		GridDataFactory.fillDefaults()//
		        .align(SWT.FILL, SWT.FILL)//
		        .grab(true, true)//
		        .span(2, 0) //
		        .applyTo(this.textValue);
		createImages();
		this.reload(IMagicCard.DEFAULT);
	}

	private void createImages() {
		{
			Image im = new Image(getDisplay(), 200, 285);
			GC gc = new GC(im);
			gc.drawText("Loading...", 10, 10);
			gc.dispose();
			this.loadingImage = drawBorder(im);
		}
		{
			Image im = new Image(getDisplay(), 200, 285);
			GC gc = new GC(im);
			gc.drawText("Can't find image", 10, 10);
			gc.dispose();
			this.cardNotFound = drawBorder(im);
		}
	}

	private Label createLabeledField(String title, Composite panel, GridDataFactory right, GridDataFactory left) {
		Label name = new Label(panel, SWT.NONE);
		right.applyTo(name);
		name.setText(title + ":");
		Label nameValue = new Label(panel, SWT.NONE);
		left.applyTo(nameValue);
		return nameValue;
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
			MessageDialog.openError(getShell(), "Error:", "Can't load image: " + e.getMessage());
		}
	}

	private void setImage(Image remoteImage) {
		if (this.image != null && this.image != this.loadingImage && this.image != this.cardNotFound) {
			this.image.dispose();
			this.image = null;
		}
		{
			Image full = drawBorder(remoteImage);
			this.image = full;
		}
		this.imageControl.setImage(this.image);
	}

	void reload(IMagicCard card) {
		try {
			this.card = card;
			if (card == IMagicCard.DEFAULT) {
				return;
			}
			this.image = this.loadingImage;
			this.imageControl.setImage(this.image);
			GridData ld = (GridData) this.imageControl.getLayoutData();
			ld.minimumWidth = this.image.getBounds().width + 1;
			ld.minimumHeight = this.image.getBounds().height + 1;
			ld.widthHint = ld.minimumWidth;
			ld.heightHint = ld.minimumHeight;
			this.nameValue.setText(card.getName());
			this.typeValue.setText(card.getType());
			String pt = "";
			if (card.getToughness().length() > 0) {
				pt = this.provider.getColumnText(card, 4) + "/" + this.provider.getColumnText(card, 5);
			}
			this.powerValue.setText(pt);
			this.setValue.setText(card.getEdition());
			this.rarityValue.setText(card.getRarity());
			String text = card.getOracleText();
			this.textValue.setText(SymbolConverter.wrapHtml(text, this));
			this.layout(true, true);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Image drawBorder(Image remoteImage) {
		Rectangle bounds = remoteImage.getBounds();
		int border = 10;
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