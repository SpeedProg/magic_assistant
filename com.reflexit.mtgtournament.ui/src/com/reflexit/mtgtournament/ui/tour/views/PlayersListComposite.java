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
	private static final int NAME_COLUMN_INDEX = 0;
	private static final int ID_COLUMN_INDEX = 1;
	private static final int PLACE_COLUMN_INDEX = 2;
	private static final int POINTS_COLUMN_INDEX = 3;
	private static final int WDL_COLUMN_INDEX = 4;
	private static final int ROUNDS_PLAYED_COLUMN_INDEX = 5;
	private static final int GAMES_PLAYED_COLUMN_INDEX = 6;
	private static final int OMW_COLUMN_INDEX = 7;
	private static final int PGW_COLUMN_INDEX = 8;
	private static final int OGW_COLUMN_INDEX = 9;

	private TableViewer viewer;
	private boolean forTournamentStanding;
	private int treeStyle;
	private TableSorter tableSorter;

	class ViewContentProvider implements IStructuredContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
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

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Player) {
				Player p = (Player) element;
				switch (columnIndex) {
				case NAME_COLUMN_INDEX:
					return getText(element);
				case ID_COLUMN_INDEX:
					return p.getId();
				case PLACE_COLUMN_INDEX:
					return "";
				case POINTS_COLUMN_INDEX:
					return String.valueOf(p.getPoints());
				case WDL_COLUMN_INDEX:
					return "";
				case ROUNDS_PLAYED_COLUMN_INDEX:
					return String.valueOf(p.getGames());
				default:
					break;
				}
			} else if (element instanceof PlayerTourInfo) {
				PlayerTourInfo pi = (PlayerTourInfo) element;
				Player p = pi.getPlayer();
				switch (columnIndex) {
				case NAME_COLUMN_INDEX:
					return p.getName();
				case ID_COLUMN_INDEX:
					return p.getId();
				case PLACE_COLUMN_INDEX:
					return String.valueOf(pi.getPlace());
				case POINTS_COLUMN_INDEX:
					return String.valueOf(pi.getPoints());
				case WDL_COLUMN_INDEX:
					return getStats(pi);
				case ROUNDS_PLAYED_COLUMN_INDEX:
					return String.valueOf(pi.getRoundsPlayed());
				case GAMES_PLAYED_COLUMN_INDEX:
					int pg = pi.getGamesPlayed();
					return String.valueOf(pg);
				case OMW_COLUMN_INDEX:
					return String.valueOf(pi.getOMW());
				case PGW_COLUMN_INDEX:
					return String.valueOf(pi.getPGW());
				case OGW_COLUMN_INDEX:
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
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
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
		createColumn(NAME_COLUMN_INDEX, "Name", 140);
		createColumn(ID_COLUMN_INDEX, "PIN", 120);
		if (forTournamentStanding) {
			createColumn(PLACE_COLUMN_INDEX, "Place", 70);
			createColumn(POINTS_COLUMN_INDEX, "Points", 70);
			createColumn(WDL_COLUMN_INDEX, "W-D-L", "Wins-Draws-Looses in Matches", 70);
			createColumn(ROUNDS_PLAYED_COLUMN_INDEX, "Matches", "Complete Matches (Rounds)", 70);
			createColumn(GAMES_PLAYED_COLUMN_INDEX, "Games", "Complete Games (Usually 3 games per match)", 70);
			createColumn(OMW_COLUMN_INDEX, "OMW%", "Opponents Matches Won/Opponents Matches Played", 70);
			createColumn(PGW_COLUMN_INDEX, "PGW%", "Player Games Won/Player Games Played", 70);
			createColumn(OGW_COLUMN_INDEX, "OGW%", "Opponents Games Won/Opponents Games Played", 70);
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
				case NAME_COLUMN_INDEX:
					rc = p1.getName().compareTo(p2.getName());
					break;
				case ID_COLUMN_INDEX:
					rc = p1.getId().compareTo(p2.getId());
					break;
				case POINTS_COLUMN_INDEX:
					rc = p1.getPoints() - p2.getPoints();
					break;
				case ROUNDS_PLAYED_COLUMN_INDEX:
					rc = p1.getGames() - p2.getGames();
					break;

				default:
					rc = 0;
				}
			} else if (e1 instanceof PlayerTourInfo && e2 instanceof PlayerTourInfo) {
				PlayerTourInfo p1 = (PlayerTourInfo) e1;
				PlayerTourInfo p2 = (PlayerTourInfo) e2;
				switch (propertyIndex) {
				case NAME_COLUMN_INDEX:
					rc = compare(viewer, p1.getPlayer(), p2.getPlayer());
					break;
				case ID_COLUMN_INDEX:
					rc = compare(viewer, p1.getPlayer(), p2.getPlayer());
					break;
				case PLACE_COLUMN_INDEX:// Place
					rc = p1.getPlace() - p2.getPlace();
					break;
				case POINTS_COLUMN_INDEX:// Points
					rc = p1.getPlace() - p2.getPlace();
					break;
				case WDL_COLUMN_INDEX:// W-D-L
					rc = p1.getPlace() - p2.getPlace();
					break;
				case ROUNDS_PLAYED_COLUMN_INDEX:
					rc = p1.getRoundsPlayed() - p2.getRoundsPlayed();
					break;
				case GAMES_PLAYED_COLUMN_INDEX:
					rc = p1.getGamesPlayed() - p2.getGamesPlayed();
					break;
				case OMW_COLUMN_INDEX: // OMW
					rc = (int) Math.signum(p1.getOMW() - p2.getOMW());
					break;
				case PGW_COLUMN_INDEX: // PGW
					rc = (int) Math.signum(p1.getPGW() - p2.getPGW());
					break;
				case OGW_COLUMN_INDEX: // OGW
					rc = (int) Math.signum(p1.getOGW() - p2.getOGW());
					break;
				default:
					rc = 0;
				}
				if (rc == 0)
					rc = p1.getPlace() - p2.getPlace();
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}

	private TableColumn createColumn(final int index, String name, String tooltip, int width) {
		TableColumn column = createColumn(index, name, width);
		column.setToolTipText(tooltip);
		return column;
	}
	private TableColumn createColumn(final int index, String name, int width) {
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
		return column;
	}

	public TableViewer getViewer() {
		return viewer;
	}
}
