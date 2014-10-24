package com.reflexit.magiccards.core.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.expr.CardFieldExpr;
import com.reflexit.magiccards.core.model.expr.Expr;
import com.reflexit.magiccards.core.model.expr.Operation;
import com.reflexit.magiccards.core.model.expr.TextValue;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.SearchToken;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.TokenType;

public class MagicCardFilter implements Cloneable {
	private Expr root;
	private int limit = Integer.MAX_VALUE;
	private SortOrder sortOrder = new SortOrder();
	private ICardField groupFields[];
	private boolean onlyLastSet = false;

	@Override
	public Object clone() {
		try {
			MagicCardFilter ret = (MagicCardFilter) super.clone();
			if (groupFields != null)
				ret.groupFields = Arrays.copyOf(groupFields, groupFields.length);
			ret.sortOrder = (SortOrder) sortOrder.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String toString() {
		return root.toString();
	}

	public static Expr TRUE = Expr.TRUE;

	public Expr getRoot() {
		return this.root;
	}

	static BinaryExpr ignoreCase1SearchDb(ICardField field, String value) {
		char c = value.charAt(0);
		if (Character.isLetter(c)) {
			String altValue = value.replaceAll("['\"%]", "_");
			if (Character.isUpperCase(c)) {
				altValue = Character.toLowerCase(c) + value.substring(1);
			} else if (Character.isLowerCase(c)) {
				altValue = Character.toUpperCase(c) + value.substring(1);
			}
			BinaryExpr b1 = BinaryExpr.fieldLike(field, "%" + value + "%");
			BinaryExpr b2 = BinaryExpr.fieldLike(field, "%" + altValue + "%");
			BinaryExpr res = new BinaryExpr(b1, Operation.OR, b2);
			return res;
		} else {
			return BinaryExpr.fieldLike(field, "%" + value + "%");
		}
	}

	public static BinaryExpr tokenSearch(ICardField field, SearchToken token) {
		String value = token.getValue();
		if (token.getType() == TokenType.REGEX) {
			TextValue tvalue = new TextValue(value, false, false, true);
			return new BinaryExpr(new CardFieldExpr(field), Operation.MATCHES, tvalue);
		} else if (token.getType() == TokenType.ABI) {
			Pattern pattern = Abilities.getPattern(value);
			if (pattern != null) {
				TextValue tvalue = new TextValue(pattern);
				return new BinaryExpr(new CardFieldExpr(field), Operation.MATCHES, tvalue);
			} else {
				// fall back to text value
			}
		}
		TextValue tvalue = new TextValue(value, false, false, false);
		char c = value.charAt(0);
		if (Character.isLetter(c) && token.getType() != TokenType.QUOTED) {
			tvalue.setWordBoundary(true);
		}
		return new BinaryExpr(new CardFieldExpr(field), Operation.MATCHES, tvalue);
	}

	public void update(HashMap<String, String> map) {
		Expr expr = TRUE;
		if (map.containsKey(ColorTypes.IDENTITY_ID)) {
			map.remove(ColorTypes.ONLY_ID);
			map.remove(ColorTypes.AND_ID);
			map.remove(ColorTypes.IDENTITY_ID);
			expr = createAndNotGroup(map, Colors.getInstance());
		} else if (map.containsKey(ColorTypes.ONLY_ID) && map.containsKey(ColorTypes.AND_ID)) {
			map.remove(ColorTypes.ONLY_ID);
			map.remove(ColorTypes.AND_ID);
			expr = createAndNotGroup(map, Colors.getInstance());
		} else if (map.containsKey(ColorTypes.AND_ID)) {
			map.remove(ColorTypes.AND_ID);
			expr = createAndGroup(map, Colors.getInstance());
		} else if (map.containsKey(ColorTypes.ONLY_ID)) {
			map.remove(ColorTypes.ONLY_ID);
			expr = createOrNotGroup(map, Colors.getInstance());
		} else {
			expr = createOrGroup(map, Colors.getInstance());
		}
		expr = expr
				.and(createOrGroup(map, ColorTypes.getInstance()))
				.and(createOrGroup(map, CardTypes.getInstance()))
				.and(createOrGroup(map, Editions.getInstance()))
				.and(createOrGroup(map, Locations.getInstance()))
				.and(createOrGroup(map, Rarity.getInstance()))
				.and(FilterField.LANG.valueExpr(map))
				.and(FilterField.TYPE_LINE.valueExpr(map))
				.and(FilterField.NAME_LINE.valueExpr(map))
				.and(FilterField.POWER.valueExpr(map))
				.and(FilterField.TOUGHNESS.valueExpr(map))
				.and(FilterField.CCC.valueExpr(map))
				.and(FilterField.COUNT.valueExpr(map))
				.and(FilterField.PRICE.valueExpr(map))
				.and(FilterField.DBPRICE.valueExpr(map))
				.and(FilterField.COMMENT.valueExpr(map))
				.and(FilterField.OWNERSHIP.valueExpr(map))
				.and(FilterField.COMMUNITYRATING.valueExpr(map))
				.and(FilterField.COLLNUM.valueExpr(map))
				.and(FilterField.ARTIST.valueExpr(map))
				.and(FilterField.SPECIAL.valueExpr(map))
				.and(FilterField.FORTRADECOUNT.valueExpr(map))
				.and(FilterField.FORMAT.valueExpr(map));
		// text fields
		Expr text = FilterField.TEXT_LINE.valueExpr(map)
				.or(FilterField.TEXT_LINE_2.valueExpr(map))
				.or(FilterField.TEXT_LINE_3.valueExpr(map));
		Expr textNot =
				FilterField.TEXT_NOT_1.valueExpr(map)
						.and(FilterField.TEXT_NOT_2.valueExpr(map))
						.and(FilterField.TEXT_NOT_3.valueExpr(map))
						.not();
		this.root = expr.and(text).and(textNot);
	}

	private Expr createOrGroup(HashMap<String, String> map, ISearchableProperty sp) {
		return createGroup(map, sp, true, false);
	}

	private Expr createAndGroup(HashMap<String, String> map, ISearchableProperty sp) {
		return createGroup(map, sp, false, false);
	}

	private Expr createAndNotGroup(HashMap<String, String> map, ISearchableProperty sp) {
		return createGroup(map, sp, false, true);
	}

	private Expr createOrNotGroup(HashMap<String, String> map, ISearchableProperty sp) {
		return createGroup(map, sp, true, true);
	}

	private Expr createGroup(HashMap<String, String> map, ISearchableProperty sp, boolean orOp, boolean notOp) {
		Expr nres = Expr.EMPTY;
		Expr res = Expr.EMPTY;
		FilterField ff = sp.getFilterField();
		for (Iterator<String> iterator = sp.getIds().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			String value = map.get(id);
			if (value == null || value.equals("false") || value.isEmpty()) {
				if (notOp) {
					Expr expr = ff.valueExpr(sp.getNameById(id));
					nres = nres.and(expr.not());
				}
			} else if (value.equals("true")) {
				Expr expr = ff.valueExpr(sp.getNameById(id));
				if (orOp)
					res = res.or(expr);
				else
					res = res.and(expr);
			} else
				throw new IllegalArgumentException();
		}
		return nres.and(res);
	}

	public int getLimit() {
		return this.limit;
	}

	/**
	 * sort field
	 * 
	 * @param sortField
	 */
	public void setSortField(ICardField sortField, boolean accending) {
		sortOrder.setSortField(sortField, accending);
	}

	public SortOrder getSortOrder() {
		return this.sortOrder;
	}

	public ICardField getGroupField() {
		if (groupFields != null)
			return groupFields[0];
		return null;
	}

	public ICardField[] getGroupFields() {
		return this.groupFields;
	}

	public void setGroupField(ICardField groupField) {
		setGroupField(0, groupField);
	}

	public void setGroupField(int index, ICardField groupField) {
		if (groupFields == null)
			groupFields = new ICardField[10];
		this.groupFields[index] = groupField;
	}

	public void setLimit(int limit) {
		if (limit < 0)
			throw new IllegalArgumentException("Invalid value for limit (must be >=0)");
		this.limit = limit;
	}

	public boolean isFiltered(Object o) {
		if (this.root == null)
			return false;
		boolean res = !this.root.evaluate(o);
		return res;
	}

	public boolean isOnlyLastSet() {
		return onlyLastSet;
	}

	public void setOnlyLastSet(boolean onlyLastSet) {
		this.onlyLastSet = onlyLastSet;
	}

	public void setNoSort() {
		sortOrder.clear();
	}

	public void setGroupFields(ICardField[] fields) {
		if (fields == null)
			groupFields = null;
		else
			groupFields = Arrays.copyOf(fields, fields.length);
	}

	public void setSortOrder(SortOrder sortOrder) {
		if (sortOrder == null)
			throw new NullPointerException();
		this.sortOrder = sortOrder;
	}
}
