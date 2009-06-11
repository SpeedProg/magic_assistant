package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

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
		createCopyImageMenu();
	}

	private void createCopyImageMenu() {
	    Menu menu = new Menu(canvas);
		canvas.setMenu(menu);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText("Copy Image");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Image image = canvas.getImage();
				Clipboard clipboard = new Clipboard(Display.getDefault());
				ImageTransfer imageTransfer = ImageTransfer.getInstance();
				clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[] { imageTransfer });
			}
		});
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
