package com.reflexit.magiccards.ui.exportWizards;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.ImportResult;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.widgets.ComboStringEditingSupport;

public class DeckImportPreviewPage extends WizardPage {
	private static final Object[] EMPTY_ARRAY = new Object[] {};
	private TableViewer tableViewer;
	private Text text;
	protected ImportResult previewResult;
	private EditingSupport setStringEditingSupport;
	private EditingSupport nameEditingSupport;

	protected DeckImportPreviewPage(String pageName) {
		super(pageName);
	}

	class TabLabelProvder extends BaseLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Object[]) {
				if (columnIndex == 0)
					return "";
				Object[] arr = (Object[]) element;
				if (arr.length <= columnIndex - 1)
					return "[]";
				Object object = arr[columnIndex - 1];
				if (object == null)
					return "";
				return object.toString();
			} else if (element instanceof ICard) {
				ICard card = (ICard) element;
				if (columnIndex == 0 && card instanceof MagicCardPhysical) {
					Object err = ((MagicCardPhysical) card).getError();
					if (err == null)
						return "";
					return err.toString();
				}
				ICardField[] pfields = previewResult.getFields();
				ICardField fi = pfields[columnIndex - 1];
				Object o = card.getObjectByField(fi);
				return o == null ? null : o.toString();
			}
			return null;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		previewResult = null;
		if (visible == true) {
			DeckImportPage startingPage = (DeckImportPage) getPreviousPage();
			CardElement element = startingPage.getElement();
			String deckName = element == null ? "newdeck" : element.getName();
			setDescription("Importing into " + deckName + ". " + (startingPage.hasHeaderRow() ? "Header row." : "No header row.")
					+ " Format " + startingPage.getReportType().getLabel() + ".");
			setErrorMessage(null);
			DeckImportWizard wizard = (DeckImportWizard) getWizard();
			try {
				InputStream st = startingPage.openInputStream();
				String textFile = "";
				if (st != null) {
					String line;
					int i = 0;
					BufferedReader b = new BufferedReader(new InputStreamReader(st));
					while ((line = b.readLine()) != null && i < 20) {
						textFile += line + "\n";
						i++;
					}
					st.close();
				}
				text.setText(textFile);
			} catch (IOException e) {
				setErrorMessage("Cannot open file: " + e.getMessage());
				return;
			}
			startingPage.performImport(true);
			ImportResult result = (ImportResult) wizard.getData();
			previewResult = result;
			TableColumn[] columns = tableViewer.getTable().getColumns();
			for (TableColumn tableColumn : columns) {
				tableColumn.dispose();
			}
			TableColumn cole = new TableColumn(tableViewer.getTable(), SWT.NONE);
			cole.setWidth(100);
			cole.setText("Errors");
			if (result.getFields() != null)
				for (ICardField f : result.getFields()) {
					TableColumn col = new TableColumn(tableViewer.getTable(), SWT.NONE);
					col.setWidth(100);
					if (f != null) {
						col.setText(f.toString());
						if (f == MagicCardField.SET) {
							TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, col);
							tableViewerColumn.setEditingSupport(setComboEditingSupport);
							tableViewerColumn.setLabelProvider(new CellLabelProvider() {
								@Override
								public void update(ViewerCell cell) {
									IMagicCard card = (IMagicCard) cell.getElement();
									if (card.getCardId() == 0)
										cell.setText("[NEW] " + card.getSet());
									else
										cell.setText(card.getSet());
								}
							});
						} else if (f == MagicCardField.NAME) {
							TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, col);
							tableViewerColumn.setEditingSupport(nameEditingSupport);
							tableViewerColumn.setLabelProvider(new CellLabelProvider() {
								@Override
								public void update(ViewerCell cell) {
									IMagicCard card = (IMagicCard) cell.getElement();
									cell.setText(card.getName());
								}
							});
						}
					}
				}
			if (result.getList().size() > 0)
				tableViewer.setInput(result.getList());
			if (result.getError() != null)
				setErrorMessage("Cannot parse data file: " + result.getError().getMessage());
			else if (result.getList().size() == 0)
				setErrorMessage("Cannot parse data file");
		}
	}

	public void createControl(Composite parent) {
		setDescription("Import preview (10 rows)");
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout());
		text = new Text(comp, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		ld.heightHint = text.getLineHeight() * 5;
		text.setLayoutData(ld);
		tableViewer = new TableViewer(comp, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection) {
					return ((Collection) inputElement).toArray();
				}
				if (inputElement instanceof ImportResult) {
					List values = ((ImportResult) inputElement).getList();
					return values.toArray();
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
		setComboEditingSupport = new ComboStringEditingSupport(tableViewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof MagicCardPhysical)
					return true;
				else
					return false;
			}

			@Override
			public String[] getItems(Object element) {
				IMagicCardPhysical card = (IMagicCardPhysical) element;
				List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
				if (cards.size() == 0)
					return null;
				if (cards.size() == 1 && cards.get(0).getSet().equals(card.getSet()))
					return null;
				int len = cards.size();
				if (card.getCardId() == 0) {
					len++;
				}
				String sets[] = new String[len];
				int i = 0;
				for (Iterator iterator = cards.iterator(); iterator.hasNext(); i++) {
					IMagicCard mCard = (IMagicCard) iterator.next();
					sets[i] = mCard.getSet();
				}
				if (card.getCardId() == 0) {
					sets[i] = "[NEW] " + card.getSet();
				}
				return sets;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhysical) {
					IMagicCardPhysical card = (IMagicCardPhysical) element;
					if (card.getCardId() == 0) {
						return "[NEW] " + card.getSet();
					}
					return card.getSet();
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					// set
					List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
					String set = (String) value;
					if (set.startsWith("[NEW] ")) {
						set = set.substring(6);
					} else
						for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
							IMagicCard iMagicCard = (IMagicCard) iterator.next();
							if (iMagicCard.getSet().equals(set)) {
								card.setMagicCard((MagicCard) iMagicCard);
								card.setError(null);
								break;
							}
						}
					tableViewer.refresh(true);
				}
			}
		};
		nameEditingSupport = new EditingSupport(tableViewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof MagicCardPhysical)
					return true;
				else
					return false;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				TextCellEditor editor = new TextCellEditor((Composite) tableViewer.getControl(), SWT.NONE);
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhysical) {
					return ((MagicCardPhysical) element).getName();
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					MagicCard base = (MagicCard) card.getBase().clone();
					base.setName((String) value);
					base.setCardId(0);
					card.setMagicCard(base);
					ImportUtils.updateCardReference(card);
					tableViewer.refresh(true);
				}
			}
		};
	}

	private EditingSupport setComboEditingSupport;

	public ImportResult getPreviewResult() {
		return previewResult;
	}
}
