package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.List;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;

public class RoundSection extends TSectionPart {
	private TableViewer viewer;

	public RoundSection(ManagedForm managedForm) {
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
				return fillElements(rounds.toArray());
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
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof TableInfo) {
				TableInfo pinfo = (TableInfo) element;
				switch (columnIndex) {
				case 0:
					return String.valueOf(pinfo.getTableNumber());
				case 1:
					return pinfo.getP1().getPlayer().getName();
				case 2:
					return pinfo.getP2().getPlayer().getName();
				case 3:
					return PlayerRoundInfo.getWinStr(pinfo.getP1().getResult());
				case 4:
					return PlayerRoundInfo.getWinStr(pinfo.getP2().getResult());
				case 5:
					int number = pinfo.getRound().getNumber();
					if (number == 0)
						return "Draft";
					return String.valueOf(number);
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
		viewer = new TableViewer(sectionClient, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
		        | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTable().setHeaderVisible(true);
		createColumn(0, "Table", 60);
		createColumn(1, "Player 1", 120);
		createColumn(2, "Player 2", 120);
		createColumn(3, "Result 1", 60);
		createColumn(4, "Result 2", 60);
		createColumn(5, "Round", 80);
	}

	private void createColumn(int i, String name, int width) {
		TableColumn col = new TableColumn(viewer.getTable(), SWT.NONE, i);
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
