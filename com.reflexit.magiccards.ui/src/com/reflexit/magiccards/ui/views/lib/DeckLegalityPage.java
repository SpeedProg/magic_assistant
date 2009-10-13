/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tym The Enchanter - initial API and implementation
 *    Alena Laskavaia - ui re-design
 *******************************************************************************/
package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.sync.ParseGathererLegality;

/**
 * Page for deck legality
 */
public class DeckLegalityPage extends AbstractDeckPage implements IDeckPage {
	private static final String NOT_PRESENT = "Not Legal";
	private TableViewer legalityTableViewer;
	private Button updateButton;
	private TableViewer cardList;
	private Map<String, String> deckInput; // map format->legality
	private Map<Integer, Map<String, String>> cardLegalities; // map card id->(map format->legaity)
	private String selectedFormat = null;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		area.setLayout(new GridLayout(2, false));
		createGui(area);
		return area;
	}

	private void createGui(Composite parent) {
		Composite left = new Composite(parent, SWT.NONE);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(GridData.FILL_BOTH));
		createUpdateButton(left);
		createLegalityTable(left);
		legalityTableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite right = new Composite(parent, SWT.NONE);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(GridData.FILL_BOTH));
		createBreakdownSection(right);
		cardList.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	class DeckLegalityLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String) {
				String format = (String) element;
				switch (columnIndex) {
				case 0:
					return format;
				case 1:
					return deckInput.get(format);
				default:
					break;
				}
			}
			return null;
		}
	}
	class CardTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof MagicCardPhisical) {
				MagicCardPhisical card = (MagicCardPhisical) element;
				switch (columnIndex) {
				case 0:
					return card.getName();
				case 1:
					return card.getCount() + "";
				case 2:
					Map<String, String> map = cardLegalities.get(card.getCardId());
					if (map == null)
						return NOT_PRESENT;
					String legality = map.get(selectedFormat);
					if (legality == null)
						return NOT_PRESENT;
					return legality;
				default:
					break;
				}
			}
			return null;
		}
	}

	private void createLegalityTable(Composite parent) {
		legalityTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		legalityTableViewer.getTable().setHeaderVisible(true);
		TableColumn formatColumn = new TableColumn(legalityTableViewer.getTable(), SWT.TOP);
		formatColumn.setText("Deck Format");
		formatColumn.setWidth(120);
		TableColumn legalityColumn = new TableColumn(legalityTableViewer.getTable(), SWT.NONE);
		legalityColumn.setText("Legality");
		legalityColumn.setWidth(80);
		legalityTableViewer.setLabelProvider(new DeckLegalityLabelProvider());
		legalityTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Map) {
					return ((Map) inputElement).keySet().toArray();
				}
				return null;
			}
		});
	}
	ViewerFilter formatFilter = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (cardLegalities == null || selectedFormat == null)
				return true;
			Map<String, String> map = cardLegalities.get(((IMagicCard) element).getCardId());
			if (map == null)
				return true;
			String string = map.get(selectedFormat);
			if (string == null)
				return true;
			if (string.equals("Legal"))
				return false;
			return true;
		}
	};

	private void createBreakdownSection(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Illegal cards in selected format");
		label.setLayoutData(new GridData());
		cardList = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		cardList.getTable().setHeaderVisible(true);
		TableColumn nameColumn = new TableColumn(cardList.getTable(), SWT.TOP);
		nameColumn.setText("Name");
		nameColumn.setWidth(120);
		TableColumn countColumn = new TableColumn(cardList.getTable(), SWT.TOP);
		countColumn.setText("Count");
		countColumn.setWidth(60);
		TableColumn legalityColumn = new TableColumn(cardList.getTable(), SWT.TOP);
		legalityColumn.setText("Legality");
		legalityColumn.setWidth(100);
		legalityTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					selectedFormat = (String) sel.getFirstElement();
				} else {
					selectedFormat = null;
				}
				cardList.setInput(store);
			}
		});
		cardList.setLabelProvider(new CardTableLabelProvider());
		cardList.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
			}

			public void dispose() {
				// TODO Auto-generated method stub
			}

			public Object[] getElements(Object inputElement) {
				Object[] cards = new Object[store.size()];
				int i = 0;
				for (Object object : store) {
					cards[i++] = object;
				}
				return cards;
			}
		});
		cardList.setFilters(new ViewerFilter[] { formatFilter });
		cardList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					view.getSite().getSelectionProvider().setSelection(sel);
				}
			}
		});
	}

	private void createUpdateButton(Composite parent) {
		updateButton = new Button(parent, SWT.PUSH);
		updateButton.setText("Calculate Deck Legality");
		updateButton.setLayoutData(new GridData());
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				performUpdate();
			}
		});
	}

	protected void performUpdate() {
		if (store != null) {
			Job job = new Job("Calculating Legality") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Calculating Legality", 100);
					cardLegalities = calculateDeckLegalities(new SubProgressMonitor(monitor, 50));
					deckInput = calculateDeckLegality(cardLegalities, new SubProgressMonitor(monitor, 50));
					getControl().getDisplay().syncExec(new Runnable() {
						public void run() {
							legalityTableViewer.setInput(deckInput);
							if (deckInput.size() > 0) {
								legalityTableViewer.setSelection(new StructuredSelection(deckInput.keySet().iterator()
								        .next()));
							}
						}
					});
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	private Map<Integer, Map<String, String>> calculateDeckLegalities(IProgressMonitor monitor) {
		try {
			monitor.beginTask("", 100);
			monitor.worked(10);
			return ParseGathererLegality.cardSetLegality(store);
		} catch (IOException e) {
			MessageDialog.openError(getControl().getShell(), "Error", e.getMessage());
			return null;
		}
	}
	private String formats[] = { "Standard", "Extended", "Legacy", "Vintage" };

	private Map<String, String> calculateDeckLegality(Map<Integer, Map<String, String>> cardLegalities,
	        IProgressMonitor monitor) {
		monitor.beginTask("", cardLegalities.size() + 1);
		Map<String, String> deckLegalityRestrictions = new LinkedHashMap<String, String>();
		for (String format : formats) {
			deckLegalityRestrictions.put(format, null);
		}
		monitor.worked(1);
		for (Map.Entry<Integer, Map<String, String>> cardLegalityEntry : cardLegalities.entrySet()) {
			updateDeckLegality(deckLegalityRestrictions, cardLegalityEntry.getValue());
			monitor.worked(1);
		}
		monitor.done();
		return deckLegalityRestrictions;
	}

	private void updateDeckLegality(Map<String, String> deckLegalityRestrictions,
	        Map<String, String> cardLegalityRestrictions) {
		for (Map.Entry<String, String> deckLegalityEntry : deckLegalityRestrictions.entrySet()) {
			if (!cardLegalityRestrictions.keySet().contains(deckLegalityEntry.getKey())) {
				deckLegalityEntry.setValue(NOT_PRESENT);
			}
		}
		for (Map.Entry<String, String> cardLegalityEntry : cardLegalityRestrictions.entrySet()) {
			String formatForCard = cardLegalityEntry.getKey();
			String formatLegality = cardLegalityEntry.getValue();
			if (deckLegalityRestrictions.get(formatForCard) == null) {
				deckLegalityRestrictions.put(formatForCard, formatLegality);
			} else {
				if (isNewRestrictionMoreRestrictive(formatLegality, deckLegalityRestrictions.get(formatForCard))) {
					deckLegalityRestrictions.put(formatForCard, formatLegality);
				}
			}
		}
	}

	private boolean isNewRestrictionMoreRestrictive(String leg1, String leg2) {
		int v1 = getRestrictiveness(leg1);
		int v2 = getRestrictiveness(leg2);
		return v1 < v2;
	}

	private int getRestrictiveness(String leg) {
		if (leg.equals("Banned"))
			return 0;
		if (leg.equals("Restricted"))
			return 5;
		if (leg.equals("Legal"))
			return 10;
		return 0;
	}
}
