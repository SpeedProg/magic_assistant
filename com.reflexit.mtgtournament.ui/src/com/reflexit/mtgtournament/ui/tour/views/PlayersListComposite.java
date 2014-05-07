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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerList;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Tournament;

public class PlayersListComposite extends Composite {
	private TableViewer viewer;
	private boolean forTournamentStanding;
	private int treeStyle;
	private TableSorter tableSorter;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Tournament) {
				Tournament t = (Tournament) parent;
				return t.getPlayersInfo().toArray();
			}
			if (parent instanceof PlayerList) {
				return ((PlayerList) parent).getPlayers().toArray();
			}
			return new Object[0];
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Player) {
				return ((Player) element).getName();
			}
			return super.getText(element);
		}

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Player) {
				Player p = (Player) element;
				switch (columnIndex) {
				case 0:
					return getText(element);
				case 1:
					return p.getId();
				case 2:
					return "";
				case 3:
					return String.valueOf(p.getPoints());
				case 4:
					return "";
				case 5:
					return String.valueOf(p.getGames());
				default:
					break;
				}
			} else if (element instanceof PlayerTourInfo) {
				PlayerTourInfo pi = (PlayerTourInfo) element;
				Player p = pi.getPlayer();
				switch (columnIndex) {
				case 0:
					return p.getName();
				case 1:
					return p.getId();
				case 2:
					return String.valueOf(pi.getPlace());
				case 3:
					return String.valueOf(pi.getPoints());
				case 4:
					return getStats(pi);
				case 5:
					return String.valueOf(pi.getRoundsPlayed());
				case 6:
					return String.valueOf(pi.getOMW());
				case 7:
					return String.valueOf(pi.getPGW());
				case 8:
					return String.valueOf(pi.getOGW());
				default:
					break;
				}
			}
			return null;
		}
	}

	public PlayersListComposite(Composite parent, int style, boolean hasColumns) {
		super(parent, SWT.NONE);
		this.treeStyle = style;
		this.forTournamentStanding = hasColumns;
		setLayout(new GridLayout());
		createBody(this);
		getViewer().getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public String getStats(PlayerTourInfo pi) {
		return pi.getWin() + "-" + pi.getDraw() + "-" + pi.getLost();
	}

	protected void createBody(Composite parent) {
		// players table
		this.viewer = (new TableViewer(parent, treeStyle | SWT.H_SCROLL | SWT.V_SCROLL));
		this.viewer.setContentProvider(new ViewContentProvider());
		this.viewer.setLabelProvider(new ViewLabelProvider());
		this.viewer.getTable().setHeaderVisible(true);
		createColumn(0, "Name", 140);
		createColumn(1, "PIN", 120);
		if (forTournamentStanding) {
			createColumn(2, "Place", 60);
			createColumn(3, "Points", 60);
			createColumn(4, "W-D-L", 60);
			createColumn(5, "Matches", 60);
			createColumn(6, "OMW%", 60);
			createColumn(7, "PGW%", 60);
			createColumn(8, "OGW%", 60);
		}
		// Set the sorter for the table
		tableSorter = new TableSorter();
		viewer.setSorter(tableSorter);
	}

	public class TableSorter extends ViewerSorter {
		private int propertyIndex;
		// private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public TableSorter() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			int rc = 0;
			if (e1 instanceof Player && e2 instanceof Player) {
				Player p1 = (Player) e1;
				Player p2 = (Player) e2;
				switch (propertyIndex) {
				case 0:
					rc = p1.getName().compareTo(p2.getName());
					break;
				case 1:
					rc = p1.getId().compareTo(p2.getId());
					break;
				case 3:
					rc = p1.getPoints() - p2.getPoints();
					break;
				case 5:
					rc = p1.getGames() - p2.getGames();
					break;
				default:
					rc = 0;
				}
			} else if (e1 instanceof PlayerTourInfo && e2 instanceof PlayerTourInfo) {
				PlayerTourInfo p1 = (PlayerTourInfo) e1;
				PlayerTourInfo p2 = (PlayerTourInfo) e2;
				switch (propertyIndex) {
				case 0:
					rc = compare(viewer, p1.getPlayer(), p2.getPlayer());
					break;
				case 1:
					rc = compare(viewer, p1.getPlayer(), p2.getPlayer());
					break;
				case 2:
					rc = p1.getPlace() - p2.getPlace();
					break;
				case 3:
					rc = p1.getPoints() - p2.getPoints();
					break;
				case 4:
					rc = p1.getWin() - p2.getWin();
					break;
				case 5:
					rc = p1.getRoundsPlayed() - p2.getRoundsPlayed();
					break;
				default:
					rc = 0;
				}
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}

	private void createColumn(final int index, String name, int width) {
		final TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE, index);
		column.setText(name);
		column.setWidth(width);
		// Setting the right sorter
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(index);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		});
	}

	public TableViewer getViewer() {
		return viewer;
	}
}
