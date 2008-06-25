package com.reflexit.magiccards.core.model;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

public class TextPrinter {
	public static void printHeader(MagicCard card, PrintStream out) {
		out.println(join(card.getHeaderNames(), SEPARATOR));
	}
	public static String SEPARATOR = "|";

	public static void print(MagicCard card, PrintStream out) {
		out.println(join(card.getValues(), SEPARATOR));
	}

	public static String join(Collection list, String sep) {
		StringBuffer buf = new StringBuffer();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element != null)
				buf.append(element.toString());
			else
				buf.append("null");
			if (iter.hasNext()) {
				buf.append(sep);
			}
		}
		return buf.toString();
	}
}
