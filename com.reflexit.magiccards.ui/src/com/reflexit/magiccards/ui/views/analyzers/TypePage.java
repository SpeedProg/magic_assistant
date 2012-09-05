package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.TypeChart;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckStatsPage;

public class TypePage extends AbstractDeckStatsPage {
	@Override
	protected IChartGenerator createChartGenerator() {
		IChartGenerator gen = new TypeChart(CardStoreUtils.buildTypeStats(store));
		return gen;
	}

	@Override
	protected CardGroup buildTree() {
		return CardStoreUtils.buildTypeGroups(store);
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		stats.setAutoExpandLevel(4);
		return area;
	}
}
