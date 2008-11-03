/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package com.alena.birt;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.RiserType;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;

public class ManaCurve implements IChartGenerator {
	String[] sa = { "0", "1", "2", "3", "4", "5", "6", "7+", "X" };
	double[] da1;// { 0, 2, 4, 10, 10, 4, 6, 0, 1 };
	double[] koeff = { 0.2, 4, 7.2, 10, 8, 4, 2, 0.2, 0.4 };
	double[] da2;
	ChartWithAxes cwaBar;
	int count;

	public ManaCurve(int[] dp1) {
		this.da1 = new double[dp1.length];
		this.count = 0;
		for (int i = 0; i < dp1.length; i++) {
			this.da1[i] = dp1[i];
			this.count += dp1[i];
		}
		double mul = this.count / 36.0;
		this.da2 = new double[this.koeff.length];
		for (int i = 0; i < this.koeff.length; i++) {
			this.da2[i] = Math.round(mul * this.koeff[i]);
		}
	}

	public final Chart create() {
		this.cwaBar = ChartWithAxesImpl.create();
		this.cwaBar.setType("Bar Chart"); //$NON-NLS-1$
		//cwaBar.setSubType("Side-by-side"); //$NON-NLS-1$
		// Plot
		//cwaBar.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = this.cwaBar.getPlot();
		//p.getClientArea().setBackground(ColorDefinitionImpl.create(255, 255, 225));
		// Legend
		Legend lg = this.cwaBar.getLegend();
		LineAttributes lia = lg.getOutline();
		lg.getText().getFont().setSize(16);
		lia.setStyle(LineStyle.SOLID_LITERAL);
		lg.getInsets().setLeft(10);
		lg.getInsets().setRight(10);
		// Title
		this.cwaBar.getTitle().getLabel().getCaption().setValue("Mana Curve: " + this.count);//$NON-NLS-1$
		// X-Axis
		Axis xAxisPrimary = this.cwaBar.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		//xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
		//xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		xAxisPrimary.getTitle().getCaption().setValue("X-Axis");//$NON-NLS-1$
		//xAxisPrimary.setTitlePosition(Position.BELOW_LITERAL);
		//	xAxisPrimary.getLabel().getCaption().getFont().setRotation(75);
		//xAxisPrimary.setLabelPosition(Position.BELOW_LITERAL);
		//xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		//xAxisPrimary.getMajorGrid().getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
		//xAxisPrimary.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.create(64, 64, 64));
		//xAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		// Y-Axis
		Axis yAxisPrimary = this.cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getLabel().getCaption().setValue("Count");//$NON-NLS-1$
		//yAxisPrimary.getLabel().getCaption().getFont().setRotation(37);
		//yAxisPrimary.setLabelPosition(Position.LEFT_LITERAL);
		//yAxisPrimary.setTitlePosition(Position.LEFT_LITERAL);
		yAxisPrimary.getTitle().getCaption().setValue("Y-Axis");//$NON-NLS-1$
		//yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		//yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		//yAxisPrimary.getMajorGrid().getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
		//yAxisPrimary.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.RED());
		//yAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		// Associate with Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(this.sa);
		NumberDataSet seriesOneValues = NumberDataSetImpl.create(this.da1);
		NumberDataSet seriesTwoValues = NumberDataSetImpl.create(this.da2);
		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		//sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);
		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE.createOrthogonalSampleData();
		//sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);
		OrthogonalSampleData sdOrthogonal2 = DataFactory.eINSTANCE.createOrthogonalSampleData();
		//sdOrthogonal2.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal2.setSeriesDefinitionIndex(1);
		sd.getOrthogonalSampleData().add(sdOrthogonal2);
		this.cwaBar.setSampleData(sd);
		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);
		// Y-Series (1)
		BarSeries bs1 = (BarSeries) BarSeriesImpl.create();
		bs1.setSeriesIdentifier("This Deck");//$NON-NLS-1$
		bs1.setDataSet(seriesOneValues);
		bs1.setRiserOutline(null);
		bs1.setRiser(RiserType.RECTANGLE_LITERAL);
		// Y-Series (2)
		LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
		ls1.setSeriesIdentifier("Average");//$NON-NLS-1$
		ls1.setDataSet(seriesTwoValues);
		ls1.getLineAttributes().setColor(ColorDefinitionImpl.GREEN());
		for (int i = 0; i < ls1.getMarkers().size(); i++) {
			((Marker) ls1.getMarkers().get(i)).setType(MarkerType.BOX_LITERAL);
		}
		ls1.setCurve(true);
		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeriesPalette().shift(0);
		sdY.getSeries().add(bs1);
		sdY.getSeries().add(ls1);
		return this.cwaBar;
	}
}
