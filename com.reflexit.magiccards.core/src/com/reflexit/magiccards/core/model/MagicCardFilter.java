package com.reflexit.magiccards.core.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.expr.CardFieldExpr;
import com.reflexit.magiccards.core.model.expr.Expr;
import com.reflexit.magiccards.core.model.expr.FilterFieldExpr;
import com.reflexit.magiccards.core.model.expr.Node;
import com.reflexit.magiccards.core.model.expr.NotExpr;
import com.reflexit.magiccards.core.model.expr.Operation;
import com.reflexit.magiccards.core.model.expr.TextValue;
import com.reflexit.magiccards.core.model.expr.Value;
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

	public void update(HashMap map) {
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
				.and(createTextSearch(map, FilterField.LANG))
				.and(createTextSearch(map, FilterField.TYPE_LINE))
				.and(createTextSearch(map, FilterField.NAME_LINE))
				.and(createNumericSearch(map, FilterField.POWER))
				.and(createNumericSearch(map, FilterField.TOUGHNESS))
				.and(createNumericSearch(map, FilterField.CCC))
				.and(createNumericSearch(map, FilterField.COUNT))
				.and(createNumericSearch(map, FilterField.PRICE))
				.and(createNumericSearch(map, FilterField.DBPRICE))
				.and(createTextSearch(map, FilterField.COMMENT))
				.and(createTextSearch(map, FilterField.OWNERSHIP))
				.and(createNumericSearch(map, FilterField.COMMUNITYRATING))
				.and(createNumericSearch(map, FilterField.COLLNUM))
				.and(createTextSearch(map, FilterField.ARTIST))
				.and(createTextSearch(map, FilterField.SPECIAL))
				.and(createNumericSearch(map, FilterField.FORTRADECOUNT))
				.and(createTextSearch(map, FilterField.FORMAT));
		// text fields
		Expr text = createTextSearch(map, FilterField.TEXT_LINE)
				.or(createTextSearch(map, FilterField.TEXT_LINE_2))
		         .or(createTextSearch(map, FilterField.TEXT_LINE_3));
		expr = expr.and(text)
				.and(createTextSearch(map, FilterField.TEXT_NOT_1))
				.and(createTextSearch(map, FilterField.TEXT_NOT_2))
				.and(createTextSearch(map, FilterField.TEXT_NOT_3));
		this.root = expr.translate();
	}

	private Expr createTextSearch(HashMap<String, String> map, FilterField fieldId) {
		if (!fieldId.getPostfix().equals(FilterField.Postfix.TEXT_POSTFIX))
			throw new IllegalArgumentException();

		String value = map.get(fieldId.getPrefConstant());
		if (value != null && value.length() > 0) {
			return new BinaryExpr(new FilterFieldExpr(fieldId), Operation.EQUALS, new Node(value));
		}
		return Expr.EMPTY;
	}

	private Expr createNumericSearch(HashMap map, FilterField fieldId) {
		if (!fieldId.getPostfix().equals(FilterField.Postfix.NUMERIC_POSTFIX))
			throw new IllegalArgumentException();
		Expr sub = Expr.EMPTY;
		String valueKey = fieldId.getPrefConstant();
		String value = (String) map.get(valueKey);
		if (value != null && value.length() > 0) {
			sub = new BinaryExpr(new FilterFieldExpr(fieldId), Operation.EQUALS, new Node(value));
		}
		return sub;
	}

	private static BinaryExpr createAndGroup(Expr a, Expr b) {
		Expr res = null;
		if (a == null && b == null) {
			res = TRUE;
		} else if (b == null)
			res = a;
		else if (a == null)
			res = b;
		else {
			res = new BinaryExpr(a, Operation.AND, b);
		}
		if (res instanceof BinaryExpr)
			return (BinaryExpr) res;
		else
			return new BinaryExpr(res, Operation.AND, TRUE);
	}


	private Expr createOrGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, true, false);
	}

	private Expr createAndGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, false, false);
	}

	private Expr createAndNotGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, false, true);
	}

	private Expr createOrNotGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, true, true);
	}

	private Expr createGroup(HashMap map, ISearchableProperty sp, boolean orOp, boolean notOp) {
		Expr nres = Expr.TRUE;
		FilterFieldExpr ffe = new FilterFieldExpr(sp.getFilterField());
		if (notOp) {
			for (Iterator<String> iterator = sp.getIds().iterator(); iterator.hasNext();) {
				String id = iterator.next();
				String value = (String) map.get(id);
				if (value == null || value.equals("false") || value.isEmpty()) {
					nres = nres.and(new NotExpr(
							new BinaryExpr(
									ffe,
									Operation.EQUALS,
									new Node(sp.getNameById(id)))
							));
				}
			}
		}
		Expr res = Expr.EMPTY;
		for (Iterator<String> iterator = sp.getIds().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			String value = (String) map.get(id);
			if (value == null || value.equals("false") || value.isEmpty())
				continue;// skip false unless notOp defined
			BinaryExpr expr = null;
			if (value.equals("true")) {
				expr = new BinaryExpr(ffe, Operation.EQUALS, new Node(sp.getNameById(id)));
			} else {
				expr = new BinaryExpr(ffe, Operation.EQUALS, new Value(value));
			}
			if (orOp)
				res = res.or(expr);
			else
				res = res.and(expr);
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
