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

import org.eclipse.jface.viewers.TreePath;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * @author Alena
 *
 */
public class TableSearch {
	public static void search(SearchContext context, IFilteredCardStore store) {
		Object last;
		String inputText;
		boolean wholeWord;
		boolean matchCase;
		boolean needWrap;
		synchronized (context) {
			inputText = context.getText();
			last = context.getLast();
			wholeWord = context.isWholeWord();
			matchCase = context.isMatchCase();
			needWrap = context.isWrapAround();
			context.setFound(false); // don't reset last yet
			context.setDidWrap(false);
		}
		if (isCamelCase(inputText)) {
			matchCase = true;
		} else {
			matchCase = false;
		}
		String escapedInput = escapeAndCamelCase(inputText);
		String pattern = escapedInput;
		if (wholeWord)
			pattern = "\\b" + escapedInput + "\\b";
		pattern = ".*" + pattern + ".*";
		int flags = Pattern.CASE_INSENSITIVE;
		if (matchCase)
			flags = 0;
		Pattern pat = Pattern.compile(pattern, flags);
		if (store != null && store.getCardGroupRoot() != null && store.getCardGroupRoot().size() > 0) {
			if (last instanceof TreePath) {
				searchTree(context, (TreePath) last, needWrap, pat, store.getCardGroupRoot(), TreePath.EMPTY);
				if (!context.isFound() && needWrap) {
					context.setDidWrap(true);
					searchTree(context, null, needWrap, pat, store.getCardGroupRoot(), TreePath.EMPTY);
				}
			} else {
				searchTree(context, null, needWrap, pat, store.getCardGroupRoot(), TreePath.EMPTY);
			}
		} else {
			if (last instanceof TreePath) {
				last = ((TreePath) last).getLastSegment();
			}
			searchFlat(context, store, last, needWrap, pat);
		}
	}

	public static int getIndex(Object last, Object[] elements) {
		if (last != null) {
			for (int i = 0; i < elements.length; i++) {
				Object card = elements[i];
				if (card == last) {
					return i;
				}
			}
		}
		return -1;
	}

	private static void searchTree(SearchContext context, TreePath last, boolean needWrap, Pattern pat,
			ICardGroup group, TreePath path) {
		Object[] elements = group.getChildren();
		int lastIndex = -1;
		int len = elements.length;
		int i1 = 0, i2 = len - 1;
		if (last != null) {
			lastIndex = getIndex(last.getFirstSegment(), elements);
			if (lastIndex != -1) {
				if (last.getSegmentCount() == 1) {
					i1 = lastIndex + 1;
				} else {
					i1 = lastIndex;
				}
			}
		}
		int start = i1, end = i2, off = 1;
		if (context.isForward() == false) {
			start = i2;
			end = i1;
			off = -1;
		}
		for (int i = start; i * off <= end * off && context.isFound() == false
				&& context.isCancelled() == false; i += off) {
			int j = i % elements.length;
			ICard card = (ICard) elements[j];
			TreePath fullPath = path.createChildPath(card);
			if (j != lastIndex && match(pat, card)) {
				context.setFound(true, fullPath);
				break;
			}
			if (card instanceof ICardGroup) {
				if (j == lastIndex) {
					searchTree(context, cutHead(last), needWrap, pat, (ICardGroup) card, fullPath);
					lastIndex = -1;
				} else
					searchTree(context, null, needWrap, pat, (ICardGroup) card, fullPath);
			}
		}
	}

	private static TreePath cutHead(TreePath last) {
		int l = last.getSegmentCount();
		if (l <= 1)
			throw new IllegalArgumentException();
		Object[] arr = new Object[l - 1];
		for (int i = 1; i < l; i++) {
			Object segment = last.getSegment(i);
			arr[i - 1] = segment;
		}
		return new TreePath(arr);
	}

	private static void searchFlat(SearchContext context, IFilteredCardStore store, Object last,
			boolean needWrap, Pattern pat) {
		if (store == null)
			return;
		Object[] elements = store.getElements();
		int lastIndex = getIndex(last, elements);
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

	private static boolean isCamelCase(String inputText) {
		char[] charArray = inputText.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (Character.isUpperCase(c)) {
				return true;
			}
		}
		return false;
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
					res.append("\\P{Lu}*");
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
	protected static boolean match(Pattern pat, ICard card) {
		if (!(card instanceof ICardGroup) && pat.matcher(card.getName()).matches())
			return true;
		return false;
	}
}
