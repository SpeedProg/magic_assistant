package com.reflexit.magiccards.ui.chart;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.DataPoint;
import org.eclipse.birt.chart.model.attribute.DataPointComponent;
import org.eclipse.birt.chart.model.attribute.DataPointComponentType;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.DataPointComponentImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
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
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

public class SpellColors implements IChartGenerator {
	private double[] series;
	private String[] labels;

	public SpellColors(int[] ibars, String[] seriesLabels) {
		this.series = new double[ibars.length];
		this.labels = new String[seriesLabels.length];
		for (int i = 0; i < ibars.length; i++) {
			series[i] = ibars[i];
			labels[i] = seriesLabels[i];
		}
	}

	public Chart create() {
		ChartWithoutAxes cwoaPie = ChartWithoutAxesImpl.create();
		cwoaPie.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
		cwoaPie.setType("Pie Chart"); //$NON-NLS-1$	
		cwoaPie.setSubType("Standard Pie Chart"); //$NON-NLS-1$
		// Plot
		cwoaPie.setSeriesThickness(5);
		cwoaPie.setMinSlice(0.1);
		cwoaPie.setMinSliceLabel("Other");
		// Legend
		Legend lg = cwoaPie.getLegend();
		lg.setVisible(true);
		lg.getOutline().setVisible(false);
		// Title
		cwoaPie.getTitle().getLabel().getCaption().setValue("Colours");//$NON-NLS-1$
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(labels);
		NumberDataSet seriesOneValues = NumberDataSetImpl.create(series);
		SampleData sdata = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sdata.getBaseSampleData().add(sdBase);
		OrthogonalSampleData sdOrthogonal = DataFactory.eINSTANCE.createOrthogonalSampleData();
		sdOrthogonal.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal.setSeriesDefinitionIndex(0);
		sdata.getOrthogonalSampleData().add(sdOrthogonal);
		cwoaPie.setSampleData(sdata);
		// Base Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sd = SeriesDefinitionImpl.create();
		cwoaPie.getSeriesDefinitions().add(sd);
		createPallette(sd);
		sd.getSeries().add(seCategory);
		// Orthogonal Series
		PieSeries sePie = (PieSeries) PieSeriesImpl.create();
		sePie.setDataSet(seriesOneValues);
		//sePie.setSeriesIdentifier("Types");//$NON-NLS-1$ 
		sePie.setExplosion(5);
		SeriesDefinition sdCity = SeriesDefinitionImpl.create();
		sd.getSeriesDefinitions().add(sdCity);
		sdCity.getSeries().add(sePie);
		sePie.setTranslucent(true); // set transparency
		// format labels
		sePie.setLabelPosition(Position.OUTSIDE_LITERAL);
		DataPoint dataPoint = sePie.getDataPoint();
		dataPoint.getComponents().clear();
		dataPoint.setSeparator("");
		DataPointComponent dpc1 = DataPointComponentImpl.create(DataPointComponentType.ORTHOGONAL_VALUE_LITERAL,
		        JavaNumberFormatSpecifierImpl.create(": \n0"));//$NON-NLS-1$
		DataPointComponent dpc2 = DataPointComponentImpl.create(
		        DataPointComponentType.PERCENTILE_ORTHOGONAL_VALUE_LITERAL, JavaNumberFormatSpecifierImpl
		                .create(" (##.##%)")); //$NON-NLS-1$
		DataPointComponent dpc3 = DataPointComponentImpl.create(DataPointComponentType.BASE_VALUE_LITERAL, null);
		dataPoint.getComponents().add(dpc3);
		dataPoint.getComponents().add(dpc1);
		dataPoint.getComponents().add(dpc2);
		return cwoaPie;
	}

	private void createPallette(SeriesDefinition sd) {
		sd.getSeriesPalette().getEntries().clear();
		for (String colorName : labels) {
			ColorDefinition colorDef = getColorByName(colorName);
			sd.getSeriesPalette().getEntries().add(colorDef);
		}
	}

	private ColorDefinition getColorByName(String colorName) {
		if (colorName.equals("Black"))
			return ColorDefinitionImpl.BLACK();
		if (colorName.equals("Red"))
			return ColorDefinitionImpl.RED();
		if (colorName.equals("Green"))
			return ColorDefinitionImpl.GREEN();
		if (colorName.equals("White"))
			return ColorDefinitionImpl.WHITE();
		if (colorName.equals("Blue"))
			return ColorDefinitionImpl.BLUE();
		if (colorName.equals("Black-White"))
			return ColorDefinitionImpl.GREY().brighter();
		if (colorName.equals("Black-Green"))
			return ColorDefinitionImpl.GREEN().darker();
		if (colorName.equals("Black-Blue"))
			return ColorDefinitionImpl.BLUE().darker();
		if (colorName.equals("Black-Red"))
			return ColorDefinitionImpl.RED().darker();
		if (colorName.equals("Blue-Green"))
			return ColorDefinitionImpl.create(0xcc, 0xff, 0xff);
		if (colorName.equals("Blue-Red"))
			return ColorDefinitionImpl.create(0xff, 0x88, 0xee);
		if (colorName.equals("Blue-White"))
			return ColorDefinitionImpl.BLUE().brighter();
		if (colorName.equals("Green-Red"))
			return ColorDefinitionImpl.create(0xdd, 0xcc, 0x77);
		if (colorName.equals("Green-White"))
			return ColorDefinitionImpl.GREEN().brighter();
		if (colorName.equals("Red-White"))
			return ColorDefinitionImpl.create(0xff, 0xcc, 0xcc);
		if (colorName.equals("Colorless"))
			return ColorDefinitionImpl.GREY().transparent();
		// Other golds
		return ColorDefinitionImpl.ORANGE();
	}
}
