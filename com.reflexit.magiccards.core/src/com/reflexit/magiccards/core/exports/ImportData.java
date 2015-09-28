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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class ImportData {
	private ArrayList<ICard> toImport = new ArrayList<ICard>();
	private ICardField[] fields = new ICardField[0];
	private ReportType type = ImportExportFactory.TEXT_DECK_CLASSIC;
	private Throwable error = null;
	private String text = "";
	private Location location = Location.createLocation("preview");
	private boolean virtual = false;
	private ImportSource importSource;
	private LinkedHashMap<String, Object> props = new LinkedHashMap<>();

	public ImportData(boolean virtual2, Location location2, String line) {
		virtual = virtual2;
		location = location2;
		text = line;
	}

	public ImportData() {
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(Throwable error) {
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public Throwable getError() {
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

	public int size() {
		return toImport.size();
	}

	public int getErrorCount() {
		int errorCount = 0;
		if (toImport.size() > 0) {
			for (Iterator<ICard> iterator = toImport.iterator(); iterator.hasNext();) {
				ICard card = iterator.next();
				if (card instanceof MagicCardPhysical && ((MagicCardPhysical) card).getError() != null) {
					errorCount++;
				}
			}
		}
		return errorCount;
	}

	public void clear() {
		toImport.clear();
		error = null;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isOk() {
		return getError() == null && toImport.size() > 0;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public Map<String, Object> getProperties() {
		return props;
	}

	public <T> T getProperty(String key) {
		return (T) getProperties().get(key);
	}

	public void setProperty(String key, Object value) {
		getProperties().put(key, value);
	}

	public ImportSource getImportSource() {
		return importSource;
	}

	public void setImportSource(ImportSource importSource) {
		this.importSource = importSource;
	}
}
