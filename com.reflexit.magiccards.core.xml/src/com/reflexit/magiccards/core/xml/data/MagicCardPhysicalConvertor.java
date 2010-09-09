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

import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
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
public class MagicCardPhysicalConvertor implements Converter {
	private ReflectionConverter reflectionConvertor;

	public MagicCardPhysicalConvertor(Mapper mapper, ReflectionProvider provider) {
		reflectionConvertor = new ReflectionConverter(mapper, provider);
	}

	public boolean canConvert(Class arg0) {
		return arg0.equals(MagicCardPhisical.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		MagicCardPhisical card = (MagicCardPhisical) value;
		writer.startNode("card");
		context.convertAnother(card.getCard(), new ReferenceCardConverter());
		writer.endNode();
		MagicCardFieldPhysical[] values = MagicCardFieldPhysical.values();
		for (MagicCardFieldPhysical field : values) {
			if (field.isTransient())
				continue;
			Object o = card.getObjectByField(field);
			if (o == null)
				continue; // skip this
			if (o instanceof Number && ((Number) o).intValue() == 0)
				continue;
			if (o instanceof String && ((String) o).length() == 0)
				continue;
			if (o instanceof Boolean && ((Boolean) o).booleanValue() == false)
				continue;
			writer.startNode(field.getJavaField().getName());
			context.convertAnother(o);
			writer.endNode();
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MagicCardPhisical card = new MagicCardPhisical(null, null);
		return context.convertAnother(card, MagicCardPhisical.class, reflectionConvertor);
	}
}
