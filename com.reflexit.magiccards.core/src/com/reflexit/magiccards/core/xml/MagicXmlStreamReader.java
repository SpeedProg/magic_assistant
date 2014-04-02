package com.reflexit.magiccards.core.xml;

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
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
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
		HashMap<String, MagicCardField> mcpFields = new HashMap<String, MagicCardField>(
				MagicCardField.values().length);
		HashMap<String, MagicCardField> mcFields = new HashMap<String, MagicCardField>(MagicCardField.values().length);
		private Locator locator;

		public MagicHandler(CardCollectionStoreObject object) {
			store = object;
			for (MagicCardField f : MagicCardField.values()) {
				if (!f.isTransient())
					mcpFields.put(f.getTag(), f);
			}
			for (MagicCardField f : MagicCardField.values()) {
				if (!f.isTransient())
					mcFields.put(f.getTag(), f);
			}
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
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
			try {
				String ttStr = text != null ? text.toString().trim() : null;
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
						store.name = ttStr;
						break;
					case key:
						store.key = ttStr;
						break;
					case comment:
						store.comment = ttStr;
						break;
					case type:
						store.type = ttStr;
						break;
					case entry:
						cardm.setProperty(key, value);
						break;
					case string:
						if (key == null)
							key = ttStr;
						else
							value = ttStr;
						break;
					case property:
						break;
					case properties:
						break;
					case fake: {
						switch (states.peek()) {
							case mc: {
								MagicCardField field = mcFields.get(last);
								cardm.setObjectByField(field, ttStr);
								break;
							}
							case card: {
								MagicCardField field = mcFields.get(last);
								cardp.getBase().setObjectByField(field, ttStr);
								break;
							}
							case mcp: {
								MagicCardField field = mcpFields.get(last);
								cardp.setObjectByField(field, ttStr);
								break;
							}
							default:
								break;
						}
						break;
					}
					case cards:
						break;
					case list:
						break;
				}
			} catch (Exception e) {
				// System.err.println("error at " + locator.getLineNumber());
				throw new SAXParseException(e.getMessage(), locator);
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
			long size = file.length();
			if (size > Integer.MAX_VALUE)
				throw new IllegalArgumentException("File " + file + " is to big " + size);
			byte buffer[] = new byte[(int) size];
			int len = FileUtils.readFileAsBytes(file, buffer);
			InputStream st = new ByteArrayInputStream(buffer, 0, len);
			CardCollectionStoreObject object = load(st);
			object.file = file;
			return object;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Cannot read " + file, e);
		}
	}

	public CardCollectionStoreObject load(InputStream st) throws IOException {
		try {
			CardCollectionStoreObject object = new CardCollectionStoreObject();
			SAXParser parser = factory.newSAXParser();
			MagicHandler handler = new MagicHandler(object);
			parser.parse(st, handler);
			return object;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Cannot read stream", e);
		}
	}

	public static void main(String[] args) throws IOException {
		String set = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\MagicDB\\Saviors_of_Kamigawa.xml";
		// String main =
		// "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\Collections\\xxx.xml";
		CardCollectionStoreObject o = new MagicXmlStreamReader().load(new File(set));
		o.file = new File("c:/tmp/test1.xml");
		new MagicXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(new BufferedReader(new FileReader(o.file)));
		System.out.println(x);
	}
}
