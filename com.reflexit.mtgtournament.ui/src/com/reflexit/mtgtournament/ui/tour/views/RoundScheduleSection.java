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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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

import com.reflexit.mtgtournament.core.edit.CmdChangePairing;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo.PlayerGameResult;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.ui.tour.dialogs.ChangePartnerDialog;
import com.reflexit.mtgtournament.ui.tour.dialogs.CubePrintDialog;
import com.reflexit.mtgtournament.ui.tour.dialogs.GameResultDialog;

public class RoundScheduleSection extends TSectionPart {
	private TreeViewer viewer;
	private Button overrideButton;

	public RoundScheduleSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
		hookDoubleClickAction();
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
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
			// if (tinfo.getRound().getDateStart() == null) { // not started
			// ChangePartnerDialog d = new
			// ChangePartnerDialog(viewer.getControl().getShell());
			// d.setInput(tinfo);
			// d.open();
			// modelUpdated();
			// }
			GameResultDialog d = new GameResultDialog(viewer.getControl().getShell());
			d.setInput(tinfo);
			if (d.open() == Dialog.OK) {
				PlayerRoundInfo[] playerRoundInfo = tinfo.getPlayerRoundInfo();
				int i = 1;
				for (PlayerRoundInfo pi : playerRoundInfo) {
					pi.setWinGames(d.getWin(i), d.getLost(i), d.getDraw());
					if (d.isDrop(i)) {
						tinfo.getRound().getTournament().playerDropped(pi.getPlayer());
					}
					i++;
				}
				modelUpdated();
			}
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
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
				List<Round> rounds = t.getRounds();
				if (t.hasDraftRound() || rounds.size() == 0)
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

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getElements(element).length > 0;
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof TableInfo) {
				TableInfo pinfo = (TableInfo) element;
				switch (columnIndex) {
				case 0:
					int number = pinfo.getRound().getNumber();
					if (number == 0)
						return "Draft";
					return String.valueOf(number);
				case 1:
					return String.valueOf(pinfo.getTableNumber());
				default:
					int index = columnIndex - 2;
					PlayerRoundInfo playerInfo = pinfo.getOpponent(index);
					if (playerInfo == null)
						return "";
					String name = playerInfo.getPlayer().getName();
					String result = playerInfo.getResult().letter();
					return "(" + result + ") " + name;
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

		{
			systemColorYellow = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			systemColorGray = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		}

		@Override
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
			if (state == RoundState.IN_PROGRESS) {
				if (tableInfo == null || tableInfo.getOpponent(0).getResult() == PlayerGameResult._NONE)
					return systemColorYellow;
			}
			return null;
		}

		@Override
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
		sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));
		// sectionClient.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		viewer = new TreeViewer(sectionClient,
				SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTree().setHeaderVisible(true);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateWidgetEnablement();
			}
		});
		createColumns(2);
		// buttons
		createButtonsComposite(sectionClient);
	}

	public void createColumns(int num) {
		int x = viewer.getTree().getColumnCount();
		if (x - 2 == num)
			return;
		// System.err.println("create for " + num);
		TreeColumn[] children = viewer.getTree().getColumns();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		createColumn(0, "Round", 80);
		createColumn(1, "Table", 60);
		for (int i = 0; i < num; i++) {
			createColumn(i + 2, "Opponent " + (i + 1), 150);
		}
		viewer.refresh(true);
	}

	protected void updateWidgetEnablement() {
		ISelection selection = viewer.getSelection();
		Object el = ((IStructuredSelection) selection).getFirstElement();
		overrideButton.setEnabled(el instanceof TableInfo);
	}

	private void createButtonsComposite(Composite parent) {
		Composite buttons = toolkit.createComposite(parent);
		buttons.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		buttons.setLayout(new GridLayout());
		GridDataFactory buttonLD = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).hint(80, -1);
		Button printButton = toolkit.createButton(buttons, "Print...", SWT.PUSH);
		printButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CubePrintDialog dialog = new CubePrintDialog(getSection().getShell(), viewer.getInput());
				dialog.open();
			};
		});
		printButton.setLayoutData(buttonLD.create());
		overrideButton = toolkit.createButton(buttons, "Edit...", SWT.PUSH);
		overrideButton.setText("Edit...");
		overrideButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editPairings();
			};
		});
		overrideButton.setLayoutData(buttonLD.create());
		Button resultButton = toolkit.createButton(buttons, "Score...", SWT.NONE);
		resultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = viewer.getSelection();
				Object el = ((IStructuredSelection) selection).getFirstElement();
				if (el instanceof TableInfo) {
					TableInfo tinfo = (TableInfo) el;
					editResult(tinfo);
				}
			};
		});
		resultButton.setLayoutData(buttonLD.create());
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
		createColumns(t.getOpponentsPerGame());
		getManagedForm().getForm().reflow(false);
		super.refresh();
	}

	@Override
	public boolean setFormInput(Object input) {
		viewer.setSelection(new StructuredSelection());
		Tournament tournament = (Tournament) input;
		if (tournament != null)
			createColumns(tournament.getOpponentsPerGame());
		if (viewer.getInput() != input) {
			viewer.setInput(input);
			viewer.expandAll();
		} else {
			viewer.refresh(true);
		}
		markStale();
		return true;
	}

	private void editPairings() {
		ISelection selection = viewer.getSelection();
		Object el = ((IStructuredSelection) selection).getFirstElement();
		if (el instanceof TableInfo) {
			TableInfo tinfo = (TableInfo) el;
			if (tinfo.getRound().getDateStart() == null) {
				ChangePartnerDialog d = new ChangePartnerDialog(viewer.getControl().getShell(), tinfo);
				d.open();
				if (d.getNewPlayer() != null) {
					// update table
					if (new CmdChangePairing(tinfo, d.getNewPlayer().getPlayer()).execute())
						modelUpdated();
				}
			} else {
				MessageDialog.openError(overrideButton.getShell(), "Cannot Edit",
						"Round is started, cannot edit pairings");
			}
		}
	}
}
