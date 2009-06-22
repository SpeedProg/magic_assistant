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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.Tournament;

public class RoundListSection extends TSectionPart {
	private TableViewer viewer;

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
				return rounds.toArray();
			} else if (parent instanceof Object[]) {
				return ((Object[]) parent);
			}
			return new Object[0];
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
		private Color systemColorYellow;
		private Color systemColorGray;
		{
			systemColorYellow = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			systemColorGray = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
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
				case 1:
					return round.getType().name();
				case 2:
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
			return "Reset";
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
		createColumn(1, "Schedule", 100);
		TableViewerColumn c = createColumn(2, "Action", 80);
		c.setEditingSupport(new RoundActionEditingSupport(viewer, 2));
		createColumn(3, "Start", 80);
		createColumn(4, "End", 80);
		createColumn(5, "State", 100);
		viewer.setContentProvider(new ViewContentProvider());
		ViewLabelProvider labelProvider = new ViewLabelProvider();
		viewer.setLabelProvider(labelProvider);
	}
	class RoundActionEditingSupport extends EditingSupport {
		private CellEditor editor;
		private int column;

		public RoundActionEditingSupport(ColumnViewer viewer, int column) {
			super(viewer);
			this.column = column;
			// Create the correct editor based on the column index
			switch (column) {
			case 2:
				editor = new CheckboxCellEditor(((TableViewer) viewer).getTable(), SWT.CHECK | SWT.READ_ONLY) {
					@Override
					protected Control createControl(Composite parent) {
						return null;
					}
				};
				break;
			default:
				editor = null;
			}
		}

		@Override
		protected boolean canEdit(Object element) {
			if (column == 2 && element instanceof Round)
				return true;
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
			case 2:
				return round.getState() != RoundState.CLOSED;
			default:
				break;
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			Round round = (Round) element;
			switch (this.column) {
			case 2:
				RoundState action = round.getState();
				if (action == RoundState.NOT_SCHEDULED) {
					round.schedule();
				} else if (action == RoundState.READY) {
					round.setDateStart(Calendar.getInstance().getTime());
				} else if (action == RoundState.IN_PROGRESS) {
					round.setDateEnd(Calendar.getInstance().getTime());
					refresh();
				}
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
		return colv;
	}

	@Override
	public void refresh() {
		viewer.refresh(true);
		getManagedForm().getForm().reflow(false);
		super.refresh();
	}

	@Override
	public boolean setFormInput(Object input) {
		viewer.setInput(input);
		markStale();
		return true;
	}
}
