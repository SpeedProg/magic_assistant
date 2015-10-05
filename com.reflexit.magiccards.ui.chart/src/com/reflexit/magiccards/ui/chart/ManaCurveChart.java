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
package com.reflexit.magiccards.ui.chart;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.ActionType;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.Direction;
import org.eclipse.birt.chart.model.attribute.Fill;
import org.eclipse.birt.chart.model.attribute.Interactivity;
import org.eclipse.birt.chart.model.attribute.LegendBehaviorType;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.RiserType;
import org.eclipse.birt.chart.model.attribute.TriggerCondition;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.InteractivityImpl;
import org.eclipse.birt.chart.model.attribute.impl.TooltipValueImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.CurveFitting;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.CurveFittingImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.ActionImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.TriggerImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.layout.TitleBlock;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;

public class ManaCurveChart implements IChartGenerator {
	String[] sa = { "0", "1", "2", "3", "4", "5", "6", "7+" };
	double[] da1;
	double[] koeff = { 0.2, 6, 9, 9.6, 7, 2.6, 0.6, 0.2 };
	double[] da2;
	ChartWithAxes cwaBar;
	int count;

	public ManaCurveChart(int[] dp1) {
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

	final Fill[] colorPalette = { ColorDefinitionImpl.BLUE(), ColorDefinitionImpl.RED() };

	public final Chart create() {
		this.cwaBar = ChartWithAxesImpl.create();
		this.cwaBar.setType("Bar Chart"); //$NON-NLS-1$
		//cwaBar.setSubType("Side-by-side"); //$NON-NLS-1$
		// Plot
		// cwaBar.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = this.cwaBar.getPlot();
		// p.getClientArea().setBackground(ColorDefinitionImpl.create(255, 255, 225));
		// System.err.println(p.getColumn() + " - " + p.getRow());
		// Legend
		Legend lg = this.cwaBar.getLegend();
		lg.setPosition(Position.ABOVE_LITERAL);
		lg.setAnchor(Anchor.WEST_LITERAL);
		lg.setDirection(Direction.LEFT_RIGHT_LITERAL);
		lg.setOrientation(Orientation.HORIZONTAL_LITERAL);
		LineAttributes legendOutlineLine = lg.getOutline();
		lg.getText().getFont().setSize(16);
		legendOutlineLine.setStyle(LineStyle.SOLID_LITERAL);
		lg.getInsets().setLeft(10);
		lg.getInsets().setRight(10);
		Interactivity li = InteractivityImpl.create();
		li.setLegendBehavior(LegendBehaviorType.TOGGLE_SERIE_VISIBILITY_LITERAL);
		cwaBar.setInteractivity(li);
		// Title
		TitleBlock title = this.cwaBar.getTitle();
		title.getLabel().getCaption().setValue("Mana Curve: " + this.count + " spells");//$NON-NLS-1$
		// X-Axis
		Axis xAxisPrimary = this.cwaBar.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		// xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
		// xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		xAxisPrimary.getTitle().getCaption().setValue("X-Axis");//$NON-NLS-1$
		// xAxisPrimary.setTitlePosition(Position.BELOW_LITERAL);
		// xAxisPrimary.getLabel().getCaption().getFont().setRotation(75);
		// xAxisPrimary.setLabelPosition(Position.BELOW_LITERAL);
		// xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		// xAxisPrimary.getMajorGrid().getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
		// xAxisPrimary.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.create(64,
		// 64, 64));
		// xAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		// Y-Axis
		Axis yAxisPrimary = this.cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getLabel().getCaption().setValue("Count");//$NON-NLS-1$
		// yAxisPrimary.getLabel().getCaption().getFont().setRotation(37);
		// yAxisPrimary.setLabelPosition(Position.LEFT_LITERAL);
		// yAxisPrimary.setTitlePosition(Position.LEFT_LITERAL);
		yAxisPrimary.getTitle().getCaption().setValue("Y-Axis");//$NON-NLS-1$
		// yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		// yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		// yAxisPrimary.getMajorGrid().getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
		// yAxisPrimary.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.RED());
		// yAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		// Associate with Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(this.sa);
		NumberDataSet seriesOneValues = NumberDataSetImpl.create(this.da1);
		NumberDataSet seriesTwoValues = NumberDataSetImpl.create(this.da2);
		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);
		// Y-Series (1)
		BarSeries bs1 = createBarSeries(seriesOneValues, "This Deck");
		// Y-Series (2)
		BarSeries bs2 = createBarSeries(seriesTwoValues, "Average");
		// LineSeries ls1 = createLineSeries(seriesTwoValues);
		// sd 2
		// Axis yAxis2 = AxisImpl.create(Axis.ORTHOGONAL);
		// yAxis2.setType(AxisType.LINEAR_LITERAL);
		// xAxisPrimary.getAssociatedAxes().add(yAxis2);
		SeriesDefinition sdY2 = SeriesDefinitionImpl.create();
		// sdY2.getSeriesPalette().getEntries().clear();
		// sdY2.getSeriesPalette().getEntries().add(ColorDefinitionImpl.BLUE());
		// sdY2.getSeriesPalette().getEntries().add(ColorDefinitionImpl.RED());
		sdY2.getSeriesPalette().shift(0);
		bs1.getCurveFitting().getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
		bs2.getCurveFitting().getLineAttributes().setColor(ColorDefinitionImpl.RED());
		yAxisPrimary.getSeriesDefinitions().add(sdY2);
		sdY2.getSeries().add(bs1);
		sdY2.getSeries().add(bs2);
		return this.cwaBar;
	}

	// private LineSeries createLineSeries(NumberDataSet seriesTwoValues) {
	// LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
	// ls1.setSeriesIdentifier("Average");//$NON-NLS-1$
	// ls1.setDataSet(seriesTwoValues);
	// // ls1.getLineAttributes().setColor(ColorDefinitionImpl.RED());
	// for (int i = 0; i < ls1.getMarkers().size(); i++) {
	// (ls1.getMarkers().get(i)).setType(MarkerType.BOX_LITERAL);
	// }
	// ls1.setCurve(true);
	// ls1.getTriggers().add(
	// TriggerImpl.create(
	// TriggerCondition.ONMOUSEOVER_LITERAL,
	// ActionImpl.create(ActionType.SHOW_TOOLTIP_LITERAL,
	// TooltipValueImpl.create(500, "dph.getDisplayValue()"))));
	// return ls1;
	// }

	private BarSeries createBarSeries(NumberDataSet seriesOneValues, String name) {
		BarSeries bs1 = (BarSeries) BarSeriesImpl.create();
		bs1.setSeriesIdentifier(name);
		bs1.setDataSet(seriesOneValues);
		bs1.setRiserOutline(null);
		bs1.setRiser(RiserType.RECTANGLE_LITERAL);
		bs1.setTranslucent(true);
		CurveFitting fitting = CurveFittingImpl.create();
		bs1.setCurveFitting(fitting);
		// bs1.getTriggers().add(
		// TriggerImpl.create(TriggerCondition.ONCLICK_LITERAL,
		// ActionImpl.create(ActionType.HIGHLIGHT_LITERAL,
		// SeriesValueImpl.create(String.valueOf(bs1.getSeriesIdentifier())))));
		bs1.getTriggers().add(
				TriggerImpl.create(
						TriggerCondition.ONMOUSEOVER_LITERAL,
						ActionImpl.create(ActionType.SHOW_TOOLTIP_LITERAL,
								TooltipValueImpl.create(500, "dph.getDisplayValue()"))));
		return bs1;
	}
}
