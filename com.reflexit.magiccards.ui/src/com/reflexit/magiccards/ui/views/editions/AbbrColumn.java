package com.reflexit.magiccards.ui.views.editions;

import com.reflexit.magiccards.core.model.Edition;

public class AbbrColumn extends AbstractEditionColumn {
	public AbbrColumn() {
		super("Abbr", EditionField.NAME);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		String abbrs = ed.getMainAbbreviation();
		if (ed.getExtraAbbreviations().length() > 0)
			abbrs += " (" + ed.getExtraAbbreviations() + ")";
		return abbrs;
	}

	@Override
	public String getTextForEdit(Edition ed) {
		String abbrs = ed.getMainAbbreviation();
		if (ed.getExtraAbbreviations().length() > 0)
			abbrs += "," + ed.getExtraAbbreviations();
		return abbrs;
	}

	@Override
	public void setText(Edition edition, String abbrOther) {
		if (abbrOther.length() > 0) {
			String[] abbrs = abbrOther.trim().split(",");
			for (int i = 0; i < abbrs.length; i++) {
				String string = abbrs[i];
				edition.addAbbreviation(string.trim());
			}
		}
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}
}