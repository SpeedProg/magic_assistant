package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;

public abstract class AbstractDeckStatsPage extends AbstractDeckListPage {
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

	@Override
	public void createCardsTree(Composite parent) {
		super.createCardsTree(parent);
		stats = (TreeViewer) getListControl().getManager().getViewer();
		stats.setAutoExpandLevel(3);
	}

	@Override
	public GroupListControl doGetMagicCardListControl() {
		return new GroupListControl(view) {
			@Override
			public IMagicColumnViewer createViewerManager() {
				return new GroupTreeManager(getPreferencePageId()) {
					@Override
					protected void createCustomColumns(List<AbstractColumn> columns) {
						super.createCustomColumns(columns);
						AbstractDeckStatsPage.this.createCustomColumns(columns);
					}
				};
			}

			@Override
			public void fillLocalToolBar(IToolBarManager mm) {
				// mm.add(actionShowPrefs);
				// mm.add(actionShowFind);
			}

			@Override
			public void fillLocalPullDown(IMenuManager mm) {
				// mm.add(actionShowFind);
			}
		};
	}

	protected void createCustomColumns(List<AbstractColumn> columns) {
		columns.add(new GenColumn(MagicCardField.COUNT, "Percent") {
			@Override
			public String getText(Object element) {
				int count = getCount(element);
				final int total = ((CardGroup) stats.getInput()).getCount();
				float per = total == 0 ? 0 : (count / (float) total);
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
		ICardGroup root = buildTree();
		stats.setInput(root);
		// listControl.getFilteredStore().clear();
		// listControl.getFilteredStore().addAll(store);
		// listControl.reloadData();
	}

	@Override
	public void dispose() {
		canvas.dispose();
		super.dispose();
	}

	abstract protected ICardGroup buildTree();

	abstract protected IChartGenerator createChartGenerator();
}
