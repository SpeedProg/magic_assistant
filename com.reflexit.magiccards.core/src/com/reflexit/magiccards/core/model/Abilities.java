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
 * TODO: add description
 */
public class Abilities {
	static class TextSeach implements ISearchableProperty {
		public String getIdPrefix() {
			return FilterHelper.TEXT_LINE;
		}

		public Collection getIds() {
			Collection list = new ArrayList<String>();
			list.add(FilterHelper.TEXT_LINE);
			list.add(FilterHelper.TEXT_LINE_2);
			list.add(FilterHelper.TEXT_LINE_3);
			return list;
		}

		public String getNameById(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		public Collection getNames() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static class TextSeachNot implements ISearchableProperty {
		public String getIdPrefix() {
			return FilterHelper.TEXT_LINE;
		}

		public Collection getIds() {
			Collection list = new ArrayList<String>();
			list.add(FilterHelper.TEXT_NOT_1);
			list.add(FilterHelper.TEXT_NOT_2);
			list.add(FilterHelper.TEXT_NOT_3);
			return list;
		}

		public String getNameById(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		public Collection getNames() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static ISearchableProperty getTextFields() {
		return new TextSeach();
	}

	static ISearchableProperty getTextNotFields() {
		return new TextSeachNot();
	}
}
