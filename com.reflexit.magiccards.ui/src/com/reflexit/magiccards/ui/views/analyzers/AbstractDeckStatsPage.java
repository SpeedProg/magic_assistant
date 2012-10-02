package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;

public abstract class AbstractDeckStatsPage extends AbstractDeckPage {
	protected ChartCanvas canvas;
	protected TreeViewer stats;

	public AbstractDeckStatsPage() {
	}

	protected ICardField[] getGroupFields() {
		return null;
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		area.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(area, SWT.HORIZONTAL);
		canvas = new ChartCanvas(sashForm, SWT.BORDER);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		createCardsTree(sashForm);
		sashForm.setWeights(new int[] { 60, 40 });
		return area;
	}

	AbstractMagicCardsListControl listControl;

	public void createCardsTree(Composite parent) {
		listControl = doGetMagicCardListControl();
		listControl.createPartControl(parent);
		// listControl.getFilter().setGroupFields(getGroupFields());
		stats = (TreeViewer) listControl.getManager().getViewer();
		// stats = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL |
		// SWT.V_SCROLL | SWT.BORDER);
		// stats.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		// TreeViewerColumn col1 = new TreeViewerColumn(stats, SWT.NONE);
		// col1.getColumn().setText("Name");
		// col1.getColumn().setWidth(200);
		// TreeViewerColumn col2 = new TreeViewerColumn(stats, SWT.NONE);
		// col2.getColumn().setText("Count");
		// col2.getColumn().setWidth(60);
		// TreeViewerColumn col3 = new TreeViewerColumn(stats, SWT.NONE);
		// col3.getColumn().setText("Percent");
		// col3.getColumn().setWidth(60);
		stats.setAutoExpandLevel(3);
		stats.getTree().setHeaderVisible(true);
		// stats.setContentProvider(new GroupContentProvider());
		stats.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					view.getSite().getSelectionProvider().setSelection(sel);
				}
			}
		});
		// col1.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof CardGroup) {
		// CardGroup node = (CardGroup) element;
		// return node.getName();
		// } else if (element instanceof IMagicCard) {
		// IMagicCard node = (IMagicCard) element;
		// return node.getName();
		// }
		// return null;
		// }
		// });
		// col2.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// int count = getCount(element);
		// return count + "";
		// }
		// });
		// col3.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// int count = getCount(element);
		// final int total = ((CardGroup) stats.getInput()).getCount();
		// float per = count / (float) total;
		// return new DecimalFormat("00.00%").format(per);
		// }
		// });
	}

	public GroupListControl doGetMagicCardListControl() {
		return new GroupListControl(view) {
			@Override
			public IMagicColumnViewer createViewerManager() {
				return new GroupTreeManager(getPreferencePageId()) {
					@Override
					protected ColumnCollection doGetColumnCollection(String prefPageId) {
						return new GroupTreeColumnCollection() {
							@Override
							protected void createColumns() {
								createCustomColumns(columns);
							}
						};
					}
				};
			}
		};
	}

	protected void createCustomColumns(List<AbstractColumn> columns) {
		columns.add(new GroupColumn(false));
		columns.add(new CountColumn());
		columns.add(new GenColumn(MagicCardFieldPhysical.COUNT, "Percent") {
			@Override
			public String getText(Object element) {
				int count = getCount(element);
				final int total = ((CardGroup) stats.getInput()).getCount();
				float per = count / (float) total;
				return new DecimalFormat("00.0%").format(per);
			}

			@Override
			public int getColumnWidth() {
				return 45;
			}
		});
	}

	@Override
	public void activate() {
		super.activate();
		IChartGenerator gen = createChartGenerator();
		canvas.setChartGenerator(gen);
		canvas.redraw();
		CardGroup root = buildTree();
		stats.setInput(root);
		// listControl.getFilteredStore().clear();
		// listControl.getFilteredStore().addAll(store);
		// listControl.reloadData();
	}

	abstract protected CardGroup buildTree();

	abstract protected IChartGenerator createChartGenerator();
}
