package com.reflexit.magiccards.ui.chart;

import org.eclipse.birt.chart.computation.DataPointHints;
import org.eclipse.birt.chart.event.StructureSource;
import org.eclipse.birt.chart.event.StructureType;
import org.eclipse.birt.chart.factory.RunTimeContext;
import org.eclipse.birt.chart.model.attribute.ActionType;
import org.eclipse.birt.chart.model.attribute.TooltipValue;
import org.eclipse.birt.chart.model.data.Action;
import org.eclipse.birt.chart.render.ActionRendererAdapter;

/**
 * Simple implementation for IActionRenderer
 */
public class ManaCurveActionRenderer extends ActionRendererAdapter {
	@Override
	public void processAction(Action action, StructureSource source, RunTimeContext rtc) {
		super.processAction(action, source, null);
	}

	public void processAction(Action action, StructureSource source) {
		if (ActionType.SHOW_TOOLTIP_LITERAL.equals(action.getType())) {
			TooltipValue tv = (TooltipValue) action.getValue();
			if (StructureType.SERIES_DATA_POINT.equals(source.getType())) {
				final DataPointHints dph = (DataPointHints) source.getSource();
				String MyToolTip = dph.getDisplayValue() + " cards " + " of cost "
						+ dph.getBaseDisplayValue();
				tv.setText(MyToolTip);
			}
		}
	}
}