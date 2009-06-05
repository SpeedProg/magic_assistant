package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.chart.ManaCurve;

public class ManaCurveControl {
	ChartCanvas canvas;
	//SwtInteractivityViewer canvas;
	ICardStore store;

	public ManaCurveControl(Composite parent, int style) {
		canvas = new ChartCanvas(parent, style);
		//canvas = new SwtInteractivityViewer(parent, style);
	}

	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
	};

	public ICardEventManager getCardStore() {
		return store;
	}

	public void updateChart() {
		if (store == null)
			return;
		IChartGenerator gen = new ManaCurve(buildManaCurve());
		canvas.setChartGenerator(gen);
		canvas.redraw();
	}

	public Control getControl() {
		return canvas;
	}

	protected int[] buildManaCurve() {
		return CardStoreUtils.getInstance().buildManaCurve(store);
	}

	public String getStatusMessage() {
		ICardEventManager cardStore = store;
		String cardCountTotal = "";
		if (cardStore instanceof ICardCountable) {
			cardCountTotal = "Total cards: " + ((ICardCountable) cardStore).getCount();
		}
		return cardCountTotal;
	}
}
