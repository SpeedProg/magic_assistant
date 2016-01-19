package com.reflexit.magiccards.ui.views.analyzers;

import java.util.Map;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.AbilityChart;
import com.reflexit.magiccards.ui.chart.IChartGenerator;

public class AbilityPage extends AbstractDeckStatsPage {
	@Override
	protected IChartGenerator createChartGenerator() {
		Map<String, Integer> affinityStatsCount = CardStoreUtils.top(10,
				CardStoreUtils.buildAbilityStats(getCardStore()));
		IChartGenerator gen = new AbilityChart(affinityStatsCount.values().toArray(new Integer[0]),
				affinityStatsCount.keySet().toArray(new String[0]));
		return gen;
	}

	@Override
	protected ICardGroup buildTree() {
		return CardStoreUtils.buildAbilityGroups(getCardStore());
	}
}
