package com.reflexit.magiccards.ui.dialogs;

import java.io.File;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.SpecialTags;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.widgets.ContextAssist;

public class EditCardsPropertiesDialog extends MagicDialog {
	private static final String VIRTUAL_VALUE = "Virtual";
	private static final String OWN_VALUE = "Own";
	public static final String COMMENT_FIELD = MagicCardField.COMMENT.name();
	public static final String SPECIAL_FIELD = MagicCardField.SPECIAL.name();
	public static final String OWNERSHIP_FIELD = MagicCardField.OWNERSHIP.name();
	public static final String COUNT_FIELD = MagicCardField.COUNT.name();
	public static final String NAME_FIELD = MagicCardField.NAME.name();
	public static final String PRICE_FIELD = MagicCardField.PRICE.name();
	public static final String UNCHANGED = "<unchanged>";
	protected Composite area;

	public EditCardsPropertiesDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell, store);
	}

	@Override
	protected void createBodyArea(Composite parent) {
		getShell().setText("Edit Card Properties");
		setTitle("Edit Magic Card Instance '" + store.getString(NAME_FIELD) + "'");
		Composite back = new Composite(parent, SWT.NONE);
		back.setLayout(new GridLayout(2, false));
		back.setLayoutData(new GridData(GridData.FILL_BOTH));
		createImageControl(back);
		area = new Composite(back, SWT.NONE);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData(GridData.FILL_BOTH);
		gda.widthHint = convertWidthInCharsToPixels(80);
		area.setLayoutData(gda);
		// Header
		createTextLabel(area, "Name");
		createTextLabel(area, store.getString(NAME_FIELD));
		// Count
		Text count = createTextFieldEditor(area, "Count", COUNT_FIELD);
		// Price
		createTextFieldEditor(area, "User Price", PRICE_FIELD);
		// ownership
		createOwnershipFieldEditor(area);
		// comment
		createTextFieldEditor(area, "Comment", COMMENT_FIELD, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		// special
		Text special = createTextFieldEditor(area, "Special Tags", SPECIAL_FIELD, SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		special.setToolTipText("Set card tags, such as foil, mint, premium, forTrade, etc. Tags are separated by ','.\n To add tag use +, to remove tag use -. For example \"+foil,-online\".");
		ContextAssist.addContextAssist(special, SpecialTags.getTags(), true);
		// end
		count.setFocus();
	}

	private void createImageControl(Composite parent) {
		Label imageControl = new Label(parent, SWT.NONE);
		GridData gda1 = new GridData(GridData.FILL_VERTICAL);
		gda1.widthHint = 223;
		gda1.heightHint = 310;
		imageControl.setLayoutData(gda1);
		String localPath = CardCache.createLocalImageFilePath(store.getInt(MagicCardField.ID.name()),
				store.getString(MagicCardField.EDITION_ABBR.name()));
		if (new File(localPath).exists()) {
			Image img = ImageCreator.getInstance().createCardImage(
					localPath, false);
			imageControl.setImage(img);
		}
	}

	public void createOwnershipFieldEditor(Composite area) {
		createTextLabel(area, "Ownership");
		final Combo ownership = new Combo(area, SWT.READ_ONLY);
		ownership.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String ovalue = store.getDefaultString(OWNERSHIP_FIELD);
		String defaultString = ovalue;
		if (!UNCHANGED.equals(ovalue))
			defaultString = Boolean.valueOf(ovalue) ? OWN_VALUE : VIRTUAL_VALUE;
		setComboChoices(ownership, new String[] { OWN_VALUE, VIRTUAL_VALUE, UNCHANGED }, defaultString);
		ownership.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean own = ownership.getText().equals(OWN_VALUE);
				store.setValue(OWNERSHIP_FIELD, String.valueOf(own));
			}
		});
	}
}
