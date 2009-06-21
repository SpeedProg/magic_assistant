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

import com.reflexit.mtgtournament.core.model.Cube;
import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.Tournament;

public class PlayersListComposite extends Composite {
	private TableViewer viewer;
	private boolean hasColumns;
	private int treeStyle;
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Tournament) {
				Tournament t = (Tournament) parent;
				return t.getPlayers().toArray();
			}
			if (parent instanceof Cube) {
				return ((Cube) parent).getPlayers().toArray();
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
		this.hasColumns = hasColumns;
		setLayout(new GridLayout());
		createBody(this);
		getViewer().getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	protected void createBody(Composite parent) {
		// players table
		this.viewer = (new TableViewer(parent, treeStyle | SWT.H_SCROLL | SWT.V_SCROLL));
		getViewer().setContentProvider(new ViewContentProvider());
		getViewer().setLabelProvider(new ViewLabelProvider());
		if (hasColumns) {
			createColumn(0, "Name", 120);
			createColumn(1, "PIN", 120);
		}
	}

	private void createColumn(int i, String name, int width) {
		TableColumn col = new TableColumn(viewer.getTable(), SWT.NONE, i);
		col.setText(name);
		col.setWidth(width);
	}

	public TableViewer getViewer() {
		return viewer;
	}
}
