package com.reflexit.magiccards.core.xml;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class MagicXmlStreamWriter {
	private MyXMLStreamWriter writer;

	MagicXmlStreamWriter() {
	}

	public synchronized void write(CardCollectionStoreObject object) throws IOException {
		write(object, new FileOutputStream(object.file));
	}

	public synchronized void write(CardCollectionStoreObject object, OutputStream st) throws IOException {
		OutputStream out = new BufferedOutputStream(st, 256 * 1024);
		try {
			writer = new MyXMLStreamWriter(st);
			try {
				writer.startEl("cards");
				writer.el("name", object.name);
				writer.el("key", object.key);
				writer.el("comment", object.comment);
				writer.el("type", object.type);
				Properties properties = object.properties;
				marshal(properties);
				// list
				if (object.list != null) {
					writer.startEl("list");
					int i = 0;
					for (Iterator iterator = object.list.iterator(); iterator.hasNext(); i++) {
						Object o = iterator.next();
						if (o instanceof MagicCardPhysical) {
							writer.startEl("mcp");
							marshal((MagicCardPhysical) o);
							writer.endEl();
						} else if (o instanceof MagicCard) {
							writer.startEl("mc");
							marshal((MagicCard) o);
							writer.endEl();
						}
					}
					writer.endEl();
				}
				writer.endEl();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			writer.close();
			out.close();
		}
	}

	public void marshal(Properties properties) throws XMLStreamException {
		if (properties != null && properties.size() > 0) {
			writer.startEl("properties");
			for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				writer.lineEl("property", "name", key, "value", properties.getProperty(key));
				// writer.writeDirect("<property name=\"" + key + "\" value=\"" +
				// properties.getProperty(key) + "\"/>");
			}
			writer.endEl();
		}
	}

	public void marshal(MagicCardPhysical card) throws XMLStreamException {
		writer.startEl("card");
		marshalReference(card.getCard());
		writer.endEl();
		MagicCardFieldPhysical[] values = MagicCardFieldPhysical.values();
		for (MagicCardFieldPhysical field : values) {
			if (field.isTransient())
				continue;
			Object o = card.getObjectByField(field);
			if (o == null)
				continue; // skip this
			if (field != MagicCardFieldPhysical.COUNT) {
				if (o instanceof Float && ((Float) o).floatValue() == 0)
					continue;
				else if (o instanceof Integer && ((Integer) o).intValue() == 0)
					continue;
				else if (o instanceof String && ((String) o).length() == 0)
					continue;
				else if (o instanceof Boolean && ((Boolean) o).booleanValue() == false)
					continue;
			}
			writer.el(field.getTag(), String.valueOf(o));
		}
	}

	public void marshalReference(MagicCard card) throws XMLStreamException {
		writer.el("id", String.valueOf(card.getCardId()));
		writer.el("name", String.valueOf(card.getName()));
		writer.el("edition", String.valueOf(card.getSet()));
	}

	public void marshal(MagicCard card) throws XMLStreamException {
		ICardField[] values = MagicCardField.allNonTransientFields();
		for (ICardField field : values) {
			Object o = card.getObjectByField(field);
			if (o == null)
				continue; // skip this
			if (field == MagicCardField.PROPERTIES) {
				writer.startEl("properties");
				marshalMap((Map) o);
				writer.endEl();
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
				writer.startEl(((MagicCardField) field).getJavaField().getName());
				String text = String.valueOf(o);
				writer.data(text);
				writer.endEl();
			}
		}
	}

	private void marshalMap(Map<String, Object> properties) throws XMLStreamException {
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			writer.startEl("entry");
			writer.el("string", key);
			Object value = properties.get(key);
			writer.el("string", value instanceof String ? (String) value : String.valueOf(value));
			writer.endEl();
		}
	}

	public static void main(String[] args) throws IOException {
		String main = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\Collections\\xxx.xml";
		String alara = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\MagicDB\\Alara_Reborn.xml";
		CardCollectionStoreObject o = CardCollectionStoreObject.initFromFile(new File(alara));
		o.file = new File("c:/tmp/test.xml");
		new MagicXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(new BufferedReader(new FileReader(o.file)));
		System.out.println(x);
	}
}
