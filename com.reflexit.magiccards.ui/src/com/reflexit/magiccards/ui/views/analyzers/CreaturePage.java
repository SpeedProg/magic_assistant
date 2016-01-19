package com.reflexit.magiccards.ui.views.analyzers;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.CreatureChart;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;

public class CreaturePage extends AbstractDeckStatsPage {
	@Override
	protected void createCustomColumns(List<AbstractColumn> columns) {
		super.createCustomColumns(columns);
		columns.add(new PowerColumn(MagicCardField.POWER, "Power", "Power") {
			@Override
			public int getColumnWidth() {
				return 80;
			}
		});
		columns.add(new PowerColumn(MagicCardField.TOUGHNESS, "Toughness", "Toughness") {
			@Override
			public int getColumnWidth() {
				return 80;
			}
		});
	}

	@Override
	public void createPageContents(Composite area) {
		super.createPageContents(area);
		SashForm sashForm = (SashForm) canvas.getParent();
		sashForm.setWeights(new int[] { 50, 50 });
	}

	@Override
	protected IChartGenerator createChartGenerator() {
		Map<String, Integer> creatureStatsCount = CardStoreUtils.top(10,
				CardStoreUtils.buildCreatureStats(getCardStore()));
		IChartGenerator gen = new CreatureChart(creatureStatsCount.values().toArray(new Integer[0]),
				creatureStatsCount.keySet().toArray(new String[0]));
		return gen;
	}

	@Override
	protected ICardGroup buildTree() {
		return CardStoreUtils.buildCreatureGroups(getCardStore());
	}
}
