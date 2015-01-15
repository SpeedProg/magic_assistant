package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.TypeChart;

public class TypePage extends AbstractDeckStatsPage {
	@Override
	protected ICardField[] getGroupFields() {
		return new ICardField[] { MagicCardField.TYPE };
	}

	@Override
	protected IChartGenerator createChartGenerator() {
		IChartGenerator gen = new TypeChart(CardStoreUtils.buildTypeStats(store));
		return gen;
	}

	@Override
	protected ICardGroup buildTree() {
		return CardStoreUtils.buildTypeGroups(store);
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		stats.setAutoExpandLevel(4);
		return area;
	}
}
