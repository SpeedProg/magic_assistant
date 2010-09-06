package com.reflexit.magiccards.ui.views.columns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
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
		this.columns.add(new NameColumn());
		this.columns.add(new IdColumn());
		this.columns.add(new CostColumn());
		this.columns.add(new TypeColumn());
		this.columns.add(new PowerColumn(MagicCardField.POWER, "P", "Power"));
		this.columns.add(new PowerColumn(MagicCardField.TOUGHNESS, "T", "Toughness"));
		this.columns.add(new OracleTextColumn());
		this.columns.add(new GenColumn(MagicCardField.SET, "Set"));
		this.columns.add(new GenColumn(MagicCardField.RARITY, "Rarity"));
		this.columns.add(new GenColumn(MagicCardField.CTYPE, "Color Type"));
		if (!this.id.equals(MagicDbView.ID)) {
			this.columns.add(new CountColumn());
			this.columns.add(new LocationColumn());
		}
		this.columns.add(new ColorColumn());
		if (!this.id.equals(MagicDbView.ID)) {
			this.columns.add(new OwnershipColumn());
			this.columns.add(new CommentColumn());
			this.columns.add(new PriceColumn());
		}
		this.columns.add(new SellerPriceColumn());
		this.columns.add(new CommunityRatingColumn());
		this.columns.add(new GenColumn(MagicCardField.ARTIST, "Artist"));
		this.columns.add(new GenColumn(MagicCardField.COLLNUM, "Collector's Number"));
		if (!this.id.equals(MagicDbView.ID)) {
			this.columns.add(new StringEditorColumn(MagicCardFieldPhysical.CONDITION, "Condition"));
			this.columns.add(new StringEditorColumn(MagicCardFieldPhysical.VARIANT, "Variant"));
			this.columns.add(new StringEditorColumn(MagicCardFieldPhysical.FORTRADECOUNT, "For Trade"));
		}
		int j = 0;
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			AbstractColumn col = (AbstractColumn) iterator.next();
			col.setColumnIndex(j++);
		}
	}

	public String[] getColumnNames() {
		int i = 0;
		String[] columnNames = new String[getColumnsNumber()];
		for (Iterator iterator = getColumns().iterator(); iterator.hasNext();) {
			AbstractColumn col = (AbstractColumn) iterator.next();
			columnNames[i++] = col.getColumnFullName();
		}
		return columnNames;
	}

	public String[] getColumnIds() {
		int i = 0;
		String[] columnNames = new String[getColumnsNumber()];
		for (Iterator iterator = getColumns().iterator(); iterator.hasNext();) {
			AbstractColumn col = (AbstractColumn) iterator.next();
			columnNames[i++] = col.getDataField().toString();
		}
		return columnNames;
	}
}
