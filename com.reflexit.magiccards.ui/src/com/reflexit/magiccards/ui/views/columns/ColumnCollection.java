package com.reflexit.magiccards.ui.views.columns;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.ui.views.MagicDbView;

public class ColumnCollection {
	private String id;

	public ColumnCollection(String viewId) {
		this.id = viewId;
	}
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
		if (!this.id.equals(MagicDbView.ID)) {
			this.columns.add(new GenColumn(11, "Count") {
				@Override
				public String getText(Object element) {
					if (element instanceof MagicCardPhisical) {
						MagicCardPhisical m = (MagicCardPhisical) element;
						return m.getCount() + "";
					} else {
						return "";
					}
				}
			});
		}
	}
}
