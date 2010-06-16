package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import com.reflexit.magiccards.core.exports.PreviewResult;
import com.reflexit.magiccards.core.model.ICardField;

public class DeckImportPreviewPage extends WizardPage {
	private static final Object[] EMPTY_ARRAY = new Object[] {};
	private TableViewer tableViewer;
	private Text text;

	protected DeckImportPreviewPage(String pageName) {
		super(pageName);
	}
	static class TabLabelProvder extends BaseLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Object[]) {
				Object object = ((Object[]) element)[columnIndex];
				if (object == null)
					return "";
				return object.toString();
			}
			return null;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible == true) {
			DeckImportPage startingPage = (DeckImportPage) getPreviousPage();
			setDescription("Importing into " + startingPage.getElement() + ". Import preview (First 10 rows). "
			        + (startingPage.hasHeaderRow() ? "Header row." : "No header row.") + " Format "
			        + startingPage.getReportType() + ".");
			setErrorMessage(null);
			DeckImportWizard wizard = (DeckImportWizard) getWizard();
			try {
				InputStream st = startingPage.openInputStream();
				String textFile = "";
				if (st != null) {
					String line;
					int i = 0;
					BufferedReader b = new BufferedReader(new InputStreamReader(st));
					while ((line = b.readLine()) != null && i < 10) {
						textFile += line + "\n";
						i++;
					}
				}
				text.setText(textFile);
				st.close();
			} catch (IOException e) {
				setErrorMessage("Cannot open file: " + e.getMessage());
				return;
			}
			startingPage.performImport(true);
			PreviewResult result = (PreviewResult) wizard.getData();
			TableColumn[] columns = tableViewer.getTable().getColumns();
			for (TableColumn tableColumn : columns) {
				tableColumn.dispose();
			}
			if (result.getFields() != null)
				for (ICardField f : result.getFields()) {
					TableColumn col = new TableColumn(tableViewer.getTable(), SWT.NONE);
					col.setWidth(100);
					if (f != null) {
						col.setText(f.toString());
					}
				}
			if (result.getValues().size() > 0)
				tableViewer.setInput(result.getValues());
			if (result.getError() != null)
				setErrorMessage("Cannot parse data file: " + result.getError().getMessage());
			else if (result.getValues().size() == 0)
				setErrorMessage("Cannot parse data file");
		}
	}

	public void createControl(Composite parent) {
		setDescription("Import preview (10 rows)");
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout());
		text = new Text(comp, SWT.WRAP | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(comp, SWT.BORDER);
		tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection) {
					return ((Collection) inputElement).toArray();
				}
				return EMPTY_ARRAY;
			}

			public void dispose() {
				// TODO Auto-generated method stub
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
			}
		});
		tableViewer.setLabelProvider(new TabLabelProvder());
		tableViewer.getTable().setHeaderVisible(true);
	}
}
