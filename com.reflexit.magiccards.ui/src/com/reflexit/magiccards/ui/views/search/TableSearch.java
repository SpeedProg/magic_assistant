/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.views.search;

import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * @author Alena
 *
 */
public class TableSearch {
	public static void search(SearchContext context, IFilteredCardStore store) {
		IMagicCard last;
		String inputText;
		boolean wholeWord;
		boolean matchCase;
		boolean needWrap;
		synchronized (context) {
			inputText = context.getText();
			last = (IMagicCard) context.getLast();
			wholeWord = context.isWholeWord();
			matchCase = context.isMatchCase();
			needWrap = context.isWrapAround();
			context.setFound(false); // don't reset last yet
			context.setDidWrap(false);
		}
		Object[] elements = store.getElements();
		String escapedInput = escapeAndCamelCase(inputText);
		String pattern = escapedInput;
		if (wholeWord)
			pattern = "\\b" + escapedInput + "\\b";
		pattern = ".*" + pattern + ".*";
		int flags = Pattern.CASE_INSENSITIVE;
		if (matchCase)
			flags = 0;
		Pattern pat = Pattern.compile(pattern, flags);
		int lastIndex = -1;
		if (last != null) {
			for (int i = 0; i < elements.length; i++) {
				IMagicCard card = (IMagicCard) elements[i];
				if (card == last) {
					lastIndex = i;
					break;
				}
			}
		}
		if (context.isForward()) {
			lastIndex++;
			for (int i = lastIndex; i < elements.length; i++) {
				IMagicCard card = (IMagicCard) elements[i];
				if (match(pat, card)) {
					context.setFound(true, card);
					break;
				}
				if (context.isCancelled())
					return;
			}
			if (needWrap && !context.isFound()) {
				context.setDidWrap(true);
				for (int i = 0; i <= lastIndex; i++) {
					IMagicCard card = (IMagicCard) elements[i];
					if (match(pat, card)) {
						context.setFound(true, card);
						break;
					}
					if (context.isCancelled())
						return;
				}
			}
		} else {
			if (lastIndex <= -1)
				lastIndex = elements.length - 1;
			else
				lastIndex--;
			for (int i = lastIndex; i >= 0; i--) {
				IMagicCard card = (IMagicCard) elements[i];
				if (match(pat, card)) {
					context.setFound(true, card);
					break;
				}
				if (context.isCancelled())
					return;
			}
			if (needWrap && !context.isFound()) {
				context.setDidWrap(true);
				if (elements.length == 0)
					return;
				for (int i = elements.length - 1; i >= lastIndex && i >= 0; i--) {
					IMagicCard card = (IMagicCard) elements[i];
					if (match(pat, card)) {
						context.setFound(true, card);
						break;
					}
				}
				if (context.isCancelled())
					return;
			}
		}
	}

	private static String escapeAndCamelCase(String inputText) {
		char[] charArray = inputText.toCharArray();
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (!(Character.isLetter(c) || c == ',' || c == ' ')) {
				res.append('.');
				continue;
			}
			if (Character.isUpperCase(c)) {
				if (i > 0)
					res.append(".*");
				res.append("\\b");
			}
			res.append(c);
		}
		return res.toString();
	}

	/**
	 * @param pat 
	 * @param card
	 * @return
	 */
	protected static boolean match(Pattern pat, IMagicCard card) {
		if (pat.matcher(card.getName()).matches())
			return true;
		return false;
	}
}
