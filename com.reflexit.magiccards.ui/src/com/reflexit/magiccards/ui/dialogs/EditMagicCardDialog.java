package com.reflexit.magiccards.ui.dialogs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class EditMagicCardDialog extends MagicDialog {
	private final static String UNCHANGED = EditCardsPropertiesDialog.UNCHANGED;
	private MagicCard card;
	private Image img;
	private Button imageButton;
	private Text urlText;
	private String localPath;

	public EditMagicCardDialog(Shell parentShell, MagicCard card) {
		super(parentShell, new PreferenceStore());
		this.card = card;
		store.setDefault(MagicCardField.DBPRICE.name(), card.getDbPrice());
		store.setDefault(MagicCardField.NAME.name(), card.getName());
		store.setDefault(MagicCardField.IMAGE_URL.name(), card.getImageUrl());
		store.setDefault(MagicCardField.SET.name(), card.getSet());
	}

	@Override
	protected void createBodyArea(Composite parent) {
		getShell().setText("Edit Magic Card Properties");
		setTitle("Edit " + card.getName());
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData();
		gda.widthHint = convertWidthInCharsToPixels(100);
		area.setLayoutData(gda);
		// Header
		createTextLabel(area, "Name");
		createTextLabel(area, store.getString(MagicCardField.NAME.name()));
		// price
		createTextFieldEditor(area, "Seller's Price", MagicCardField.DBPRICE.name());
		urlText = createTextFieldEditor(area, "Image URL", MagicCardField.IMAGE_URL.name());
		createTextLabel(area, "Image");
		imageButton = createPushButton(area, "");
		imageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseImage();
			}
		});
		localPath = CardCache.createLocalImageFilePath(card);
		if (new File(localPath).exists())
			reloadImage(localPath);
	}

	private void reloadImage(String path) {
		if (img != null)
			img.dispose();
		img = ImageCreator.getInstance().createCardImage(
				path, false);
		imageButton.setImage(img);
		imageButton.getParent().layout(true);
	}

	protected void browseImage() {
		FileDialog fileDialog = new FileDialog(getShell());
		String file = fileDialog.open();
		if (file != null) {
			try {
				URL url = new File(file).toURI().toURL();
				store.setValue(MagicCardField.IMAGE_URL.name(), url.toExternalForm());
				urlText.setText(url.toExternalForm());
				new File(localPath).delete(); // delete card cached image
				File loc = new File(url.getFile());
				if (loc.exists())
					reloadImage(loc.getAbsolutePath());
			} catch (MalformedURLException e) {
				// ignore
			}
		} else {
			// cancelled, lets reload url
			if (WebUtils.isWorkOffline())
				return;
			try {
				CardCache.saveCachedFile(new File(localPath), new URL(store.getString(MagicCardField.IMAGE_URL.name())));
				reloadImage(localPath);
			} catch (IOException e) {
				MagicUIActivator.log(e);
			}
		}
	}

	@Override
	protected void okPressed() {
		editCard(this.card, store, true);
		super.okPressed();
	}

	@Override
	public boolean close() {
		if (img != null)
			img.dispose();
		return super.close();
	}

	private void editCard(MagicCard card, PreferenceStore store, boolean update) {
		boolean modified = false;
		modified = setField(card, store, MagicCardField.DBPRICE) || modified;
		modified = setField(card, store, MagicCardField.IMAGE_URL) || modified;
		if (modified && update) {
			DataManager.update(card);
		}
	}

	protected boolean setField(MagicCard card, PreferenceStore store, ICardField field) {
		Boolean modified = false;
		String orig = String.valueOf(card.get(field));
		String edited = store.getString(field.name());
		if (!UNCHANGED.equals(edited) && !edited.equals(orig)) {
			try {
				card.set(field, edited);
				modified = true;
			} catch (Exception e) {
				// was bad value
				MessageDialog.openError(getShell(), "Error", "Invalid value for " + field + ": " + edited);
			}
		}
		return modified;
	}
}
