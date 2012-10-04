package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import com.reflexit.magiccards.ui.views.columns.GenColumn;

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
		stats.setAutoExpandLevel(3);
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
	}

	public GroupListControl doGetMagicCardListControl() {
		return new GroupListControl(view) {
			@Override
			public IMagicColumnViewer createViewerManager() {
				return new GroupTreeManager(getPreferencePageId()) {
					@Override
					protected void createCustomColumns(ArrayList<AbstractColumn> columns) {
						super.createCustomColumns(columns);
						AbstractDeckStatsPage.this.createCustomColumns(columns);
					}
				};
			}
		};
	}

	protected void createCustomColumns(List<AbstractColumn> columns) {
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
				return 52;
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
