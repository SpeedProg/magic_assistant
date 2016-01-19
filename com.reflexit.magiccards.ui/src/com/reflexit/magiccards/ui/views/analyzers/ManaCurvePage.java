package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.ManaCurveChart;

public class ManaCurvePage extends AbstractDeckStatsPage {
	@Override
	protected IChartGenerator createChartGenerator() {
		IChartGenerator gen = new ManaCurveChart(CardStoreUtils.buildManaCurveStats(getCardStore()));
		return gen;
	}

	@Override
	protected ICardGroup buildTree() {
		return CardStoreUtils.buildManaCurveGroup(getCardStore());
	}

	@Override
	public void createPageContents(Composite parent) {
		super.createPageContents(parent);
		stats.setAutoExpandLevel(2);
	}

	@Override
	protected ICardField[] getGroupFields() {
		return new ICardField[] { MagicCardField.CMC };
	}
}
