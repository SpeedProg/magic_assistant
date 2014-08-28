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
	private Composite area;
	private Text priceText;

	public EditMagicCardDialog(Shell parentShell, MagicCard card) {
		super(parentShell, new PreferenceStore());
		this.card = card;
		ICardField[] allFields = MagicCardField.allFields();
		for (ICardField field : allFields) {
			String value = card.getString(field);
			if (value != null)
				store.setDefault(field.name(), value);
			else
				store.setDefault(field.name(), "");
		}
	}

	@Override
	protected void createBodyArea(Composite parent) {
		getShell().setText("Edit Magic Card Properties");
		setTitle("Edit " + card.getName());
		Composite back = new Composite(parent, SWT.NONE);
		back.setLayout(new GridLayout(2, false));
		imageButton = createPushButton(back, "");
		imageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseImage();
			}
		});
		GridData gda1 = new GridData(GridData.GRAB_VERTICAL);
		gda1.widthHint = 223;
		gda1.heightHint = 310;
		imageButton.setLayoutData(gda1);
		area = new Composite(back, SWT.NONE);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData(GridData.FILL_BOTH);
		area.setLayoutData(gda);
		createReadOnlyField("Name", MagicCardField.NAME);
		priceText = createEditableField("Seller's Price", MagicCardField.DBPRICE);
		urlText = createEditableField("Image URL", MagicCardField.IMAGE_URL);
		// set defaults
		localPath = CardCache.createLocalImageFilePath(card);
		if (new File(localPath).exists())
			reloadImage(localPath);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 2, "Reload Image", false);
		createButton(parent, 3, "Restore Defaults", false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 2) {// reload
			reloadImageFromUrl();
		} else if (buttonId == 3) { // restore default
			store.setValue(MagicCardField.DBPRICE.name(), 0);
			store.setValue(MagicCardField.IMAGE_URL.name(), card.getDefaultImageUrl());
			priceText.setText(store.getString(MagicCardField.DBPRICE.name()));
			urlText.setText(store.getString(MagicCardField.IMAGE_URL.name()));
			reloadImageFromUrl();
		} else
			super.buttonPressed(buttonId);
	}

	private Text createEditableField(String label, MagicCardField field) {
		return createTextFieldEditor(area, label, field.name());
	}

	private Text createReadOnlyField(String label, MagicCardField field) {
		Text text = createTextFieldEditor(area, label, field.name());
		text.setEditable(false);
		text.setToolTipText("Not editable");
		return text;
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
			reloadImageFromUrl();
		}
	}

	private void reloadImageFromUrl() {
		if (WebUtils.isWorkOffline())
			return;
		try {
			CardCache.saveCachedFile(new File(localPath), new URL(store.getString(MagicCardField.IMAGE_URL.name())));
			reloadImage(localPath);
		} catch (IOException e) {
			MagicUIActivator.log(e);
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
