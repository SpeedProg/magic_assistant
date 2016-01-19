package com.reflexit.magiccards.ui.views.analyzers;

import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.SpellColorChart;

public class SpellColourPage extends AbstractDeckStatsPage {
	@Override
	public IChartGenerator createChartGenerator() {
		HashMap<String, Integer> colorStats = CardStoreUtils.countStats((ICardGroup) buildTree().getChildAtIndex(0));
		IChartGenerator gen = new SpellColorChart(colorStats.values().toArray(new Integer[0]),
				colorStats.keySet().toArray(new String[0]));
		return gen;
	}

	@Override
	protected ICardGroup buildTree() {
		return CardStoreUtils.buildSpellColorGroups(getCardStore());
	}

	@Override
	public void createPageContents(Composite parent) {
		super.createPageContents(parent);
		stats.setAutoExpandLevel(2);
	}

	@Override
	protected ICardField[] getGroupFields() {
		return new ICardField[] { MagicCardField.COST };
	}
}
