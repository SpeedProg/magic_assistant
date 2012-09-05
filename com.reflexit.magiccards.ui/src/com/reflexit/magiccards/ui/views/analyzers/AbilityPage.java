package com.reflexit.magiccards.ui.views.analyzers;

import java.util.HashMap;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.AbilityChart;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckStatsPage;

public class AbilityPage extends AbstractDeckStatsPage {
	@Override
	protected IChartGenerator createChartGenerator() {
		HashMap<String, Integer> affinityStatsCount = CardStoreUtils.buildAbilityStats(store);
		IChartGenerator gen = new AbilityChart(affinityStatsCount.values().toArray(new Integer[0]), affinityStatsCount.keySet().toArray(
				new String[0]));
		return gen;
	}

	@Override
	protected CardGroup buildTree() {
		return CardStoreUtils.buildAbilityGroups(store);
	}
}
