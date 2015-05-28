package com.reflexit.magiccards.core.xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class MagicXmlStreamWriter {
	protected MyXMLStreamWriter writer;

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
				writer.writeDirect("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
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
					for (Iterator iterator = object.list.iterator(); iterator.hasNext();) {
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
			writer = null;
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
		MagicCardField[] values = MagicCardField.values();
		for (MagicCardField field : values) {
			if (!field.isPhysical())
				continue;
			if (field.isTransient())
				continue;
			Object o = card.get(field);
			if (o == null)
				continue; // skip this
			if (field != MagicCardField.COUNT) {
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
		// special hack for old forTrade
		int forTrade = card.getForTrade();
		if (forTrade > 0 && card.isForTrade() == false) {
			// old style
			writer.el(MagicCardField.FORTRADECOUNT.getTag(), String.valueOf(forTrade));
		}
	}

	public void marshalReference(MagicCard card) throws XMLStreamException {
		writer.el("id", String.valueOf(card.getCardId()));
		writer.el("name", String.valueOf(card.getName()));
		writer.el("edition", String.valueOf(card.getSet()));
	}

	private static ICardField[] values = MagicCardField.allNonTransientFields(false);

	public void marshal(MagicCard card) throws XMLStreamException {
		for (ICardField field : values) {
			Object o = card.get(field);
			if (o == null)
				continue; // skip this
			if (field == MagicCardField.PROPERTIES) {
				writer.startEl("properties");
				marshalMap((Map) o);
				writer.endEl();
			} else if (field == MagicCardField.DBPRICE) {
				// skip this field, prices will be stored separately
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
				writer.el(((MagicCardField) field).getTag(), String.valueOf(o));
			}
		}
	}

	private void marshalMap(Map<String, Object> properties) throws XMLStreamException {
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			writer.startEl("entry");
			writer.el("string", key.toString());
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
		String x = FileUtils.readFileAsString(o.file);
		System.out.println(x);
	}
}
