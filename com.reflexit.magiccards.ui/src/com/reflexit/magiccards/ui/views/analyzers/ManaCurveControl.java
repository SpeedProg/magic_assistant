package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.ManaCurve;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class ManaCurveControl extends AbstractDeckPage implements IDeckPage {
	ChartCanvas canvas;

	@Override
	public Composite createContents(Composite parent) {
		canvas = new ChartCanvas(parent, SWT.BORDER);
		return canvas;
	}

	@Override
	public void updateFromStore() {
		if (store == null)
			return;
		IChartGenerator gen = new ManaCurve(buildManaCurve());
		canvas.setChartGenerator(gen);
		canvas.redraw();
	}

	@Override
	public Control getControl() {
		return canvas;
	}

	protected int[] buildManaCurve() {
		return CardStoreUtils.getInstance().buildManaCurve(store);
	}

	@Override
	public String getStatusMessage() {
		ICardEventManager cardStore = store;
		String cardCountTotal = "";
		if (cardStore instanceof ICardCountable) {
			float acost = CardStoreUtils.getInstance().getAverageManaCost(store);
			int count = ((ICardCountable) cardStore).getCount();
			cardCountTotal = "Total cards: " + count + ". Average cost: " + acost;
		}
		return cardCountTotal;
	}
}
