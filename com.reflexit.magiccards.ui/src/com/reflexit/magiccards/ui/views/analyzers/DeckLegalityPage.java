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
package com.reflexit.magiccards.ui.views.analyzers;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
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

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.sync.ParseGathererLegality;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.utils.StoredSelectionProvider;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

/**
 * Page for deck legality
 */
public class DeckLegalityPage extends AbstractDeckPage implements IDeckPage {
	private TableViewer legalityTableViewer;
	private Button updateButton;
	private TableViewer cardList;
	private LegalityMap deckInput; // map format->legality
	private Map<Integer, LegalityMap> cardLegalities; // map card
														// id->(map
														// format->legaity)
	private ISelectionProvider selProvider = new StoredSelectionProvider();
	private String selectedFormat = null;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		getArea().setLayout(layout);
		createGui(getArea());
		return getArea();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return selProvider;
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
						return deckInput.get(format).getLabel();
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
			if (element instanceof MagicCardPhysical) {
				MagicCardPhysical card = (MagicCardPhysical) element;
				switch (columnIndex) {
					case 0:
						return card.getName();
					case 1:
						return card.getCount() + "";
					case 2:
						LegalityMap map = cardLegalities.get(card.getCardId());
						if (map == null)
							return Legality.NOT_LEGAL.getLabel();
						Legality legality = map.get(selectedFormat);
						if (legality == null)
							return Legality.NOT_LEGAL.getLabel();
						return legality.getLabel();
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
				// ignore
			}

			public void dispose() {
				// ignore
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
			LegalityMap map = cardLegalities.get(((IMagicCard) element).getCardId());
			if (map == null)
				return true;
			Legality string = map.get(selectedFormat);
			if (string == null)
				return true;
			if (string == Legality.LEGAL)
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
				selProvider.setSelection(sel);
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
					cardLegalities = calculateDeckLegalities(new SubProgressMonitor(monitor, 90));
					deckInput = LegalityMap.calculateDeckLegality(cardLegalities.values());
					getControl().getDisplay().syncExec(new Runnable() {
						public void run() {
							legalityTableViewer.setInput(deckInput);
							if (deckInput.size() > 0) {
								legalityTableViewer.setSelection(new StructuredSelection(deckInput.keySet().iterator().next()));
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

	private Map<Integer, LegalityMap> calculateDeckLegalities(IProgressMonitor monitor) {
		try {
			return ParseGathererLegality.cardSetLegality(store, new CoreMonitorAdapter(monitor));
		} catch (IOException e) {
			MessageDialog.openError(getControl().getShell(), "Error", e.getMessage());
			return null;
		}
	}
}
