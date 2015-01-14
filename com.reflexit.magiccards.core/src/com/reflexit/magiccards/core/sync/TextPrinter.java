package com.reflexit.magiccards.core.sync;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class TextPrinter {
	static final Collection<ICardField> magicCardExportFields;
	static {
		ICardField[] values = MagicCardField.allNonTransientFields(false);
		LinkedHashSet<ICardField> list = new LinkedHashSet<ICardField>();
		for (ICardField magicCardField : values) {
			list.add(magicCardField);
		}
		list.remove(MagicCardField.RATING);
		list.remove(MagicCardField.RULINGS);
		list.remove(MagicCardField.DBPRICE);
		list.remove(MagicCardField.LANG);
		list.remove(MagicCardField.ENID);
		magicCardExportFields = list;
	}

	private static Collection<Object> values(IMagicCard card, Collection<ICardField> fields) {
		Collection<Object> list = new ArrayList<Object>();
		for (ICardField magicCardField : fields) {
			list.add(card.get(magicCardField));
		}
		return list;
	};

	private static Collection<String> headers(Collection<ICardField> fields) {
		Collection<String> list = new ArrayList<String>();
		for (ICardField magicCardField : fields) {
			list.add(magicCardField.toString());
		}
		return list;
	};

	public static void printHeader(MagicCard card, PrintStream out) {
		out.println(getHeaderMC());
	}

	public static String getHeaderMC() {
		return join(headers(magicCardExportFields), SEPARATOR);
	}

	public static String SEPARATOR = "|";
	public static char SEPARATOR_CHAR = '|';

	public static void print(MagicCard card, PrintStream out) {
		out.println(join(values(card, magicCardExportFields), SEPARATOR));
	}

	public static String toString(IMagicCard card) {
		if (card instanceof MagicCard) {
			return join(values(card, magicCardExportFields), SEPARATOR);
		} else if (card instanceof MagicCardPhysical) {
			return join(((MagicCardPhysical) card).getValues(), SEPARATOR);
		}
		return card.toString();
	}

	public static String join(Collection list, String sep) {
		StringBuffer buf = new StringBuffer();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element != null) {
				String string = element.toString();
				if (string.contains("\n")) {
					string = string.replaceAll("\n", "<br>");
				}
				buf.append(string);
			} else
				buf.append("");
			if (iter.hasNext()) {
				buf.append(sep);
			}
		}
		return buf.toString();
	}
}
