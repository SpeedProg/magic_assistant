package com.reflexit.magiccards.core.xml;

import gnu.trove.map.hash.TIntFloatHashMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.reflexit.magiccards.core.FileUtils;

public class PricesXmlStreamReader {
	private static SAXParserFactory factory = SAXParserFactory.newInstance();

	static enum Tag {
		cards, list, properties, property, mc, name, comment, dbprice, id, fake,
	}

	static class PricesXmlHandler extends DefaultHandler {
		PriceProviderStoreObject store;
		Stack<Tag> states = new Stack<Tag>();
		Tag state = Tag.cards;
		private int id;
		private float price;
		String last = null;
		StringBuffer text = new StringBuffer();
		String key;
		String value;
		private Locator locator;

		public PricesXmlHandler(PriceProviderStoreObject object) {
			store = object;
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
			try {
				current = Tag.valueOf(qName);
			} catch (IllegalArgumentException e) {
				current = Tag.fake;
			}
			switch (current) {
			case list:
				store.map = new TIntFloatHashMap();
				break;
			case mc:
				id = 0;
				price = 0;
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
				String ttStr = text.toString().trim();
				switch (state) {
				case mc:
					store.map.put(id, price);
					break;
				case name:
					store.name = ttStr;
					break;
				case comment:
					store.comment = ttStr;
					break;
				case property:
					break;
				case properties:
					break;
				case id:
					if (states.peek() == Tag.mc) {
						if (ttStr != null && !ttStr.isEmpty()) {
							id = Integer.valueOf(ttStr);
						}
					}
					break;
				case dbprice:
					if (states.peek() == Tag.mc) {
						if (ttStr != null && !ttStr.isEmpty()) {
							price = Float.valueOf(ttStr);
						}
					}
					break;
				case fake: {
					switch (states.peek()) {
					case mc: {
						if (ttStr != null && !ttStr.isEmpty()) {
							if (last.equals("id"))
								id = Integer.valueOf(ttStr);
							else if (last.equals("dbprice"))
								price = Float.valueOf(ttStr);
						}
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
			case comment:
			case fake:
			case id:
			case dbprice:
				text.append(ch, start, length);
				break;
			default:
				break;
			}
		}
	}

	public PriceProviderStoreObject load(File file) throws IOException {
		try {
			BufferedInputStream st = new BufferedInputStream(new FileInputStream(file), FileUtils.DEFAULT_BUFFER_SIZE);
			PriceProviderStoreObject object = load(st);
			object.file = file;
			st.close();
			return object;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Cannot read " + file, e);
		}
	}

	public PriceProviderStoreObject load(InputStream st) throws IOException {
		try {
			PriceProviderStoreObject object = new PriceProviderStoreObject();
			SAXParser parser = factory.newSAXParser();
			PricesXmlHandler handler = new PricesXmlHandler(object);
			parser.parse(st, handler);
			return object;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Cannot read stream", e);
		}
	}

	public static void main(String[] args) throws IOException {
		String set = "C:\\Develop\\magic\\runtime-magic.product-init1\\magiccards\\MagicDB\\prices\\TCG_Player__Medium_.xml";
		// String main =
		// "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\Collections\\xxx.xml";
		PriceProviderStoreObject o = new PricesXmlStreamReader().load(new File(set));
		o.file = new File("c:/tmp/test1.xml");
		new PricesXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(o.file);
		System.out.println(x);
	}
}
