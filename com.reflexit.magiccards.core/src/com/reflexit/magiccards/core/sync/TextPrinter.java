package com.reflexit.magiccards.core.sync;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class TextPrinter {
	public static void printHeader(MagicCard card, PrintStream out) {
		out.println(join(card.getHeaderNames(), SEPARATOR));
	}

	public static String SEPARATOR = "|";

	public static void print(MagicCard card, PrintStream out) {
		out.println(join(card.getValues(), SEPARATOR));
	}

	public static String toString(IMagicCard card) {
		if (card instanceof MagicCard) {
			return join(((MagicCard) card).getValues(), SEPARATOR);
		} else if (card instanceof MagicCardPhysical) {
			return join(((MagicCardPhysical) card).getValues(), SEPARATOR);
		}
		return card.toString();
	}

	private static String join(Collection list, String sep) {
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
