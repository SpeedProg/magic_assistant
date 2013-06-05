package com.reflexit.magiccards.ui.dialogs;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;

public class EditExporterDialog extends MagicDialog {
	public EditExporterDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell, store == null ? new PreferenceStore() : store);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Edit Exporter");
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(2, false));
		GridData gda = new GridData();
		gda.widthHint = convertWidthInCharsToPixels(60);
		area.setLayoutData(gda);
		Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getExportTypes();
		String ids[] = new String[types.size()];
		int i = 0;
		for (Iterator iterator = types.iterator(); iterator.hasNext(); i++) {
			ReportType reportType = (ReportType) iterator.next();
			ids[i] = reportType.getLabel();
		}
		createTextFieldEditor(area, "Name", "name");
		store.setDefault("parent", ReportType.TEXT_DECK_CLASSIC.getLabel());
		createComboFieldEditor(area, "Copy From", "parent", ids);
		store.setDefault("ext", ReportType.TEXT_DECK_CLASSIC.getExtension());
		createTextFieldEditor(area, "File extension", "ext");
		createTextFieldEditor(area, "Row Format C-Style", "row.format");
		return area;
	}
}
