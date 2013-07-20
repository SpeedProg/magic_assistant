package com.reflexit.magiccards.ui.exportWizards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.dialogs.NewSetDialog;
import com.reflexit.magiccards.ui.views.TableViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.columns.SetColumn;
import com.reflexit.magiccards.ui.widgets.ComboStringEditingSupport;

public class DeckImportPreviewPage extends WizardPage {
	private static final Object[] EMPTY_ARRAY = new Object[] {};
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
				String prefColumns = colls.getColumn(MagicCardFieldPhysical.ERROR).getColumnFullName();
				for (int i = 0; i < fields.length; i++) {
					ICardField field = fields[i];
					AbstractColumn column = colls.getColumn(field);
					if (column != null)
						prefColumns += "," + column.getColumnFullName();
				}
				manager.updateColumns(prefColumns);
			}
			int count = 0;
			List list = result.getList();
			if (list.size() > 0) {
				manager.updateViewer(list);
				// for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				// IMagicCard card = (IMagicCard) iterator.next();
				// if (card instanceof MagicCardPhysical && ((MagicCardPhysical) card).getError() !=
				// null) {
				// count++;
				// }
				// }
			}
			setDescription(desc);
			if (result.getError() != null)
				setErrorMessage("Cannot parse data file: " + result.getError().getMessage());
			else if (list.size() == 0)
				setErrorMessage("Cannot parse data file");
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
		String desc = "Importing into " + deckName + ". "
				+ "Review the cards and fix errors by editing set or name of the card using cell editor";
		return desc;
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
		manager = new TableViewerManager(null) {
			@Override
			protected ColumnCollection doGetColumnCollection(String prefPageId) {
				return new MagicColumnCollection(prefPageId) {
					@Override
					protected GroupColumn createGroupColumn() {
						return new GroupColumn(true, true, true) {
							@Override
							public Color getForeground(Object element) {
								IMagicCard card = (IMagicCard) element;
								if (card.getCardId() == 0) {
									if (Editions.getInstance().getEditionByName(card.getSet()) != null)
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
								return new ComboStringEditingSupport(viewer) {
									@Override
									protected boolean canEdit(Object element) {
										if (element instanceof MagicCardPhysical)
											return true;
										else
											return false;
									}

									@Override
									public int getStyle() {
										return SWT.NONE;
									}

									@Override
									public String[] getItems(Object element) {
										IMagicCardPhysical card = (IMagicCardPhysical) element;
										List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
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
											sets[i] = card.getSet();
										}
										return sets;
									}

									@Override
									protected Object getValue(Object element) {
										if (element instanceof MagicCardPhysical) {
											IMagicCardPhysical card = (IMagicCardPhysical) element;
											return card.getSet();
										}
										return null;
									}

									@Override
									protected void setValue(Object element, Object value) {
										if (element instanceof MagicCardPhysical) {
											MagicCardPhysical card = (MagicCardPhysical) element;
											final String oldSet = card.getSet();
											String set = (String) value;
											// set
											List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
											boolean found = false;
											for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
												IMagicCard iMagicCard = (IMagicCard) iterator.next();
												if (iMagicCard.getSet().equals(set)) {
													card.setMagicCard((MagicCard) iMagicCard);
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

							@Override
							public Color getForeground(Object element) {
								IMagicCard card = (IMagicCard) element;
								if (card.getCardId() == 0)
									return Display.getDefault().getSystemColor(SWT.COLOR_RED);
								return super.getForeground(element);
							}
						};
					}
				};
			}
		};
		Control control = manager.createContents(comp);
		GridData tld = new GridData(GridData.FILL_BOTH);
		tld.widthHint = 100 * 5;
		control.setLayoutData(tld);
	}

	public ImportResult getPreviewResult() {
		return previewResult;
	}
}
