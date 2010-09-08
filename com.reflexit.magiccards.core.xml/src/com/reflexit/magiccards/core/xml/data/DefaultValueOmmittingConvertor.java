package com.reflexit.magiccards.core.xml.data;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * TODO: add description
 */
final class DefaultValueOmmittingConvertor implements Converter {
	private Class clazz;

	public DefaultValueOmmittingConvertor(Class clazz) {
		this.clazz = clazz;
	}

	public boolean canConvert(Class arg0) {
		return arg0.equals(clazz);
	}

	public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext context) {
		return context.convertAnother(null, clazz);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		if (value instanceof Number && ((Number) value).intValue() == 0)
			return;
		if (value instanceof String && ((String) value).length() == 0)
			return;
		context.convertAnother(value);
	}
}