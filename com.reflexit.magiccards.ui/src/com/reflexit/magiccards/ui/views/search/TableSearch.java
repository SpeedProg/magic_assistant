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

import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * @author Alena
 *
 */
public class TableSearch {
	public static void search(SearchContext context, IFilteredCardStore<IMagicCard> store) {
		IMagicCard last = (IMagicCard) context.last;
		Object[] elements = store.getElements();
		context.last = null;
		context.status = false;
		context.didWrap = false;
		String pattern = ".*\\Q" + context.text + "\\E.*";
		if (context.wholeWord)
			pattern = ".*\\b\\Q" + context.text + "\\E\\b.*";
		int flags = Pattern.CASE_INSENSITIVE;
		if (context.matchCase)
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
		if (context.forward) {
			lastIndex++;
			for (int i = lastIndex; i < elements.length; i++) {
				IMagicCard card = (IMagicCard) elements[i];
				if (match(pat, card)) {
					context.last = card;
					context.status = true;
					break;
				}
			}
			if (!context.status) {
				context.didWrap = true;
				for (int i = 0; i <= lastIndex; i++) {
					IMagicCard card = (IMagicCard) elements[i];
					if (match(pat, card)) {
						context.last = card;
						context.status = true;
						break;
					}
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
					context.last = card;
					context.status = true;
					break;
				}
			}
			if (!context.status) {
				context.didWrap = true;
				for (int i = elements.length - 1; i >= lastIndex; i--) {
					IMagicCard card = (IMagicCard) elements[i];
					if (match(pat, card)) {
						context.last = card;
						context.status = true;
						break;
					}
				}
			}
		}
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
