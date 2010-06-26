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

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.Location;

public class PreviewResult {
	private ArrayList<String[]> values = new ArrayList<String[]>();
	private ICardField[] fields = new ICardField[0];
	private ReportType type;
	private Exception error;
	private Location location;

	/**
	 * @param error the error to set
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
	 * @param fields the fields to set
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
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param type the type to set
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
	 * @param values the values to set
	 */
	public void setValues(ArrayList<String[]> values) {
		this.values = values;
	}

	/**
	 * @return the values
	 */
	public ArrayList<String[]> getValues() {
		return values;
	}
}