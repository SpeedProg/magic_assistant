package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.TypeStats;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class TypeStatsControl extends AbstractDeckPage implements IDeckPage {
	ChartCanvas canvas;
	private TreeViewer stats;

	static class TypesContentProider implements ITreeContentProvider {
		public Object[] getChildren(Object element) {
			if (element instanceof CardGroup) {
				CardGroup node = (CardGroup) element;
				if (node.getChildren() == null)
					return new Object[] {};
				if (node.getChildren().size() > 0)
					return node.getChildren().toArray();
			}
			return new Object[] {};
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof CardGroup) {
				CardGroup node = (CardGroup) element;
				if (node.getChildren() == null)
					return false;
				if (node.getChildren().size() > 0)
					return true;
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		area.setLayout(new GridLayout(2, true));
		canvas = new ChartCanvas(area, SWT.BORDER);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		stats = new TreeViewer(area);
		stats.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		TreeViewerColumn col1 = new TreeViewerColumn(stats, SWT.NONE);
		col1.getColumn().setText("Type");
		col1.getColumn().setWidth(200);
		TreeViewerColumn col2 = new TreeViewerColumn(stats, SWT.NONE);
		col2.getColumn().setText("Count");
		col2.getColumn().setWidth(60);
		TreeViewerColumn col3 = new TreeViewerColumn(stats, SWT.NONE);
		col3.getColumn().setText("Percent");
		col3.getColumn().setWidth(60);
		stats.setAutoExpandLevel(3);
		stats.getTree().setHeaderVisible(true);
		stats.setContentProvider(new TypesContentProider());
		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CardGroup) {
					CardGroup node = (CardGroup) element;
					return node.getName();
				} else if (element instanceof IMagicCard) {
					IMagicCard node = (IMagicCard) element;
					return node.getType();
				}
				return null;
			}
		});
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				int count = getCount(element);
				return count + "";
			}
		});
		col3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				int count = getCount(element);
				final int total = ((CardGroup) stats.getInput()).getCount();
				float per = count / (float) total;
				return new DecimalFormat("00.00%").format(per);
			}
		});
		return area;
	}

	@Override
	public void activate() {
		super.activate();
		IChartGenerator gen = new TypeStats(buildTypeStats());
		canvas.setChartGenerator(gen);
		canvas.redraw();
		CardGroup root = buildTypes();
		stats.setInput(root);
	}

	private CardGroup buildTypes() {
		return CardStoreUtils.getInstance().buildTypeGroups(store);
	}

	protected int[] buildTypeStats() {
		return CardStoreUtils.getInstance().buildTypeStats(store);
	}

	@Override
	public String getStatusMessage() {
		ICardEventManager cardStore = store;
		String cardCountTotal = "";
		if (cardStore instanceof ICardCountable) {
			cardCountTotal = "Total cards: " + ((ICardCountable) cardStore).getCount();
		}
		return cardCountTotal;
	}

	private int getCount(Object element) {
		int count = 0;
		if (element instanceof CardGroup) {
			CardGroup node = (CardGroup) element;
			count = node.getCount();
		} else if (element instanceof IMagicCard) {
			IMagicCard node = (IMagicCard) element;
			count = ((node instanceof ICardCountable) ? ((ICardCountable) node).getCount() : 1);
		}
		return count;
	}
}
