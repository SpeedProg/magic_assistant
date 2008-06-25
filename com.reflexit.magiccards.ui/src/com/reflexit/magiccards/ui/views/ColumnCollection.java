package com.reflexit.magiccards.ui.views;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.views.columns.CostColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;
import com.reflexit.magiccards.ui.views.columns.IdColumn;
import com.reflexit.magiccards.ui.views.columns.NameColumn;
import com.reflexit.magiccards.ui.views.columns.OracleTextColumn;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;
import com.reflexit.magiccards.ui.views.columns.TypeColumn;

public class ColumnCollection {
	protected ArrayList columns = new ArrayList();

	public Collection getColumns() {
		return this.columns;
	}

	public int getColumnsNumber() {
		return this.columns.size();
	}

	public void createColumnLabelProviders() {
		this.columns.add(new NameColumn(IMagicCard.INDEX_NAME));
		this.columns.add(new IdColumn(IMagicCard.INDEX_ID));
		this.columns.add(new CostColumn(IMagicCard.INDEX_COST));
		this.columns.add(new TypeColumn(IMagicCard.INDEX_TYPE));
		this.columns.add(new PowerColumn(IMagicCard.INDEX_POWER, "P", "Power"));
		this.columns.add(new PowerColumn(IMagicCard.INDEX_TOUGHNESS, "T", "Toughness"));
		this.columns.add(new OracleTextColumn(IMagicCard.INDEX_ORACLE));
		this.columns.add(new GenColumn(IMagicCard.INDEX_EDITION, "Edition"));
		this.columns.add(new GenColumn(IMagicCard.INDEX_RARITY, "Rarity"));
		this.columns.add(new GenColumn(IMagicCard.INDEX_CTYPE, "Color Type"));
	}
}
