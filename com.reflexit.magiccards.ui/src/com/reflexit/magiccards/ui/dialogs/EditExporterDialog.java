package com.reflexit.magiccards.ui.dialogs;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.exports.CustomExportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.preferences.ExportersPreferencePage;

public class EditExporterDialog extends MagicDialog {
	public static final String PROP_NAME = "name";
	public static final String PROP_EXT = "ext";
	private boolean newexporter;
	private Text previewText;
	private ReportType origType;

	public EditExporterDialog(Shell parentShell, ReportType orig) {
		super(parentShell, new PreferenceStore());
		this.newexporter = orig == null;
		this.origType = orig;
		store.setDefault(PROP_NAME, "My Format");
		store.setDefault(PROP_EXT, ReportType.TEXT_DECK_CLASSIC.getExtension());
		store.setDefault(CustomExportDelegate.ROW_FIELDS, "COUNT,NAME");
		store.setDefault(CustomExportDelegate.ROW_FORMAT, "{0,number,#} {1}");
		store.setDefault(CustomExportDelegate.HEADER, "# " + CustomExportDelegate.DECK_NAME_VAR);
		store.setDefault(CustomExportDelegate.SB_HEADER, "Sideboard");
		if (orig != null) {
			copyFromType(orig);
		}
	}

	@Override
	protected void createBodyArea(Composite area1) {
		getShell().setText(newexporter ? "New Exporter" : "Edit Exporter");
		setTitle("Define Export Format");
		setMessage("Define formatter ether by definintg java style formatter string or using field separator.\n"
				+ "Define fields and their order using fields field.");
		Composite area = new Composite(area1, SWT.NONE);
		area.setLayout(new GridLayout(4, false));
		GridData gda = new GridData(GridData.FILL_HORIZONTAL);
		area.setLayoutData(gda);
		// createFromCombo(area);
		Text name = createTextFieldEditor(area, "Name", PROP_NAME);
		createTextFieldEditor(area, "File Extension", PROP_EXT, "Default file extension for exported file");
		createTextFieldEditor(area, "Header", CustomExportDelegate.HEADER, "Line separating multiple decks or file header, "
				+ "leave empty to ommit.\n" + "User can also disable it from export dialog using 'Generate header row' checkbox");
		createTextFieldEditor(area, "Sideboard Header", CustomExportDelegate.SB_HEADER,
				"Line separating sideboard from the deck, leave empty to ommit");
		createTextFieldEditor(area, "Formatter (Java Style)", CustomExportDelegate.ROW_FORMAT,
				"Formatter for fields values, i.e {0} x {1}, would result in 3 x Naturalize, if fields set to COUNT,NAME");
		createTextFieldEditor(area, "Field Separator", CustomExportDelegate.FIELD_SEP, "Instead of formatter can use field separator");
		Composite fparent = new Composite(area, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 4;
		fparent.setLayoutData(layoutData);
		StringButtonFieldEditor fields = new StringButtonFieldEditor(CustomExportDelegate.ROW_FIELDS, "Fields", fparent) {
			@Override
			protected String changePressed() {
				new MagicFieldSelectorDialog(getParentShell(), store).open();
				validate();
				return store.getString(CustomExportDelegate.ROW_FIELDS);
			}
		};
		fields.setPreferenceStore(store);
		fields.load();
		GridData ld1 = new GridData(GridData.FILL_BOTH);
		ld1.horizontalSpan = 4;
		createPreviewGroup(fparent).setLayoutData(ld1);
		name.setFocus();
	}

	protected Group createPreviewGroup(Composite parent) {
		Group previewGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		previewGroup.setLayout(layout);
		previewGroup.setText("Preview");
		previewGroup.setFont(parent.getFont());
		previewText = new Text(previewGroup, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		previewText.setText("");
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 100;
		previewText.setLayoutData(layoutData);
		return previewGroup;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button butt = createButton(parent, 2, "Preview", false);
		butt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePreview();
			}
		});
		// create OK and Cancel buttons by default
		super.createButtonsForButtonBar(parent);
	}

	protected void updatePreview() {
		ReportType type = getReportType("preview");
		String text;
		try {
			text = ExportersPreferencePage.exportDeck(new NullProgressMonitor(), type, true, true);
			type.delete();
		} catch (Exception e) {
			text = "Error: " + e;
		}
		previewText.setText(text);
	}

	@Override
	protected void validate() {
		setErrorMessage(null);
		String name = store.getString(PROP_NAME);
		if (newexporter) {
			if (ReportType.getByLabel(name) != null) {
				setErrorMessage("Type '" + name + "'already exists");
			}
		}
		String ext = store.getString(PROP_EXT);
		if (ext == null || ext.length() == 0) {
			setErrorMessage("File extension should not be empty");
		}
	}

	protected void createFromCombo(Composite area) {
		store.setDefault("parent", ReportType.TEXT_DECK_CLASSIC.getLabel());
		Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getExportTypes();
		String ids[] = new String[types.size()];
		int i = 0;
		for (Iterator iterator = types.iterator(); iterator.hasNext(); i++) {
			ReportType reportType = (ReportType) iterator.next();
			ids[i] = reportType.getLabel();
		}
		createComboFieldEditor(area, "Copy From", "parent", ids);
	}

	@Override
	protected void okPressed() {
		validate();
		if (getErrorMessage() != null)
			return;
		ReportType type = getReportType();
		if (type != origType && origType != null) {
			try {
				origType.delete();
			} catch (IOException e) {
				setErrorMessage(e.getMessage());
				return;
			}
		}
		try {
			type.save();
		} catch (IOException e) {
			setErrorMessage(e.getMessage());
			return;
		}
		super.okPressed();
	}

	public ReportType getReportType() {
		String name = store.getString(EditExporterDialog.PROP_NAME);
		return getReportType(name);
	}

	public ReportType getReportType(String name) {
		ReportType type = ReportType.createReportType(name);
		type.setCustom(true);
		copyFromStore(type);
		type.setExportDelegate(new CustomExportDelegate(type));
		return type;
	}

	public void copyFromStore(ReportType type) {
		String[] preferenceNames = store.preferenceNames();
		for (int i = 0; i < preferenceNames.length; i++) {
			String key = preferenceNames[i];
			type.setProperty(key, store.getString(key));
		}
	}

	public void copyFromType(ReportType type) {
		String[] preferenceNames = type.getProperties().keySet().toArray(new String[type.getProperties().size()]);
		for (int i = 0; i < preferenceNames.length; i++) {
			String key = preferenceNames[i];
			store.setValue(key, type.getProperty(key));
		}
		store.setValue(EditExporterDialog.PROP_NAME, type.getLabel());
	}
}
