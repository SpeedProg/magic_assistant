package com.reflexit.magiccards.ui.exportWizards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.ImportError;
import com.reflexit.magiccards.core.exports.ImportResult;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.NewSetDialog;
import com.reflexit.magiccards.ui.views.TableViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.columns.SetColumn;

public class DeckImportPreviewPage extends WizardPage {
	private TableViewerManager manager;
	private Text text;
	protected ImportResult previewResult;

	protected DeckImportPreviewPage(String pageName) {
		super(pageName);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		previewResult = null;
		if (visible == true) {
			DeckImportPage startingPage = (DeckImportPage) getPreviousPage();
			setTitle("Importing format " + startingPage.getReportType().getLabel());
			String desc = getFirstDescription();
			setErrorMessage(null);
			DeckImportWizard wizard = (DeckImportWizard) getWizard();
			try {
				InputStream st = startingPage.openInputStream();
				String textFile = getTextOfFileAsString(st, 20);
				text.setText(textFile);
			} catch (IOException e) {
				setErrorMessage("Cannot open file: " + e.getMessage());
				return;
			}
			startingPage.performImport(true);
			ImportResult result = (ImportResult) wizard.getData();
			if (result == null) {
				setErrorMessage("Cannot import");
				return;
			}
			previewResult = result;
			ICardField[] fields = result.getFields();
			if (fields != null) {
				ColumnCollection colls = manager.getColumnsCollection();
				AbstractColumn errColumn = colls.getColumn(MagicCardField.ERROR);
				String prefColumns = errColumn.getColumnFullName();
				for (int i = 0; i < fields.length; i++) {
					ICardField field = fields[i];
					AbstractColumn column = colls.getColumn(field);
					if (column != null)
						prefColumns += "," + column.getColumnFullName();
				}
				manager.updateColumns(prefColumns);
			}
			List list = result.getList();
			int count = 0;
			if (list.size() > 0) {
				manager.updateViewer(list);
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (card instanceof MagicCardPhysical && ((MagicCardPhysical) card).getError() != null) {
						count++;
					}
				}
			}
			if (result.getError() != null) {
				MagicUIActivator.log(result.getError());
				setErrorMessage("Cannot parse data file: " + result.getError().getMessage());
			} else if (list.size() == 0)
				setErrorMessage("Cannot parse data file");
			else if (count == 0)
				setDescription(desc);
			else {
				setErrorMessage(count
						+ " errors during import. Review the cards and fix errors by editing set or name of the card using cell editor");
				// manager.setSortColumn(0, 1);
			}
		}
	}

	public String getTextOfFileAsString(InputStream st, int lines) throws FileNotFoundException, IOException {
		String textFile = "";
		if (st != null) {
			String line;
			int i = 0;
			BufferedReader b = new BufferedReader(new InputStreamReader(st));
			while ((line = b.readLine()) != null && i < lines) {
				textFile += line + "\n";
				i++;
			}
			st.close();
		}
		return textFile;
	}

	public String getFirstDescription() {
		DeckImportPage startingPage = (DeckImportPage) getPreviousPage();
		CardElement element = startingPage.getElement();
		String deckName = element == null ? "newdeck" : element.getName();
		String desc = "Importing into " + deckName + ".";
		return desc;
	}

	@Override
	public void createControl(Composite parent) {
		setDescription("Import preview (10 rows)");
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout());
		text = new Text(comp, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		ld.heightHint = text.getLineHeight() * 5;
		text.setLayoutData(ld);
		manager = new TableViewerManager(columns);
		Control control = manager.createContents(comp);
		GridData tld = new GridData(GridData.FILL_BOTH);
		tld.widthHint = 100 * 5;
		control.setLayoutData(tld);
	}

	public ImportResult getPreviewResult() {
		return previewResult;
	}

	private MagicColumnCollection columns = new MagicColumnCollection(null) {
		@Override
		protected GroupColumn createGroupColumn() {
			return new GroupColumn(true, true, true) {
				@Override
				public Color getForeground(Object element) {
					IMagicCard card = (IMagicCard) element;
					if (card.getCardId() == 0) {
						if (!card.getEdition().isUnknown())
							return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
					return super.getForeground(element);
				}
			};
		}

		@Override
		protected SetColumn createSetColumn() {
			return new SetColumn() {
				@Override
				public EditingSupport getEditingSupport(ColumnViewer viewer) {
					return new SetEditingSupport(viewer) {
						@Override
						protected void setValue(Object element, Object value) {
							if (element instanceof MagicCardPhysical) {
								MagicCardPhysical card = (MagicCardPhysical) element;
								String set = (String) value;
								// set
								Collection<IMagicCard> cards = DataManager.getCardHandler().getMagicDBStore()
										.getCandidates(card.getName());
								boolean found = false;
								for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
									IMagicCard base = (IMagicCard) iterator.next();
									if (base.getSet().equals(set)) {
										card.setMagicCard((MagicCard) base);
										card.setError(null);
										found = true;
										break;
									}
								}
								if (!found) {
									NewSetDialog newdia = new NewSetDialog(getShell(), set);
									if (newdia.open() == Window.OK) {
										card.getBase().setSet(newdia.getSet().getName());
										card.setError(null);
									} else {
										card.getBase().setSet(set);
										card.setError(ImportError.SET_NOT_FOUND_ERROR);
									}
								}
								manager.getViewer().refresh(true);
							}
						}
					};
				}
			};
		}
	};
}
