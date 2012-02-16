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

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converter for magic card physical
 */
public class MagicCardConvertor implements Converter {
	private ReflectionConverter reflectionConvertor;

	public MagicCardConvertor(Mapper mapper, ReflectionProvider provider) {
		reflectionConvertor = new ReflectionConverter(mapper, provider);
	}

	public boolean canConvert(Class arg0) {
		return arg0.equals(MagicCard.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		MagicCard card = (MagicCard) value;
		ICardField[] values = MagicCardField.allNonTransientFields();
		for (ICardField field : values) {
			Object o = card.getObjectByField(field);
			if (o == null)
				continue; // skip this
			if (o instanceof Float && ((Float) o).floatValue() == 0)
				continue;
			else if (o instanceof Integer && ((Integer) o).intValue() == 0)
				continue;
			else if (o instanceof String && ((String) o).length() == 0)
				continue;
			else if (o instanceof Boolean && ((Boolean) o).booleanValue() == false)
				continue;
			writer.startNode(((MagicCardField) field).getJavaField().getName());
			context.convertAnother(o);
			writer.endNode();
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MagicCard card = new MagicCard();
		return context.convertAnother(card, MagicCard.class, reflectionConvertor);
	}
}
