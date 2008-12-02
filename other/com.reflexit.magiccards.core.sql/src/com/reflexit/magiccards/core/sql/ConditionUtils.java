package com.reflexit.magiccards.core.sql;

import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.SuperTypes;
import com.reflexit.magiccards.core.model.MagicCardFilter.BinaryExpr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Operation;

public class ConditionUtils {
	static String getCondition(MagicCardFilter filter) {
		MagicCardFilter.Expr root = filter.getRoot();
		if (root == null)
			return null;
		String cond = getCondition(root);
		System.err.println("Condition " + cond);
		return cond;
	}

	static String getCondition(Expr expr) {
		if (expr instanceof MagicCardFilter.BinaryExpr) {
			MagicCardFilter.BinaryExpr bin = (BinaryExpr) expr;
			MagicCardFilter.Operation op = bin.getOp();
			String res = getSpecial(bin);
			if (res != null)
				return res;
			return getCondition(bin.getLeft()) + " " + op.toString() + " " + getCondition(bin.getRight());
		}
		if (expr instanceof MagicCardFilter.Node) {
			return expr.toString();
		}
		throw new UnsupportedOperationException(expr.toString());
	}

	/**
	 * @param bin
	 * @return
	 */
	static String getSpecial(MagicCardFilter.BinaryExpr bin) {
		MagicCardFilter.Operation op = bin.getOp();
		if (op == Operation.EQUALS) {
			String requestedId = bin.getLeft().toString();
			String value = bin.getRight().toString();
			if (Colors.getInstance().getIdPrefix().equals(requestedId)) {
				String en = Colors.getInstance().getEncodeByName(value);
				if (en != null) {
					return "cost LIKE '%{" + en + "}%'";
				} else if (value.equals("Multi-Color")) {
					return "colorType = 'multi'";
				} else if (value.equals("Colorless")) {
					return "( colorType = 'colorless' OR colorType = 'land' )";
				}
			} else if (CardTypes.getInstance().getIdPrefix().equals(requestedId)) {
				if (value.equals("Artifact")) {
					return "( type LIKE '%Artifact' OR type LIKE '%Artifact -%' )";
				}
				return ignoreCase1Search("type", value);
			} else if (Editions.getInstance().getIdPrefix().equals(requestedId)) {
				return "( edition  = '" + value + "' )";
			} else if (SuperTypes.getInstance().getIdPrefix().equals(requestedId)) {
				return "( type LIKE '%" + value + " %' AND type NOT LIKE '%" + value + " -%' )";
			} else if (FilterHelper.SUBTYPE.equals(requestedId)) {
				return textSearch("type", value);
			} else if (FilterHelper.TEXT_LINE.equals(requestedId)) {
				return textSearch("oracleText", value);
			} else if (FilterHelper.POWER.equals(requestedId)) {
				return "( power is NULL OR power " + value + " )";
			} else if (FilterHelper.CCC.equals(requestedId)) {
				return "( cmc " + value + " )";
			} else if (FilterHelper.TOUGHNESS.equals(requestedId)) {
				return "( toughness is NULL OR toughness " + value + " )";
			}
		}
		return null;
	}

	static String textSearch(String field, String text) {
		text = text.trim();
		text = text.replaceAll("\\s\\s*", " ");
		text = text.replaceAll("['\"%]", "_");
		String[] split = text.split(" ");
		StringBuffer res = new StringBuffer();
		boolean addop = false;
		for (int i = 0; i < split.length; i++) {
			String value = split[i];
			if (value.equals("OR") || value.equals("(") || value.equals(")") || value.equals("AND")
			        || value.equals("NOT")) {
				res.append(" ");
				res.append(value);
				res.append(" ");
				addop = false;
				continue;
			}
			if (addop) {
				res.append(" AND ");
			}
			if (value.startsWith("-") && value.length() > 1) {
				res.append(" NOT ");
				value = value.substring(1);
			}
			res.append(ignoreCase1Search(field, value));
			addop = true;
		}
		return res.toString();
	}

	static String ignoreCase1Search(String field, String value) {
		String altValue = value;
		char c = value.charAt(0);
		if (Character.isUpperCase(c)) {
			altValue = Character.toLowerCase(c) + value.substring(1);
		} else if (Character.isLowerCase(c)) {
			altValue = Character.toUpperCase(c) + value.substring(1);
		}
		return "( " + field + " LIKE '%" + value + "%'" + " OR " + field + " LIKE '%" + altValue + "%'" + " )";
	}
}
