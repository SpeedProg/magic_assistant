package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.widgets.ComboContributionItem;

public class PresentationComboContributionItem extends ComboContributionItem {
	protected PresentationComboContributionItem(String string) {
		super("pres_id");
		ArrayList<String> list = new ArrayList<>();
		for (final Presentation rt : Presentation.values()) {
			list.add(rt.getLabel());
		}
		setLabels(list);
		setSelection(string);
	}

	@Override
	protected int computeWidth(Control control) {
		return 110;
	}
}