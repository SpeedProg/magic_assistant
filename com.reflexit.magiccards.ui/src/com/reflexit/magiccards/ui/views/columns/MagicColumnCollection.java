package com.reflexit.magiccards.ui.views.columns;

import java.util.List;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class MagicColumnCollection extends ColumnCollection {
	private String id;
	private GroupColumn groupColumn;
	private SetColumn setColumn;

	public MagicColumnCollection(String prefPageId) {
		this.id = prefPageId;
	}

	@Override
	protected void createColumns(List<AbstractColumn> columns) {
		boolean myCards = true;
		groupColumn = createGroupColumn();
		columns.add(groupColumn);
		columns.add(new IdColumn());
		columns.add(new CostColumn());
		columns.add(new TypeColumn());
		columns.add(new PowerColumn(MagicCardField.POWER, "P", "Power"));
		columns.add(new PowerColumn(MagicCardField.TOUGHNESS, "T", "Toughness"));
		columns.add(new OracleTextColumn());
		setColumn = createSetColumn();
		columns.add(setColumn);
		columns.add(new GenColumn(MagicCardField.RARITY, "Rarity"));
		columns.add(new GenColumn(MagicCardField.CTYPE, "Color Type"));
		if (myCards) {
			columns.add(new CountColumn());
			columns.add(new LocationColumn());
			columns.add(new OwnershipColumn());
			columns.add(new CommentColumn());
			columns.add(new PriceColumn());
		}
		columns.add(new ColorColumn());
		columns.add(new ColorIdentityColumn());
		columns.add(new SellerPriceColumn());
		columns.add(new CommunityRatingColumn());
		columns.add(new GenColumn(MagicCardField.ARTIST, "Artist"));
		columns.add(new CollectorsNumberColumn());
		if (myCards) {
			columns.add(new StringEditorColumn(MagicCardField.SPECIAL, "Special"));
			columns.add(new ForTradeCountColumn());
		}
		columns.add(new LanguageColumn());
		columns.add(new TextColumn());
		columns.add(new OwnCountColumn());
		columns.add(new OwnUniqueColumn());
		columns.add(new LegalityColumn());
		if (myCards) {
			columns.add(new GenColumn(MagicCardField.SIDEBOARD, "Sideboard"));
			columns.add(new GenColumn(MagicCardField.ERROR, "Error"));
			columns.add(new CreationDateColumn());
		}
		columns.add(new ReleaseDateColumn());
		if (MagicUIActivator.TRACE_EXPORT) {
			columns.add(new GenColumn(MagicCardField.HASHCODE, "HashCode"));
		}
	}

	protected SetColumn createSetColumn() {
		return new SetColumn();
	}

	protected GroupColumn createGroupColumn() {
		return new GroupColumn();
	}

	public GroupColumn getGroupColumn() {
		if (groupColumn == null)
			groupColumn = createGroupColumn();
		return groupColumn;
	}

	public SetColumn getSetColumn() {
		if (setColumn == null)
			setColumn = createSetColumn();
		return setColumn;
	}

	@Override
	public String getId() {
		return id;
	}
}
