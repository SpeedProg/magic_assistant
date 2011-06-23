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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.ui.tour.dialogs.CubePrintDialog;
import com.reflexit.mtgtournament.ui.tour.dialogs.GameResultDialog;

public class RoundScheduleSection extends TSectionPart {
	private TreeViewer viewer;

	public RoundScheduleSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
		hookDoubleClickAction();
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.isEmpty())
					return;
				editResult(selection.getFirstElement());
			}
		});
	}

	/**
	 * @param firstElement
	 */
	protected void editResult(Object element) {
		if (element instanceof TableInfo) {
			TableInfo tinfo = (TableInfo) element;
			if (tinfo.getRound().getNumber() == 0)
				return; // cannot edit draft
			GameResultDialog d = new GameResultDialog(viewer.getControl().getShell());
			d.setInput(tinfo);
			if (d.open() == Dialog.OK) {
				PlayerRoundInfo p1 = tinfo.getPlayerInfo(1);
				p1.setWinGames(d.getWin1(), d.getWin2(), d.getDraw());
				PlayerRoundInfo p2 = tinfo.getPlayerInfo(2);
				p2.setWinGames(d.getWin2(), d.getWin1(), d.getDraw());
				if (d.isDrop1()) {
					tinfo.getRound().getTournament().playerDropped(p1.getPlayer());
				}
				if (d.isDrop2()) {
					tinfo.getRound().getTournament().playerDropped(p2.getPlayer());
				}
				modelUpdated();
			}
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
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
			} else if (parent instanceof Round[]) {
				return ((Object[]) parent);
			} else if (parent instanceof Object[]) {
				return fillElements((Object[]) parent);
			} else if (parent instanceof Round) {
				return fillElements(new Object[] { parent });
			}
			return new Object[0];
		}

		private Object[] fillElements(Object[] array) {
			ArrayList<TableInfo> tinfo = new ArrayList<TableInfo>();
			for (Object element : array) {
				if (element instanceof Round) {
					Round round = (Round) element;
					tinfo.addAll(round.getTables());
				}
			}
			return tinfo.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getElements(element).length > 0;
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof TableInfo) {
				TableInfo pinfo = (TableInfo) element;
				switch (columnIndex) {
				case 1:
					return String.valueOf(pinfo.getTableNumber());
				case 2:
					return pinfo.getPlayerInfo(1).getPlayer().getName();
				case 3:
					return pinfo.getPlayerInfo(2).getPlayer().getName();
				case 4:
					return PlayerRoundInfo.getWinStr(pinfo.getPlayerInfo(1).getResult());
				case 5:
					return PlayerRoundInfo.getWinStr(pinfo.getPlayerInfo(2).getResult());
				case 0:
					int number = pinfo.getRound().getNumber();
					if (number == 0)
						return "Draft";
					return String.valueOf(number);
				}
			} else if (element instanceof Round) {
				Round round = (Round) element;
				switch (columnIndex) {
				case 0:
					int number = round.getNumber();
					if (number == 0)
						return "Draft";
					return "Round " + String.valueOf(number);
				default:
					return "";
				}
			}
			return "";
		}

		private Color systemColorYellow;
		private Color systemColorGray;
		private Color systemColorBlue;
		{
			systemColorYellow = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			systemColorGray = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			systemColorBlue = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
		}

		public Color getBackground(Object element, int columnIndex) {
			Round round;
			TableInfo tableInfo = null;
			if (element instanceof Round) {
				round = (Round) element;
			} else if (element instanceof TableInfo) {
				tableInfo = (TableInfo) element;
				round = tableInfo.getRound();
			} else {
				return null;
			}
			RoundState state = round.getState();
			switch (state) {
			case IN_PROGRESS:
				if (tableInfo == null || tableInfo.getPlayerInfo(1).getResult() == null)
					return systemColorYellow;
				break;
			}
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			Round round;
			TableInfo tableInfo = null;
			if (element instanceof Round) {
				round = (Round) element;
			} else if (element instanceof TableInfo) {
				tableInfo = (TableInfo) element;
				round = tableInfo.getRound();
			} else {
				return null;
			}
			RoundState state = round.getState();
			switch (state) {
			case CLOSED:
				return systemColorGray;
			default:
				return null;
			}
		}
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Round Schedule and Results");
		// section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		viewer = new TreeViewer(sectionClient, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTree().setHeaderVisible(true);
		createColumn(0, "Round", 80);
		createColumn(1, "Table", 60);
		createColumn(2, "Player 1", 120);
		createColumn(3, "Player 2", 120);
		createColumn(4, "Result 1", 60);
		createColumn(5, "Result 2", 60);
		// buttons
		createButtonsComposite(sectionClient);
	}

	private void createButtonsComposite(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		buttons.setLayout(new GridLayout());
		Button printButton = new Button(buttons, SWT.NONE);
		printButton.setText("Print...");
		printButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CubePrintDialog dialog = new CubePrintDialog(getSection().getShell(), viewer.getInput());
				dialog.open();
			};
		});
		printButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).create());
	}

	private void createColumn(int i, String name, int width) {
		TreeColumn col = new TreeColumn(viewer.getTree(), SWT.NONE, i);
		col.setText(name);
		col.setWidth(width);
	}

	protected void modelUpdated() {
		save();
		reload();
	}

	@Override
	public void refresh() {
		viewer.refresh(true);
		Tournament t = (Tournament) viewer.getInput();
		List<Round> rounds = t.getRounds();
		for (Round round : rounds) {
			if (round.getState() == RoundState.IN_PROGRESS) {
				viewer.setExpandedState(round, true);
			}
		}
		getManagedForm().getForm().reflow(false);
		super.refresh();
	}

	@Override
	public boolean setFormInput(Object input) {
		if (viewer.getInput() != input) {
			viewer.setInput(input);
			viewer.expandAll();
		} else {
			viewer.refresh(true);
		}
		markStale();
		return true;
	}
}
