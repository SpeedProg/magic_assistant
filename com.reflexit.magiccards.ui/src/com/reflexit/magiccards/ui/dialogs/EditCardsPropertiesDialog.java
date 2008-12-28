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

public class EditCardsPropertiesDialog extends TrayDialog {
	public static final String COMMENT_FIELD = "comment";
	public static final String OWNERSHIP_FIELD = "ownership";
	public static final String COUNT_FIELD = "count";
	public static final String NAME_FIELD = "name";
	public static final String PRICE_FIELD = "price";
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
		setComboChoices(ownership, new String[] { "Own", "Not Own" }, store.getDefaultString(OWNERSHIP_FIELD));
		ownership.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(OWNERSHIP_FIELD, ownership.getText());
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
