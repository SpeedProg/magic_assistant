/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;
import com.reflexit.mtgtournament.ui.tour.dialogs.RoundEditorDialog;

public class RoundListSection extends TSectionPart {
	private static final int SCHEDULE_COL = 1;
	private static final int ACTION_COL = 2;
	private TableViewer viewer;
	private Tournament tournament;
	private Button add;
	private Button del;
	private Button edit;

	public RoundListSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
	}
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Tournament) {
				Tournament t = (Tournament) parent;
				List<Round> rounds = t.getRounds();
				if (t.isDraftRound() || rounds.size() == 0)
					return rounds.toArray();
				else
					return rounds.subList(1, rounds.size()).toArray();
			} else if (parent instanceof Object[]) {
				return ((Object[]) parent);
			}
			return new Object[0];
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
		private Color systemColorYellow;
		private Color systemColorGray;
		private Color systemColorBlue;
		{
			systemColorYellow = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			systemColorGray = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			systemColorBlue = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Round) {
				Round round = (Round) element;
				switch (columnIndex) {
				case 0:
					int number = round.getNumber();
					if (number == 0)
						return "Draft";
					return "Round " + String.valueOf(number);
				case SCHEDULE_COL:
					return round.getType().name();
				case ACTION_COL:
					return getAction(round);
				case 3:
					return toDate(round.getDateStart());
				case 4:
					return toDate(round.getDateEnd());
				case 5:
					return round.getState().name();
				default:
					return "";
				}
			}
			return "";
		}

		private String toDate(Date d) {
			if (d == null)
				return "";
			DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT);
			return format.format(d);
		}

		public Color getBackground(Object element, int columnIndex) {
			if (element instanceof Round) {
				Round round = (Round) element;
				RoundState action = round.getState();
				switch (action) {
				case IN_PROGRESS:
					return systemColorYellow;
				default:
					return null;
				}
			}
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			if (element instanceof Round) {
				Round round = (Round) element;
				RoundState action = round.getState();
				if (columnIndex == ACTION_COL && action != RoundState.CLOSED && action != RoundState.NOT_READY) {
					return systemColorBlue;
				}
				switch (action) {
				case CLOSED:
					return systemColorGray;
				default:
					return null;
				}
			}
			return null;
		}
	}

	protected String getAction(Round round) {
		RoundState action = round.getState();
		switch (action) {
		case NOT_SCHEDULED:
			return "Schedule";
		case READY:
			return "Start";
		case IN_PROGRESS:
			return "End";
		case CLOSED:
			return "";
		default:
			return null;
		}
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Rounds");
		//section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		Table table = toolkit.createTable(sectionClient, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
		        | SWT.BORDER);
		viewer = new TableViewer(table);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_VERTICAL));
		viewer.getTable().setHeaderVisible(true);
		createColumn(0, "Round", 80);
		createColumn(SCHEDULE_COL, "Schedule", 100);
		createColumn(ACTION_COL, "Action", 80);
		createColumn(3, "Start", 80);
		createColumn(4, "End", 80);
		createColumn(5, "State", 100);
		viewer.setContentProvider(new ViewContentProvider());
		ViewLabelProvider labelProvider = new ViewLabelProvider();
		viewer.setLabelProvider(labelProvider);
		createButtons(sectionClient);
		updateEnablement();
	}

	/**
	 * 
	 */
	private void updateEnablement() {
		boolean enabled = false;
		if (tournament != null) {
			if (tournament.isScheduled())
				enabled = true;
		}
		add.setEnabled(enabled);
		del.setEnabled(enabled);
	}

	protected void createButtons(Composite sectionClient) {
		Composite buttons = new Composite(sectionClient, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		buttons.setLayout(layout);
		add = toolkit.createButton(buttons, "Add", SWT.PUSH);
		add.setLayoutData(hor.create());
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				try {
					addRound();
				} catch (Exception e) {
					showError(e.getMessage());
				}
				modelUpdated();
			}
		});
		edit = toolkit.createButton(buttons, "Edit...", SWT.PUSH);
		edit.setLayoutData(hor.create());
		edit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				try {
					IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
					editRounds(sel.toList());
				} catch (Exception e) {
					showError(e.getMessage());
				}
				modelUpdated();
			}
		});
		del = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		del.setLayoutData(hor.create());
		del.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				try {
					deleteRounds(sel.toList());
				} catch (Exception e) {
					showError(e.getMessage());
				}
				modelUpdated();
			}
		});
	}

	protected void editRounds(List<Round> list) {
		if (list.size() == 0)
			throw new IllegalArgumentException("Round is not selected");
		RoundEditorDialog d = new RoundEditorDialog(getSection().getShell(), list.get(0));
		if (d.open() == Dialog.OK) {
			// well...
		}
	}

	/**
	 * @param list
	 */
	protected void deleteRounds(List<Round> list) {
		Collections.reverse(list);
		for (Round round : list) {
			deleteRound(round);
		}
	}

	protected void deleteRound(Round round) {
		Tournament t = round.getTournament();
		int n = t.getNumberOfRounds();
		if (round.getNumber() != n) {
			throw new IllegalStateException("Cannot delete non last round");
		}
		if (round.getState() == RoundState.IN_PROGRESS || round.getState() == RoundState.CLOSED) {
			throw new IllegalStateException("Cannot delete in-progress or complete round");
		}
		if (t.removeRound(round))
			t.setNumberOfRounds(n - 1);
	}

	/**
	 * 
	 */
	protected void addRound() {
		int n = tournament.getNumberOfRounds();
		Round r = new Round(n + 1);
		tournament.addRound(r);
		r.setType(tournament.getType());
		tournament.setNumberOfRounds(n + 1);
	}
	class RoundActionEditingSupport extends EditingSupport {
		private CellEditor editor;
		private int column;

		public RoundActionEditingSupport(ColumnViewer viewer, int column) {
			super(viewer);
			this.column = column;
			// Create the correct editor based on the column index
			switch (column) {
			case ACTION_COL:
				editor = new CheckboxCellEditor(((TableViewer) viewer).getTable(), SWT.CHECK | SWT.READ_ONLY) {
					@Override
					protected Control createControl(Composite parent) {
						return null;
					}
				};
				break;
			case SCHEDULE_COL:
				editor = new ComboBoxCellEditor(((TableViewer) viewer).getTable(), TournamentType.stringValues());
				break;
			default:
				editor = null;
			}
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof Round) {
				Round round = (Round) element;
				switch (column) {
				case ACTION_COL:
					return true;
				case SCHEDULE_COL:
					//if (round.getTournament().getType() != TournamentType.ROUND_ROBIN) 
				{
					return true;
				}
					//return false;
				default:
					break;
				}
				return false;
			}
			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			Round round = (Round) element;
			switch (this.column) {
			case ACTION_COL:
				return round.getState() != RoundState.CLOSED;
			case SCHEDULE_COL:
				return round.getType().ordinal();
			default:
				break;
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			Round round = (Round) element;
			switch (this.column) {
			case ACTION_COL:
				RoundState state = round.getState();
				if (state == RoundState.NOT_SCHEDULED) {
					round.schedule();
					modelUpdated();
				} else if (state == RoundState.READY) {
					round.setDateStart(Calendar.getInstance().getTime());
					modelUpdated();
				} else if (state == RoundState.IN_PROGRESS) {
					round.close();
					modelUpdated();
				}
				break;
			case SCHEDULE_COL:
				round.setType(TournamentType.valueOf(((Integer) value).intValue()));
				modelUpdated();
				break;
			default:
				break;
			}
			getViewer().update(element, null);
		}
	}

	private TableViewerColumn createColumn(int i, String name, int width) {
		TableColumn col = new TableColumn(viewer.getTable(), SWT.NONE, i);
		col.setText(name);
		col.setWidth(width);
		TableViewerColumn colv = new TableViewerColumn(viewer, col);
		colv.setEditingSupport(new RoundActionEditingSupport(viewer, i));
		return colv;
	}

	@Override
	public void refresh() {
		viewer.refresh(true);
		getManagedForm().getForm().reflow(false);
		super.refresh();
	}

	protected void modelUpdated() {
		save();
		reload();
	}

	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof Tournament) {
			this.tournament = (Tournament) input;
		}
		viewer.setInput(input);
		markStale();
		updateEnablement();
		return true;
	}
}
