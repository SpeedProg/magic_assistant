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

public class SearchContext {
	// input
	private boolean matchCase;
	private boolean wholeWord;
	private String text;
	private boolean forward; // should search forward
	private boolean wrapAround; // should continue if wrapped around
	private boolean cancelled; // request is cancelled
	// output
	private Object last; // last found object, used also as input
	private boolean status;
	private boolean didWrap;

	public synchronized void setLast(Object last) {
		this.last = last;
	}

	public synchronized Object getLast() {
		return last;
	}

	public synchronized void setText(String text) {
		this.text = text;
		this.setCancelled(true);
	}

	public synchronized String getText() {
		return text;
	}

	public synchronized void setDidWrap(boolean didWrap) {
		this.didWrap = didWrap;
	}

	public synchronized boolean isDidWrap() {
		return didWrap;
	}

	public synchronized void setForward(boolean forward) {
		this.forward = forward;
	}

	public synchronized boolean isForward() {
		return forward;
	}

	public synchronized void setFound(boolean status) {
		this.status = status;
	}

	public synchronized void setFound(boolean status, Object object) {
		this.status = status;
		this.last = object;
	}

	public synchronized boolean isFound() {
		return status;
	}

	public synchronized void setWholeWord(boolean wholeWord) {
		this.wholeWord = wholeWord;
	}

	public synchronized boolean isWholeWord() {
		return wholeWord;
	}

	public synchronized void setWrapAround(boolean wrapAround) {
		this.wrapAround = wrapAround;
	}

	public synchronized boolean isWrapAround() {
		return wrapAround;
	}

	public synchronized void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}

	public synchronized boolean isMatchCase() {
		return matchCase;
	}

	public synchronized void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public synchronized boolean isCancelled() {
		return cancelled;
	}
}