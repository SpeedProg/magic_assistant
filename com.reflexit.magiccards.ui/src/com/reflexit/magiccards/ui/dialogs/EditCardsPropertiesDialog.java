package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;

public class EditCardsPropertiesDialog extends TrayDialog {
	private static final String VIRTUAL_VALUE = "Virtual";
	private static final String OWN_VALUE = "Own";
	public static final String COMMENT_FIELD = MagicCardFieldPhysical.COMMENT.name();
	public static final String SPECIAL_FIELD = MagicCardFieldPhysical.SPECIAL.name();
	public static final String OWNERSHIP_FIELD = MagicCardFieldPhysical.OWNERSHIP.name();
	public static final String COUNT_FIELD = MagicCardFieldPhysical.COUNT.name();
	public static final String NAME_FIELD = MagicCardField.NAME.name();
	public static final String PRICE_FIELD = MagicCardFieldPhysical.PRICE.name();
	public static final String UNCHANGED = "<unchanged>";;
	private PreferenceStore store;

	public EditCardsPropertiesDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.store = store;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Edit Card Properties");
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData();
		gda.widthHint = convertWidthInCharsToPixels(60);
		area.setLayoutData(gda);
		// Header
		createTextLabel(area, "Name");
		createTextLabel(area, store.getString(NAME_FIELD));
		// Count
		createTextLabel(area, "Count");
		final Text count = new Text(area, SWT.BORDER);
		count.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		count.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				count.setFocus();
				store.setValue(COUNT_FIELD, count.getText());
			}
		});
		count.setText(store.getString(COUNT_FIELD));
		// Count
		createTextLabel(area, "Price");
		final Text price = new Text(area, SWT.BORDER);
		price.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		price.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				price.setFocus();
				store.setValue(PRICE_FIELD, price.getText());
			}
		});
		price.setText(store.getString(PRICE_FIELD));
		// ownership
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
		// comment
		createTextLabel(area, "Comment");
		final Text comm = new Text(area, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = convertHeightInCharsToPixels(4);
		comm.setLayoutData(gd);
		comm.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				comm.setFocus();
				store.setValue(COMMENT_FIELD, comm.getText());
			}
		});
		comm.setText(store.getString(COMMENT_FIELD));
		// special
		createTextLabel(area, "Special Tags");
		final Text special = new Text(area, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		special.setToolTipText("Set card tags, such as foil, mint, premium, etc. Tags are separated by ','.\n To add tag use +, to remove tag use -. For example \"+foil,-online\".");
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.heightHint = convertHeightInCharsToPixels(4);
		special.setLayoutData(gd);
		special.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				special.setFocus();
				store.setValue(SPECIAL_FIELD, special.getText());
			}
		});
		special.setText(store.getString(SPECIAL_FIELD));
		// end
		count.setFocus();
		return area;
	}

	private void setComboChoices(Combo ownership, String[] strings, String defaultString) {
		boolean hasDefault = false;
		for (String string : strings) {
			ownership.add(string);
			if (string.equals(defaultString)) {
				hasDefault = true;
			}
		}
		if (!hasDefault) {
			ownership.add(defaultString);
		}
		ownership.setText(defaultString);
	}

	private Label createTextLabel(Composite area, String string) {
		Label label = new Label(area, SWT.NONE);
		label.setText(string);
		GridData ld = new GridData();
		ld.verticalAlignment = SWT.TOP;
		label.setLayoutData(ld);
		return label;
	}
}
