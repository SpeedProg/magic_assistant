package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class MagicDialog extends TitleAreaDialog {
	protected PreferenceStore store;

	public MagicDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.store = store;
	}

	protected void validate() {
		// override to validate store fields
	}

	public Text createTextFieldEditor(Composite area, String labelString, final String property) {
		return createTextFieldEditor(area, labelString, property, SWT.BORDER);
	}

	public Text createTextFieldEditor(Composite area, String labelString, final String property,
			String tooltip) {
		Text text = createTextFieldEditor(area, labelString, property, SWT.BORDER);
		text.setToolTipText(tooltip);
		return text;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		createBodyArea(area);
		this.store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		});
		return area;
	}

	protected void createBodyArea(Composite composite) {
		// to override
	}

	public Text createTextFieldEditor(Composite area, String labelString, final String property, int flags) {
		if (labelString != null)
			createTextLabel(area, labelString);
		final Text text = new Text(area, flags);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		if ((flags & SWT.WRAP) != 0) {
			gd.heightHint = convertHeightInCharsToPixels(4);
		}
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				store.setValue(property, text.getText());
			}
		});
		text.setText(store.getString(property));
		return text;
	}

	public Combo createComboFieldEditor(Composite area, String labelString, final String property,
			String[] values) {
		createTextLabel(area, labelString);
		final Combo combo = new Combo(area, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setComboChoices(combo, values, store.getString(property));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(property, combo.getText());
				store.setValue(property + ".num", combo.getSelectionIndex());
			}
		});
		return combo;
	}

	protected void setComboChoices(Combo combo, String[] strings, String defaultString) {
		boolean hasDefault = false;
		for (String string : strings) {
			combo.add(string);
			if (string.equals(defaultString)) {
				hasDefault = true;
			}
		}
		if (!hasDefault) {
			combo.add(defaultString);
		}
		combo.setText(defaultString);
	}

	public Label createTextLabel(Composite area, String string) {
		Label label = new Label(area, SWT.NONE);
		label.setText(string);
		GridData ld = new GridData();
		ld.verticalAlignment = SWT.TOP;
		label.setLayoutData(ld);
		return label;
	}

	public Button createPushButton(Composite area, String string) {
		Button label = new Button(area, SWT.PUSH);
		label.setText(string);
		GridData ld = new GridData();
		ld.verticalAlignment = SWT.TOP;
		label.setLayoutData(ld);
		return label;
	}
}
