package com.reflexit.magiccards.ui.views.editions;

import com.reflexit.magiccards.core.model.Edition;

public class AliasesColumn extends AbstractEditionColumn {
	public AliasesColumn() {
		super("Aliases", EditionField.ALIASES);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		return ed.getExtraAliases();
	}

	@Override
	public String getTextForEdit(Edition ed) {
		return ed.getExtraAliases();
	}

	@Override
	public void setText(Edition edition, String extraAliases) {
		if (extraAliases.length() > 0) {
			String[] abbrs = extraAliases.trim().split(",");
			for (int i = 0; i < abbrs.length; i++) {
				abbrs[i] = abbrs[i].trim();
			}
			edition.setNameAliases(abbrs);
		}
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}
}