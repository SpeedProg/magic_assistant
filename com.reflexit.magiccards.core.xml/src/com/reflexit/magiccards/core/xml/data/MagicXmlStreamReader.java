package com.reflexit.magiccards.core.xml.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class MagicXmlStreamReader {
	private static SAXParserFactory factory = SAXParserFactory.newInstance();

	static enum Tag {
		cards,
		list,
		properties,
		property,
		mc,
		mcp,
		card,
		name,
		key,
		comment,
		type,
		entry,
		string,
		fake
	}

	static class MagicHandler extends DefaultHandler {
		CardCollectionStoreObject store;
		Stack<Tag> states = new Stack<Tag>();
		Tag state = Tag.cards;
		private MagicCard cardm;
		private MagicCardPhysical cardp;
		String last = null;
		StringBuffer text = new StringBuffer();
		String key;
		String value;
		HashMap<String, MagicCardFieldPhysical> mcpFields = new HashMap<String, MagicCardFieldPhysical>(
				MagicCardFieldPhysical.values().length);
		HashMap<String, MagicCardField> mcFields = new HashMap<String, MagicCardField>(MagicCardField.values().length);

		public MagicHandler(CardCollectionStoreObject object) {
			store = object;
			for (MagicCardFieldPhysical f : MagicCardFieldPhysical.values()) {
				if (!f.isTransient())
					mcpFields.put(f.getJavaField().getName(), f);
			}
			for (MagicCardField f : MagicCardField.values()) {
				if (!f.isTransient())
					mcFields.put(f.getJavaField().getName(), f);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			last = qName;
			Tag current = Tag.fake;
			text.delete(0, text.length());
			switch (state) {
				case mc:
					if (qName.equals(Tag.properties.toString())) {
						current = Tag.properties;
					}
					break;
				case card:
					break;
				case mcp:
					if (qName.equals(Tag.card.toString())) {
						current = Tag.card;
					}
					break;
				default:
					current = Tag.valueOf(qName);
					break;
			}
			switch (current) {
				case list:
					store.list = new ArrayList<IMagicCard>();
					break;
				case mc:
					cardm = new MagicCard();
					break;
				case mcp:
					cardp = new MagicCardPhysical(new MagicCard(), null);
					break;
				case entry:
					if (state == Tag.properties) {
						key = null;
						value = null;
					}
					break;
				case property:
					String name = attributes.getValue("name");
					String value = attributes.getValue("value");
					store.properties.setProperty(name, value);
					break;
				default:
					break;
			}
			states.push(state);
			state = current;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (state) {
				case card:
					break;
				case mcp:
					store.list.add(cardp);
					break;
				case mc:
					store.list.add(cardm);
					break;
				case name:
					store.name = text.toString();
					break;
				case key:
					store.key = text.toString();
					break;
				case comment:
					store.comment = text.toString();
					break;
				case type:
					store.type = text.toString();
					break;
				case entry:
					cardm.setProperty(key, value);
					break;
				case string:
					if (key == null)
						key = text.toString();
					else
						value = text.toString();
					break;
				case properties:
					break;
				case fake: {
					switch (states.peek()) {
						case mc: {
							MagicCardField field = mcFields.get(last);
							cardm.setObjectByField(field, text.toString());
							break;
						}
						case card: {
							MagicCardField field = mcFields.get(last);
							cardp.getBase().setObjectByField(field, text.toString());
							break;
						}
						case mcp: {
							MagicCardFieldPhysical field = mcpFields.get(last);
							cardp.setObjectByField(field, text.toString());
							break;
						}
						default:
							break;
					}
					break;
				}
			}
			text.delete(0, text.length());
			state = states.pop();
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			switch (state) {
				case name:
				case key:
				case comment:
				case type:
				case string:
				case fake:
					text.append(ch, start, length);
					break;
				default:
					break;
			}
		}
	}

	public CardCollectionStoreObject load(File file) throws IOException {
		try {
			CardCollectionStoreObject object = new CardCollectionStoreObject();
			object.file = file;
			SAXParser parser = factory.newSAXParser();
			MagicHandler handler = new MagicHandler(object);
			long size = object.file.length();
			if (size > Integer.MAX_VALUE)
				throw new IllegalArgumentException("File " + file + " is to big " + size);
			byte buffer[] = new byte[(int) size];
			int len = FileUtils.readFileAsBytes(file, buffer);
			InputStream st = new ByteArrayInputStream(buffer, 0, len);
			parser.parse(st, handler);
			return object;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Cannot read " + file, e);
		}
	}

	public static void main(String[] args) throws IOException {
		String set = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\MagicDB\\Saviors_of_Kamigawa.xml";
		String main = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\Collections\\xxx.xml";
		CardCollectionStoreObject o = new MagicXmlStreamReader().load(new File(set));
		o.file = new File("c:/tmp/test1.xml");
		new MagicXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(new BufferedReader(new FileReader(o.file)));
		System.out.println(x);
	}
}
