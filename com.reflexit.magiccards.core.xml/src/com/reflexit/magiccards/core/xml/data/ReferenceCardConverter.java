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
package com.reflexit.magiccards.core.xml.data;

import com.reflexit.magiccards.core.model.MagicCard;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Custom convertor to store less data in xml
 */
public class ReferenceCardConverter implements Converter {
	public boolean canConvert(Class clazz) {
		return clazz.equals(MagicCard.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		MagicCard card = (MagicCard) value;
		writer.startNode("id");
		writer.setValue(String.valueOf(card.getCardId()));
		writer.endNode();
		writer.startNode("name");
		writer.setValue(String.valueOf(card.getName()));
		writer.endNode();
		writer.startNode("edition");
		writer.setValue(String.valueOf(card.getSet()));
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MagicCard card = new MagicCard();
		return context.convertAnother(card, MagicCard.class);
	}
}
