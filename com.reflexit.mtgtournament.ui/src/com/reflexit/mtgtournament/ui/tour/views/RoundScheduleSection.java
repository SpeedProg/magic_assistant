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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.List;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;

public class RoundScheduleSection extends TSectionPart {
	private TreeViewer viewer;

	public RoundScheduleSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
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
				return rounds.toArray();
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
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
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
					return pinfo.getP1().getPlayer().getName();
				case 3:
					return pinfo.getP2().getPlayer().getName();
				case 4:
					return PlayerRoundInfo.getWinStr(pinfo.getP1().getResult());
				case 5:
					return PlayerRoundInfo.getWinStr(pinfo.getP2().getResult());
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
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Round Schedule and Results");
		//section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		viewer = new TreeViewer(sectionClient, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
		        | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTree().setHeaderVisible(true);
		viewer.setAutoExpandLevel(2);
		createColumn(0, "Round", 80);
		createColumn(1, "Table", 60);
		createColumn(2, "Player 1", 120);
		createColumn(3, "Player 2", 120);
		createColumn(4, "Result 1", 60);
		createColumn(5, "Result 2", 60);
	}

	private void createColumn(int i, String name, int width) {
		TreeColumn col = new TreeColumn(viewer.getTree(), SWT.NONE, i);
		col.setText(name);
		col.setWidth(width);
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
