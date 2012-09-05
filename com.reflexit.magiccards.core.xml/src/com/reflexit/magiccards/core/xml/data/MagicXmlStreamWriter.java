package com.reflexit.magiccards.core.xml.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class MagicXmlStreamWriter {
	class MyOutputStreamWriter {
		final Charset UTF_8 = Charset.forName("utf-8");
		OutputStream st;
		int bufSize = 256 * 1024;
		StringBuilder builder = new StringBuilder(bufSize);

		public MyOutputStreamWriter(File file) throws FileNotFoundException {
			st = new FileOutputStream(file);
		}

		public void write(String string) throws IOException {
			builder.append(string);
			if (builder.length() > bufSize)
				dump();
		}

		private void dump() throws IOException {
			st.write(builder.toString().getBytes(UTF_8));
			builder.delete(0, builder.length());
		}

		public void write(char c) throws IOException {
			builder.append(c);
			if (builder.length() > bufSize)
				dump();
		}

		public void close() throws IOException {
			dump();
			st.close();
		}
	}

	class MyXMLStreamWriter {
		private MyOutputStreamWriter out;
		private Stack<String> stack;

		public MyXMLStreamWriter(File file) throws FileNotFoundException {
			this.out = new MyOutputStreamWriter(file);
			this.stack = new Stack<String>();
		}

		public void writeStartElement(String string) throws XMLStreamException {
			try {
				int indent = stack.size();
				for (int i = 0; i < indent; i++) {
					out.write("  ");
				}
				out.write('<');
				out.write(string);
				out.write('>');
				stack.push(string);
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
		}

		public void writeEndElement() throws XMLStreamException {
			try {
				String string = stack.pop();
				// int indent = stack.size();
				// for (int i = 0; i < indent; i++) {
				// out.write("  ");
				// }
				out.write("</");
				out.write(string);
				out.write('>');
				out.write('\n');
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
		}

		public void writeCharacters(String string) throws XMLStreamException {
			try {
				if (string != null) {
					if (string.indexOf('&') >= 0) {
						string = string.replaceAll("&", "\\&amp;");
					}
					if (string.indexOf('<') >= 0) {
						string = string.replaceAll("<", "&lt;");
					}
					if (string.indexOf('>') >= 0) {
						string = string.replaceAll(">", "&gt;");
					}
					out.write(string);
				}
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
		}

		public void writeDirect(String string) throws XMLStreamException {
			try {
				int indent = stack.size();
				for (int i = 0; i < indent; i++) {
					out.write("  ");
				}
				out.write(string);
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
		}

		public void close() {
			try {
				out.close();
			} catch (IOException e) {
				// ignore
				e.printStackTrace();
			}
		}
	}

	class XMLStreamException extends Exception {
		public XMLStreamException(Throwable e) {
			super(e);
		}
	}

	private MyXMLStreamWriter writer;

	MagicXmlStreamWriter() {
	}

	public synchronized void write(CardCollectionStoreObject object) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(object.file), 256 * 1024);
		try {
			writer = new MyXMLStreamWriter(object.file);
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
					int i = 0;
					for (Iterator iterator = object.list.iterator(); iterator.hasNext(); i++) {
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
			writer.close();
			out.close();
		}
	}

	public void marshal(Properties properties) throws XMLStreamException {
		if (properties != null && properties.size() > 0) {
			writer.writeStartElement("properties");
			for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				// writer.writeEmptyElement("property");
				// writer.writeAttribute("name", key);
				// writer.writeAttribute("value", properties.getProperty(key));
				writer.writeDirect("<property name=\"" + key + "\" value=\"" + properties.getProperty(key) + "\"/>");
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
		CardCollectionStoreObject o = CardCollectionStoreObject.initFromFile(new File(alara));
		o.file = new File("c:/tmp/test.xml");
		new MagicXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(new BufferedReader(new FileReader(o.file)));
		System.out.println(x);
	}
}
