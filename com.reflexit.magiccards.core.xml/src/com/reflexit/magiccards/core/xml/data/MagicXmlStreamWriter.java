package com.reflexit.magiccards.core.xml.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;

public class MagicXmlStreamWriter {
	private XMLStreamWriter writer;

	MagicXmlStreamWriter() {
	}

	public synchronized void write(CardCollectionStoreObject object) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(object.file), 256 * 1024);
		try {
			writer = new IndentingXMLStreamWriter(XMLStreamWriterFactory.create(out, Charset.forName("utf-8").toString()));
			try {
				writer.writeStartElement("cards");
				writeSimpleElement("name", object.name);
				writeSimpleElement("key", object.key);
				writeSimpleElement("comment", object.comment);
				writeSimpleElement("type", object.type);
				Properties properties = object.properties;
				marshal(properties);
				// list
				if (object.list != null) {
					writer.writeStartElement("list");
					for (Iterator iterator = object.list.iterator(); iterator.hasNext();) {
						Object o = iterator.next();
						if (o instanceof MagicCardPhysical) {
							writer.writeStartElement("mcp");
							marshal((MagicCardPhysical) o);
							writer.writeEndElement();
						} else if (o instanceof MagicCard) {
							writer.writeStartElement("mc");
							marshal((MagicCard) o);
							writer.writeEndElement();
						}
					}
					writer.writeEndElement();
				}
				writer.writeEndElement();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			out.close();
		}
	}

	public void marshal(Properties properties) throws XMLStreamException {
		if (properties != null && properties.size() > 0) {
			writer.writeStartElement("properties");
			for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				writer.writeEmptyElement("property");
				writer.writeAttribute("name", key);
				writer.writeAttribute("value", properties.getProperty(key));
			}
			writer.writeEndElement();
		}
	}

	public void marshal(MagicCardPhysical card) throws XMLStreamException {
		writer.writeStartElement("card");
		marshalReference(card.getCard());
		writer.writeEndElement();
		MagicCardFieldPhysical[] values = MagicCardFieldPhysical.values();
		for (MagicCardFieldPhysical field : values) {
			if (field.isTransient())
				continue;
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
			writer.writeStartElement(field.getJavaField().getName());
			writer.writeCharacters(String.valueOf(o));
			writer.writeEndElement();
		}
	}

	public void marshalReference(MagicCard card) throws XMLStreamException {
		writer.writeStartElement("id");
		writer.writeCharacters(String.valueOf(card.getCardId()));
		writer.writeEndElement();
		writer.writeStartElement("name");
		writer.writeCharacters(String.valueOf(card.getName()));
		writer.writeEndElement();
		writer.writeStartElement("edition");
		writer.writeCharacters(String.valueOf(card.getSet()));
		writer.writeEndElement();
	}

	public void marshal(MagicCard card) throws XMLStreamException {
		ICardField[] values = MagicCardField.allNonTransientFields();
		for (ICardField field : values) {
			Object o = card.getObjectByField(field);
			if (o == null)
				continue; // skip this
			if (field == MagicCardField.PROPERTIES) {
				writer.writeStartElement("properties");
				marshalMap((Map) o);
				writer.writeEndElement();
			} else {
				if (o instanceof Float && ((Float) o).floatValue() == 0)
					continue;
				else if (o instanceof Integer && ((Integer) o).intValue() == 0)
					continue;
				else if (o instanceof String && ((String) o).length() == 0)
					continue;
				else if (o instanceof Boolean && ((Boolean) o).booleanValue() == false)
					continue;
				else if (field == MagicCardField.LANG && o.equals("English"))
					continue;
				writer.writeStartElement(((MagicCardField) field).getJavaField().getName());
				String text = String.valueOf(o);
				writer.writeCharacters(text);
				writer.writeEndElement();
			}
		}
	}

	private void marshalMap(Map<String, String> properties) throws XMLStreamException {
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			writer.writeStartElement("entry");
			writeSimpleElement("string", key);
			writeSimpleElement("string", properties.get(key));
			writer.writeEndElement();
		}
	}

	private void writeSimpleElement(String attname, String value) throws XMLStreamException {
		writer.writeStartElement(attname);
		writer.writeCharacters(value);
		writer.writeEndElement();
	}

	public static void main(String[] args) throws IOException {
		String main = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\Collections\\xxx.xml";
		String alara = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\MagicDB\\Alara_Reborn.xml";
		CardCollectionStoreObject o = CardCollectionStoreObject.initFromFile(new File(main));
		o.file = new File("c:/tmp/test.xml");
		new MagicXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(new BufferedReader(new FileReader(o.file)));
		System.out.println(x);
	}
}
