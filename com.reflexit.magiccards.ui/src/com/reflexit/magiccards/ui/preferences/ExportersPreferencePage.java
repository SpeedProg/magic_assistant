package com.reflexit.magiccards.ui.preferences;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.CustomExportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.EditExporterDialog;
import com.reflexit.magiccards.ui.jobs.ExportDeckJob;

public class ExportersPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private ListEditor listEditor;
	private Text previewText;

	public ExportersPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		listEditor = new ListEditor("exp", "Exporters", parent) {
			@Override
			protected String[] parseString(String stringList) {
				String ids[] = stringList.split(",");
				return ids;
			}

			@Override
			protected String getNewInputObject() {
				return createNewExportType();
			}

			@Override
			protected String createList(String[] items) {
				return ExportersPreferencePage.this.createList(items);
			}
		};
		IPreferenceStore store = getPreferenceStore();
		Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getExportTypes();
		String ids[] = new String[types.size()];
		int i = 0;
		for (Iterator iterator = types.iterator(); iterator.hasNext(); i++) {
			ReportType reportType = (ReportType) iterator.next();
			ids[i] = reportType.getLabel();
		}
		String value = createList(ids);
		store.setDefault(listEditor.getPreferenceName(), value);
		store.setValue(listEditor.getPreferenceName(), createList(ids));
		addField(listEditor);
		listEditor.getListControl(parent).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				updatePreview(listEditor.getListControl(parent).getSelection());
			}
		});
		createPreviewGroup(getFieldEditorParent());
	}

	protected String createNewExportType() {
		PreferenceStore store = new PreferenceStore();
		EditExporterDialog dialog = new EditExporterDialog(previewText.getShell(), store);
		if (dialog.open() == Window.OK) {
			String name = store.getString("name");
			String ext = store.getString("ext");
			if (ext.startsWith("."))
				ext = ext.substring(1);
			ReportType type = ReportType.createReportType(name, name, ext, false);
			String[] preferenceNames = store.preferenceNames();
			for (int i = 0; i < preferenceNames.length; i++) {
				String key = preferenceNames[i];
				type.setProperty(key, store.getString(key));
			}
			ImportExportFactory.addExportWorker(type, new CustomExportDelegate(type));
			return name;
		}
		return null;
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		return super.createContents(parent);
	}

	protected void createPreviewGroup(Composite parent) {
		Group previewGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		previewGroup.setLayout(layout);
		GridData ld1 = new GridData(GridData.FILL_BOTH);
		ld1.horizontalSpan = 2;
		previewGroup.setLayoutData(ld1);
		previewGroup.setText("Preview");
		previewGroup.setFont(parent.getFont());
		previewText = new Text(previewGroup, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		previewText.setText("preview...");
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 100;
		previewText.setLayoutData(layoutData);
	}

	protected void updatePreview(String[] selection) {
		if (selection.length == 0) {
			previewText.setText("");
		} else {
			ReportType type = ReportType.getByLabel(selection[0]);
			String text;
			try {
				text = exportDeck(new NullProgressMonitor(), type, true, true);
			} catch (Exception e) {
				text = "Error: " + e;
			}
			previewText.setText(text);
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
	}

	public String createList(String[] items) {
		return Arrays.toString(items).replace(", ", ",").replaceAll("[\\[\\]]", "");
	}

	public String exportDeck(IProgressMonitor monitor, ReportType reportType, boolean header, boolean sideboard)
			throws FileNotFoundException, IOException, InvocationTargetException, InterruptedException {
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IFilteredCardStore filteredLibrary = createExample(sideboard);
		new ExportDeckJob(outStream, reportType, header, filteredLibrary).syncRun();
		outStream.close();
		return outStream.toString();
	}

	private IFilteredCardStore createExample(boolean sideboard) {
		IFilteredCardStore store = new MemoryFilteredCardStore<IMagicCard>();
		IMagicCard card = (IMagicCard) DataManager.getMagicDBStore().getCard(151097);
		IMagicCard card2 = (IMagicCard) DataManager.getMagicDBStore().getCard(83035);
		IMagicCard card3 = (IMagicCard) DataManager.getMagicDBStore().getCard(83002);
		Location xxx = Location.createLocation("xxx");
		store.getCardStore().add(new MagicCardPhysical(card, xxx));
		MagicCardPhysical mcp = new MagicCardPhysical(card2, xxx);
		mcp.setCount(4);
		store.getCardStore().add(mcp);
		store.getCardStore().add(new MagicCardPhysical(card3, Location.createLocation("xxx-sideboard")));
		store.update();
		return store;
	}
}
