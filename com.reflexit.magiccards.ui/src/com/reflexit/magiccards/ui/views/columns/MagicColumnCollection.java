package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;

public class MagicColumnCollection extends ColumnCollection {
	private String id;

	public MagicColumnCollection(String prefPageId) {
		this.id = prefPageId;
	}

	@Override
	protected void createColumns() {
		boolean myCards = true;
		if (id != null && id.equals(MagicDbViewPreferencePage.class.getName())) {
			myCards = false;
		}
		this.columns.add(createGroupColumn());
		this.columns.add(new IdColumn());
		this.columns.add(new CostColumn());
		this.columns.add(new TypeColumn());
		this.columns.add(new PowerColumn(MagicCardField.POWER, "P", "Power"));
		this.columns.add(new PowerColumn(MagicCardField.TOUGHNESS, "T", "Toughness"));
		this.columns.add(new OracleTextColumn());
		this.columns.add(createSetColumn());
		this.columns.add(new GenColumn(MagicCardField.RARITY, "Rarity"));
		this.columns.add(new GenColumn(MagicCardField.CTYPE, "Color Type"));
		if (myCards) {
			this.columns.add(new CountColumn());
			this.columns.add(new LocationColumn());
			this.columns.add(new OwnershipColumn());
			this.columns.add(new CommentColumn());
			this.columns.add(new PriceColumn());
		}
		this.columns.add(new ColorColumn());
		this.columns.add(new SellerPriceColumn());
		this.columns.add(new CommunityRatingColumn());
		this.columns.add(new GenColumn(MagicCardField.ARTIST, "Artist"));
		this.columns.add(new CollectorsNumberColumn());
		if (myCards) {
			this.columns.add(new StringEditorColumn(MagicCardField.SPECIAL, "Special"));
			this.columns.add(new StringEditorColumn(MagicCardField.FORTRADECOUNT, "For Trade"));
		}
		this.columns.add(new LanguageColumn());
		this.columns.add(new TextColumn());
		columns.add(new OwnCountColumn());
		columns.add(new OwnUniqueColumn());
		columns.add(new LegalityColumn());
		if (myCards) {
			columns.add(new GenColumn(MagicCardField.SIDEBOARD, "Sideboard"));
			columns.add(new GenColumn(MagicCardField.ERROR, "Error"));
			columns.add(new CreationDateColumn());
		}
		columns.add(new ReleaseDateColumn());
	}

	protected SetColumn createSetColumn() {
		return new SetColumn();
	}

	protected GroupColumn createGroupColumn() {
		return new GroupColumn();
	}
}
