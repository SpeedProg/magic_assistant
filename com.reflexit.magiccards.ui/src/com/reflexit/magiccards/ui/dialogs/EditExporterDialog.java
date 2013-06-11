package com.reflexit.magiccards.ui.dialogs;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.exports.CustomExportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;

public class EditExporterDialog extends MagicDialog {
	public static final String PROP_NAME = "name";
	public static final String PROP_EXT = "ext";

	public EditExporterDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell, store == null ? new PreferenceStore() : store);
	}

	@Override
	protected void createBodyArea(Composite area1) {
		getShell().setText("Edit Exporter");
		setTitle("Define Export Format");
		setMessage("Define formatter ether by definintg java style formatter string or using field separator.\n Define fields and their order using fields field.");
		Composite area = new Composite(area1, SWT.NONE);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData();
		gda.widthHint = convertWidthInCharsToPixels(60);
		area.setLayoutData(gda);
		// createFromCombo(area);
		store.setDefault(PROP_NAME, "My Format");
		store.setDefault(PROP_EXT, ReportType.TEXT_DECK_CLASSIC.getExtension());
		store.setDefault(CustomExportDelegate.ROW_FIELDS, "COUNT,NAME");
		store.setDefault(CustomExportDelegate.ROW_FORMAT, "{0,number,#} {1}");
		store.setDefault(CustomExportDelegate.HEADER, "# " + CustomExportDelegate.DECK_NAME_VAR);
		store.setDefault(CustomExportDelegate.SB_HEADER, "Sideboard");
		Text name = createTextFieldEditor(area, "Name", PROP_NAME);
		createTextFieldEditor(area, "File extension", PROP_EXT);
		createTextFieldEditor(area, "Header", CustomExportDelegate.HEADER);
		createTextFieldEditor(area, "Sideboard Header", CustomExportDelegate.SB_HEADER);
		createTextFieldEditor(area, "Row Format (Java Style)", CustomExportDelegate.ROW_FORMAT);
		createTextFieldEditor(area, "Field Separator", CustomExportDelegate.FIELD_SEP);
		Composite fparent = new Composite(area, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		fparent.setLayoutData(layoutData);
		StringButtonFieldEditor fields = new StringButtonFieldEditor(CustomExportDelegate.ROW_FIELDS, "Fields", fparent) {
			@Override
			protected String changePressed() {
				new MagicFieldSelectorDialog(getParentShell(), store).open();
				return store.getString(CustomExportDelegate.ROW_FIELDS);
			}
		};
		fields.setPreferenceStore(store);
		fields.load();
		name.setFocus();
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
		super.okPressed();
	}
}
