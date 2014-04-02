package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.SpecialTags;
import com.reflexit.magiccards.ui.widgets.ContextAssist;

public class EditCardsPropertiesDialog extends MagicDialog {
	private static final String VIRTUAL_VALUE = "Virtual";
	private static final String OWN_VALUE = "Own";
	public static final String COMMENT_FIELD = MagicCardField.COMMENT.name();
	public static final String SPECIAL_FIELD = MagicCardField.SPECIAL.name();
	public static final String OWNERSHIP_FIELD = MagicCardField.OWNERSHIP.name();
	public static final String COUNT_FIELD = MagicCardField.COUNT.name();
	public static final String COUNT_FOR_TRADE = MagicCardField.FORTRADECOUNT.name();
	public static final String NAME_FIELD = MagicCardField.NAME.name();
	public static final String PRICE_FIELD = MagicCardField.PRICE.name();
	public static final String UNCHANGED = "<unchanged>";

	public EditCardsPropertiesDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell, store);
	}

	@Override
	protected void createBodyArea(Composite parent) {
		getShell().setText("Edit Card Properties");
		setTitle("Edit Card Properties");
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData();
		gda.widthHint = convertWidthInCharsToPixels(60);
		area.setLayoutData(gda);
		// Header
		createTextLabel(area, "Name");
		createTextLabel(area, store.getString(NAME_FIELD));
		// Count
		Text count = createTextFieldEditor(area, "Count", COUNT_FIELD);
		// count for trade
		createTextFieldEditor(area, "Count For Trade", COUNT_FOR_TRADE);
		// Price
		createTextFieldEditor(area, "Price", PRICE_FIELD);
		// ownership
		createOwnershipFieldEditor(area);
		// comment
		createTextFieldEditor(area, "Comment", COMMENT_FIELD, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		// special
		Text special = createTextFieldEditor(area, "Special Tags", SPECIAL_FIELD, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		special.setToolTipText("Set card tags, such as foil, mint, premium, etc. Tags are separated by ','.\n To add tag use +, to remove tag use -. For example \"+foil,-online\".");
		ContextAssist.addContextAssist(special, SpecialTags.getTags(), true);
		// end
		count.setFocus();
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
