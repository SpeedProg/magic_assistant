package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.SpellColors;

public class ColorControl extends AbstractDeckPage implements IDeckPage {
	ChartCanvas spellColorCanvas;

	@Override
	public Composite createContents(Composite parent) {
		area = super.createContents(parent);
		area.setLayout(new GridLayout(2, false));
		spellColorCanvas = new ChartCanvas(area, SWT.BORDER);
		spellColorCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		return area;
	}

	@Override
	public void updateFromStore() {
		if (store == null)
			return;
		Collection<CardGroup> groups = buildSpellColorStats();
		int values[] = new int[groups.size()];
		String labels[] = new String[groups.size()];
		int i = 0;
		for (Iterator iterator = groups.iterator(); iterator.hasNext(); i++) {
			CardGroup cardGroup = (CardGroup) iterator.next();
			values[i] = cardGroup.getCount();
			labels[i] = cardGroup.getName();
		}
		IChartGenerator gen = new SpellColors(values, labels);
		spellColorCanvas.setChartGenerator(gen);
		spellColorCanvas.redraw();
	}

	protected Collection<CardGroup> buildSpellColorStats() {
		HashMap<CardGroup, CardGroup> groupsList = new HashMap();
		for (Object element : store) {
			IMagicCard elem = (IMagicCard) element;
			if (elem.getType().contains("Land"))
				continue;
			String name = Colors.getColorName(elem.getCost());
			CardGroup g = new CardGroup(MagicCardField.COST, name);
			if (groupsList.containsKey(g)) {
				groupsList.get(g).addCount(1);
			} else {
				g.addCount(1);
				groupsList.put(g, g);
			}
		}
		return groupsList.keySet();
	}

	@Override
	public String getStatusMessage() {
		ICardEventManager cardStore = store;
		String cardCountTotal = "";
		if (cardStore instanceof ICardCountable) {
			cardCountTotal = "Total cards: " + ((ICardCountable) cardStore).getCount();
		}
		return cardCountTotal;
	}
}
