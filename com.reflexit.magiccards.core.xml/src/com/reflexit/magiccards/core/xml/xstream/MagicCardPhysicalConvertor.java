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
package com.reflexit.magiccards.core.xml.xstream;

import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
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
		return arg0.equals(MagicCardPhysical.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		MagicCardPhysical card = (MagicCardPhysical) value;
		writer.startNode("card");
		context.convertAnother(card.getCard(), new ReferenceCardConverter());
		writer.endNode();
		// count
		writer.startNode(MagicCardFieldPhysical.COUNT.getTag());
		context.convertAnother(card.getCount());
		writer.endNode();
		// ownership
		if (card.isOwn()) {
			writer.startNode(MagicCardFieldPhysical.OWNERSHIP.getTag());
			context.convertAnother(Boolean.TRUE);
			writer.endNode();
		}
		// other fields
		if (card.getProperties() != null) {
			writer.startNode("properties");
			context.convertAnother(card.getProperties());
			writer.endNode();
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MagicCardPhysical card = new MagicCardPhysical(null, null);
		return context.convertAnother(card, MagicCardPhysical.class, reflectionConvertor);
	}
}
