package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.ManaCurveChart;

public class ManaCurvePage extends AbstractDeckStatsPage {
	@Override
	protected IChartGenerator createChartGenerator() {
		IChartGenerator gen = new ManaCurveChart(CardStoreUtils.buildManaCurveStats(store));
		return gen;
	}

	@Override
	protected ICardGroup buildTree() {
		return CardStoreUtils.buildManaCurveGroup(store);
	}

	@Override
	public Control createContents(Composite parent) {
		Control area = super.createContents(parent);
		stats.setAutoExpandLevel(2);
		return area;
	}

	@Override
	protected ICardField[] getGroupFields() {
		return new ICardField[] { MagicCardField.CMC };
	}
}
