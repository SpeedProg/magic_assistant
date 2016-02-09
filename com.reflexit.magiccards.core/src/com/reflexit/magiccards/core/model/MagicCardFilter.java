package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.expr.CardFieldExpr;
import com.reflexit.magiccards.core.model.expr.Expr;
import com.reflexit.magiccards.core.model.expr.Operation;
import com.reflexit.magiccards.core.model.expr.TextValue;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.SearchToken;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.TokenType;

public class MagicCardFilter implements Cloneable {
	private Expr root = Expr.TRUE;
	private SortOrder sortOrder = new SortOrder();
	private GroupOrder groupOrder = new GroupOrder();
	private boolean onlyLastSet = false;
	private boolean nameGroupping = true;

	@Override
	public Object clone() {
		try {
			MagicCardFilter ret = (MagicCardFilter) super.clone();
			ret.groupOrder = (GroupOrder) this.groupOrder.clone();
			SortOrder x = new SortOrder();
			x.setFrom(this.sortOrder);
			ret.sortOrder = x;
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String toString() {
		return root.toString();
	}

	public Expr getRoot() {
		return this.root;
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

	public void setFilter(Expr root) {
		if (root == null)
			root = Expr.TRUE;
		this.root = root;
	}

	public void update(HashMap<String, String> map) {
		Expr expr = Expr.TRUE;
		expr = expr.and(createColorGroup(map));
		expr = expr.and(createOrGroup(map, CardTypes.getInstance())).and(createOrGroup(map, Editions.getInstance()))
				.and(createOrGroup(map, Locations.getInstance())).and(createOrGroup(map, Rarity.getInstance()))
				.and(FilterField.LANG.valueExpr(map)).and(FilterField.TYPE_LINE.valueExpr(map))
				.and(FilterField.NAME_LINE.valueExpr(map)).and(FilterField.POWER.valueExpr(map))
				.and(FilterField.TOUGHNESS.valueExpr(map)).and(FilterField.CCC.valueExpr(map))
				.and(FilterField.COUNT.valueExpr(map)).and(FilterField.PRICE.valueExpr(map))
				.and(FilterField.DBPRICE.valueExpr(map)).and(FilterField.COMMENT.valueExpr(map))
				.and(FilterField.OWNERSHIP.valueExpr(map)).and(FilterField.COMMUNITYRATING.valueExpr(map))
				.and(FilterField.COLLNUM.valueExpr(map)).and(FilterField.ARTIST.valueExpr(map))
				.and(FilterField.SPECIAL.valueExpr(map)).and(FilterField.FORTRADECOUNT.valueExpr(map))
				.and(FilterField.FORMAT.valueExpr(map));
		// text fields
		Expr text = FilterField.TEXT_LINE.valueExpr(map).or(FilterField.TEXT_LINE_2.valueExpr(map))
				.or(FilterField.TEXT_LINE_3.valueExpr(map));
		Expr textNot = FilterField.TEXT_NOT_1.valueExpr(map).and(FilterField.TEXT_NOT_2.valueExpr(map))
				.and(FilterField.TEXT_NOT_3.valueExpr(map)).not();
		this.root = expr.and(text).and(textNot);
	}

	public Expr createColorGroup(HashMap<String, String> map) {
		FilterField ff = FilterField.COLOR;
		boolean orOp = true;
		boolean only = false;
		if (map.containsKey(ColorTypes.IDENTITY_ID)) {
			ff = FilterField.COLOR_IDENITY;
			only = true;
		}
		if (map.containsKey(ColorTypes.AND_ID)) {
			orOp = false;
		}
		if (map.containsKey(ColorTypes.ONLY_ID)) {
			only = true;
		}
		Expr expr = createGroup(map, Colors.getInstance(), orOp, only, ff);
		// remaining color types
		map.remove(ColorTypes.ONLY_ID);
		map.remove(ColorTypes.AND_ID);
		map.remove(ColorTypes.IDENTITY_ID);
		expr = expr.and(createOrGroup(map, ColorTypes.getInstance()));
		return expr;
	}

	private Expr createOrGroup(HashMap<String, String> map, ISearchableProperty sp) {
		return createGroup(map, sp, true, false);
	}

	private Expr createGroup(HashMap<String, String> map, ISearchableProperty sp, boolean orOp, boolean notOp) {
		FilterField ff = sp.getFilterField();
		return createGroup(map, sp, orOp, notOp, ff);
	}

	private Expr createGroup(HashMap<String, String> map, ISearchableProperty sp, boolean orOp, boolean notOp,
			FilterField ff) {
		Expr nres = Expr.EMPTY;
		Expr res = Expr.EMPTY;
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

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public ICardField getGroupField() {
		return groupOrder.getTop();
	}


	public GroupOrder getGroupOrder() {
		return groupOrder;
	}

	public boolean isGroupped() {
		return groupOrder.isGroupped();
	}

	public boolean isFiltered(Object o) {
		if (this.root == Expr.TRUE)
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

	public void setGroupFields(ICardField... fields) {
		groupOrder = new GroupOrder(fields);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupOrder.hashCode();
		result = prime * result + (onlyLastSet ? 1231 : 1237);
		result = prime * result + root.hashCode();
		result = prime * result + ((sortOrder == null) ? 0 : sortOrder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MagicCardFilter))
			return false;
		MagicCardFilter other = (MagicCardFilter) obj;
		if (onlyLastSet != other.onlyLastSet)
			return false;
		if (!groupOrder.equals(other.groupOrder))
			return false;
		if (!sortOrder.equals(other.sortOrder))
			return false;
		if (!root.equals(other.root))
			return false;
		return true;
	}

	public boolean equalsGroups(MagicCardFilter other) {
		if (this == other)
			return true;
		if (groupOrder.equals(other.groupOrder))
			return true;
		return false;
	}

	public ICard[] filterCards(Iterable<? extends ICard> childrenList) {
		// String key = "filter cards";
		// MagicLogger.traceStart(key);
		Collection<ICard> filteredList = new ArrayList<ICard>();
		for (ICard elem : childrenList) {
			if (elem instanceof ICardGroup) {
				if (((ICardGroup) elem).size() != 0) {
					filteredList.add(elem);
				}
			} else if (!isFiltered(elem)) {
				filteredList.add(elem);
			}
		}
		if (isOnlyLastSet())
			filteredList = removeSetDuplicates(filteredList);
		// MagicLogger.traceEnd(key);
		return filteredList.toArray(new ICard[filteredList.size()]);
	}

	public Collection removeSetDuplicates(Collection filteredList) {
		LinkedHashMap<String, IMagicCard> unique = new LinkedHashMap<String, IMagicCard>();
		for (Iterator<IMagicCard> iterator = filteredList.iterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			if (elem instanceof MagicCard) {
				MagicCard card = (MagicCard) elem;
				IMagicCard old = unique.get(card.getName());
				if (old == null) {
					unique.put(card.getName(), card);
				} else {
					Edition oldE = old.getEdition();
					Edition newE = card.getEdition();
					if (oldE.getReleaseDate() != null && newE.getReleaseDate() != null) {
						if (oldE.getReleaseDate().before(newE.getReleaseDate())) {
							unique.put(card.getName(), card);
						}
						continue;
					}
					if (old.getCardId() < card.getCardId()) {
						unique.put(card.getName(), card);
					}
				}
			}
		}
		if (unique.size() > 0)
			return unique.values();
		return filteredList;
	}

	public void setNameGroupping(boolean nameGroupping) {
		this.nameGroupping = nameGroupping;
	}

	public boolean isNameGroupping() {
		return nameGroupping;
	}

	public void setGroupOrder(GroupOrder groupOrder) {
		if (this.groupOrder.equals(groupOrder))
			return;
		this.groupOrder = (GroupOrder) groupOrder.clone();
	}
}
