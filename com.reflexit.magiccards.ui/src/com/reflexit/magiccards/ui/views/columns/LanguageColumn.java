package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.Languages.Language;
import com.reflexit.magiccards.core.model.MagicCardField;

public class LanguageColumn extends GenColumn {
	public LanguageColumn() {
		super(MagicCardField.LANG, "Language");
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}

	@Override
	public String getText(Object element) {
		String text = super.getText(element);
		if (text == null || text.length() == 0)
			return Language.ENGLISH.getLang();
		return text;
	}
}
