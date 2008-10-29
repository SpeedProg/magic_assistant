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
package com.alena.birt;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * @author Alena
 *
 */
public class ChartCanvas extends Canvas {
	private IDeviceRenderer render;
	protected Image cachedImage;
	private Chart chart;
	private GeneratedChartState state;

	public ChartCanvas(Composite parent, int style) {
		super(parent, style);
		// initialize the SWT rendering device
		try {
			PluginSettings ps = PluginSettings.instance();
			this.render = ps.getDevice("dv.SWT");
		} catch (ChartException pex) {
			Activator.log(pex);
		}
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Composite co = (Composite) e.getSource();
				final Rectangle rect = co.getClientArea();
				if (ChartCanvas.this.cachedImage == null) {
					drawToCachedImage(rect);
				}
				if (ChartCanvas.this.cachedImage != null)
					e.gc.drawImage(ChartCanvas.this.cachedImage, 0, 0, ChartCanvas.this.cachedImage.getBounds().width,
					        ChartCanvas.this.cachedImage.getBounds().height, 0, 0, rect.width, rect.height);
			}
		});
	}

	public void setChart(Chart chart) {
		this.chart = chart;
	}

	public void setChartGenerator(IChartGenerator chart) {
		this.chart = chart.create();
	}

	private void buildChart() {
		Point size = getSize();
		Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
		int resolution = this.render.getDisplayServer().getDpiResolution();
		bo.scale(72d / resolution);
		try {
			Generator gr = Generator.instance();
			this.state = gr.build(this.render.getDisplayServer(), this.chart, bo, null, null, null);
		} catch (ChartException ex) {
			Activator.log(ex);
		}
	}

	public void drawToCachedImage(Rectangle size) {
		GC gc = null;
		try {
			if (this.chart == null)
				return;
			if (this.cachedImage != null)
				this.cachedImage.dispose();
			this.cachedImage = new Image(Display.getCurrent(), size);
			gc = new GC(this.cachedImage);
			this.render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
			Generator gr = Generator.instance();
			buildChart();
			gr.render(this.render, this.state);
		} catch (ChartException ex) {
			Activator.log(ex);
		} finally {
			if (gc != null)
				gc.dispose();
		}
	}
}
