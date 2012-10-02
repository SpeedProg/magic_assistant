package com.reflexit.magiccards.ui.views.analyzers;

import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.SpellColorChart;

public class SpellColourPage extends AbstractDeckStatsPage {
	@Override
	public IChartGenerator createChartGenerator() {
		HashMap<String, Integer> colorStats = CardStoreUtils.buildSpellColorStats(store);
		IChartGenerator gen = new SpellColorChart(colorStats.values().toArray(new Integer[0]), colorStats.keySet().toArray(new String[0]));
		return gen;
	}

	@Override
	protected CardGroup buildTree() {
		return CardStoreUtils.buildSpellColorGroups(store);
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		stats.setAutoExpandLevel(2);
		return area;
	}

	@Override
	protected ICardField[] getGroupFields() {
		return new ICardField[] { MagicCardField.COST };
	}
}
