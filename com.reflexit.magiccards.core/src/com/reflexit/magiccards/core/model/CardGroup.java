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
package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Alena
 *
 */
public class CardGroup implements ICardCountable {
	private String name;
	private int fieldIndex;
	private int count;
	private ArrayList children;

	public CardGroup(int fieldIndex, String name) {
		this.fieldIndex = fieldIndex;
		this.name = name;
		this.children = new ArrayList();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getFieldIndex() {
		return this.fieldIndex;
	}

	public Collection<IMagicCard> getChildren() {
		return this.children;
	}

	public void add(Object elem) {
		this.children.add(elem);
		if (elem instanceof MagicCardPhisical) {
			this.count += ((MagicCardPhisical) elem).getCount();
		} else {
			this.count++;
		}
	}

	/**
	 * @param index
	 */
	public Object getChildAtIndex(int index) {
		return this.children.get(index);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getByIndex(int)
	 */
	public String getFieldByIndex(int i) {
		if (i == this.fieldIndex)
			return this.name;
		return "";
	}
}
