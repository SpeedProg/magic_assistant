/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.chart;

import org.eclipse.birt.chart.device.ICallBackNotifier;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.CallBackValue;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.StyledComponent;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.FontDefinitionImpl;
import org.eclipse.birt.chart.style.BaseStyleProcessor;
import org.eclipse.birt.chart.style.IStyle;
import org.eclipse.birt.chart.style.SimpleStyle;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;

/**
 * @author Alena
 * 
 */
public class ChartCanvas extends Canvas {
	private IDeviceRenderer render;
	protected Image cachedImage;
	private Chart chart;
	private GeneratedChartState state;
	private CallBackNotifier notifier;
	private boolean needRebuild = false;
	private boolean needRender = false;

	public ChartCanvas(Composite parent, int style) {
		super(parent, style);
		notifier = new CallBackNotifier();
		// initialize the SWT rendering device
		try {
			PluginSettings ps = PluginSettings.instance();
			this.render = ps.getDevice("dv.SWT");
			render.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, notifier);
		} catch (ChartException pex) {
			Activator.log(pex);
		}
		createCopyImageMenu();
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Composite co = (Composite) e.getSource();
				final Rectangle chartBounds = co.getClientArea();
				if (ChartCanvas.this.cachedImage != null) {
					e.gc.drawImage(ChartCanvas.this.cachedImage, 0, 0);
					if (cachedImage.getBounds().width != chartBounds.width
							|| cachedImage.getBounds().height != chartBounds.height) {
						needRebuild = true;
					}
				} else {
					needRebuild = true;
				}
				if (needRebuild || needRender) {
					drawToCachedImage(chartBounds, e.gc);
					if (cachedImage != null)
						e.gc.drawImage(ChartCanvas.this.cachedImage, 0, 0);
				}
			}
		});
	}

	public void createCopyImageMenu() {
		Menu menu = new Menu(this);
		this.setMenu(menu);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText("Copy Image");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Image image = getImage();
				if (image == null)
					return;
				Clipboard clipboard = new Clipboard(Display.getDefault());
				ImageTransfer imageTransfer = ImageTransfer.getInstance();
				clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[] { imageTransfer });
			}
		});
	}

	public void setChart(Chart chart) {
		this.chart = chart;
		this.cachedImage = null;
	}

	public void setChartGenerator(IChartGenerator gen) {
		setChart(gen.create());
	}

	private void buildChart(GC gc) {
		Point size = getSize();
		Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
		int resolution = this.render.getDisplayServer().getDpiResolution();
		bo.scale(72d / resolution);
		try {
			Generator gr = Generator.instance();
			BaseStyleProcessor externalProcessor = createExternalStyleProcessor(gc);
			this.state = gr.build(this.render.getDisplayServer(), this.chart, bo, null, null,
					externalProcessor);
			this.state.getRunTimeContext().setActionRenderer(new ManaCurveActionRenderer());
		} catch (ChartException ex) {
			Activator.log(ex);
		}
	}

	public BaseStyleProcessor createExternalStyleProcessor(GC gc) {
		final Color fg = gc.getForeground();
		final FontDefinition fd = FontDefinitionImpl.createEmpty();
		FontData swtfontdata = getFont().getFontData()[0];
		fd.setSize(swtfontdata.getHeight());
		fd.setName(swtfontdata.getName());
		final SimpleStyle style = new SimpleStyle();
		style.setColor(ColorDefinitionImpl.create(fg.getRed(), fg.getGreen(), fg.getBlue()));
		style.setFont(fd);
		BaseStyleProcessor externalProcessor = new BaseStyleProcessor() {
			@Override
			public IStyle getStyle(Chart model, StyledComponent name) {
				return style;
			}
		};
		return externalProcessor;
	}

	public synchronized void drawToCachedImage(Rectangle size, GC gcOrig) {
		GC gc = null;
		try {
			if (this.chart == null)
				return;
			// prepare image
			if (this.cachedImage != null)
				this.cachedImage.dispose();
			else
				needRebuild = true;
			this.cachedImage = new Image(Display.getCurrent(), size);
			// prepare gc
			gc = new GC(this.cachedImage);
			gc.setBackground(gcOrig.getBackground());
			gc.setForeground(gcOrig.getForeground());
			gc.setFont(gcOrig.getFont());
			// Fills background.
			gc.fillRectangle(0, 0, size.width + 1, size.height + 1);
			// rebuild
			this.render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
			if (needRebuild) {
				buildChart(gc);
				needRebuild = false;
			}
			// render
			Generator gr = Generator.instance();
			gr.render(this.render, this.state);
			needRender = false;
		} catch (ChartException ex) {
			Activator.log(ex);
		} finally {
			if (gc != null)
				gc.dispose();
		}
	}

	class CallBackNotifier implements ICallBackNotifier {
		public Chart getDesignTimeModel() {
			return chart;
		}

		public void callback(Object arg0, Object arg1, CallBackValue value) {
			MessageBox mb = new MessageBox(getShell());
			mb.setMessage(value.getIdentifier());
			mb.open();
		}

		public Chart getRunTimeModel() {
			return state.getChartModel();
		}

		public Object peerInstance() {
			return ChartCanvas.this;
		}

		public void regenerateChart() {
			needRebuild = true;
			redraw();
		}

		public void repaintChart() {
			needRender = true;
			redraw();
		}
	}

	public Image getImage() {
		return cachedImage;
	}

	@Override
	public void dispose() {
		render.dispose();
		chart = null;
		super.dispose();
	}
}
