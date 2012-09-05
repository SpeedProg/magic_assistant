package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.ManaCurveChart;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckStatsPage;

public class ManaCurvePage extends AbstractDeckStatsPage {
	@Override
	protected IChartGenerator createChartGenerator() {
		IChartGenerator gen = new ManaCurveChart(CardStoreUtils.buildManaCurveStats(store));
		return gen;
	}

	@Override
	protected CardGroup buildTree() {
		return CardStoreUtils.buildManaCurveGroup(store);
	}
}
