package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.ui.views.MagicDbView;

public class MagicColumnCollection extends ColumnCollection {
	private String id;

	public MagicColumnCollection(String viewId) {
		this.id = viewId;
	}

	@Override
	protected void createColumns() {
		this.columns.add(new GroupColumn());
		this.columns.add(new NameColumn());
		this.columns.add(new IdColumn());
		this.columns.add(new CostColumn());
		this.columns.add(new TypeColumn());
		this.columns.add(new PowerColumn(MagicCardField.POWER, "P", "Power"));
		this.columns.add(new PowerColumn(MagicCardField.TOUGHNESS, "T", "Toughness"));
		this.columns.add(new OracleTextColumn());
		this.columns.add(new SetColumn());
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
			this.columns.add(new StringEditorColumn(MagicCardFieldPhysical.SPECIAL, "Special"));
			this.columns.add(new StringEditorColumn(MagicCardFieldPhysical.FORTRADECOUNT, "For Trade"));
		}
		this.columns.add(new GenColumn(MagicCardField.LANG, "Language"));
		this.columns.add(new TextColumn());
	}
}
