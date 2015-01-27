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
package com.reflexit.magiccards.core.exports;

import java.util.ArrayList;
import java.util.List;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class ImportResult {
	private ArrayList<ICard> toImport = new ArrayList<ICard>();
	private ICardField[] fields = new ICardField[0];
	private ReportType type;
	private Exception error;

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(Exception error) {
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public Exception getError() {
		return error;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(ICardField[] fields) {
		this.fields = fields;
	}

	/**
	 * @return the fields
	 */
	public ICardField[] getFields() {
		return fields;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ReportType type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public ReportType getType() {
		return type;
	}

	/**
	 * @param toImport
	 *            the toImport to set
	 */
	public void add(ICard card) {
		toImport.add(card);
	}

	/**
	 * @return the toImport
	 */
	public List<? extends ICard> getList() {
		return toImport;
	}

	public void clear() {
		toImport.clear();
	}
}