package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.analyzers.GroupListControl.GroupTreeViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;

public abstract class AbstractDeckStatsPage extends AbstractDeckListPage {
	protected ChartCanvas canvas;
	protected TreeViewer stats;
	protected IAction actionRefresh;

	public AbstractDeckStatsPage() {
	}

	protected ICardField[] getGroupFields() {
		return null;
	}

	@Override
	public void createPageContents(Composite area) {
		area.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(area, SWT.HORIZONTAL);
		createMainControl(sashForm);
		canvas = new ChartCanvas(sashForm, SWT.BORDER);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm.setWeights(new int[] { 40, 60 });
		makeActions();
		loadInitial();
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		actionRefresh = new RefreshAction(this::activate);
	}

	@Override
	public void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionRefresh);
		// super.fillLocalPullDown(manager);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		// super.fillLocalToolBar(manager);
		manager.add(actionRefresh);
	}

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		stats = new GroupTreeViewer(AbstractDeckStatsPage.this.getPreferencePageId(), parent) {
			@Override
			protected void createCustomColumns(List<AbstractColumn> columns) {
				super.createCustomColumns(columns);
				AbstractDeckStatsPage.this.createCustomColumns(columns);
			}
		};
		stats.setAutoExpandLevel(3);
		return (IMagicColumnViewer) stats;
	}

	@Override
	protected Composite createTopBar(Composite composite) {
		Composite bar = super.createTopBar(composite);
		bar.setVisible(false);
		GridData gridData = (GridData) bar.getLayoutData();
		gridData.exclude = true;
		bar.getParent().layout(true, true);
		return bar;
	}

	@Override
	protected String getPreferencePageId() {
		return null;
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
	}

	@Override
	public void refresh() {
		getCardStore();
		IChartGenerator gen = createChartGenerator();
		canvas.setChartGenerator(gen);
		canvas.redraw();
		ICardGroup root = buildTree();
		stats.setInput(root);
	}

	@Override
	public void dispose() {
		canvas.dispose();
		super.dispose();
	}

	abstract protected ICardGroup buildTree();

	abstract protected IChartGenerator createChartGenerator();
}
