package com.reflexit.magiccards.core.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;

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
		private int forTradeCount = 0;
		String last = null;
		StringBuffer text = new StringBuffer();
		String key;
		String value;
		HashMap<String, MagicCardField> mcpFields = new HashMap<String, MagicCardField>(
				MagicCardField.values().length);
		private Locator locator;

		public MagicHandler(CardCollectionStoreObject object) {
			store = object;
			for (MagicCardField f : MagicCardField.values()) {
				if (!f.isTransient())
					mcpFields.put(f.getTag(), f);
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
					forTradeCount = 0;
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
				String ttStr = text.toString().trim();
				switch (state) {
					case card:
						break;
					case mcp:
						add(cardp);
						break;
					case mc:
						add(cardm);
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
						if (key != null && !key.isEmpty()) {
							ICardField field = MagicCardField.fieldByName(key);
							if (field == null)
								MagicLogger.log("Uknown property " + key);
							else
								cardm.set(field, StringCache.intern(value));
						}
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
						try {
							switch (states.peek()) {
								case mc: {
									MagicCardField field = mcpFields.get(last);
									if (field == null)
										MagicLogger.log("Uknown element " + last);
									else
										cardm.set(field, StringCache.intern(ttStr));
									break;
								}
								case card:
								case mcp: {
									MagicCardField field = mcpFields.get(last);
									// transient field but should read it for backward compatibity
									if (field == null && MagicCardField.FORTRADECOUNT.getTag().equals(last)) {
										forTradeCount = Integer.valueOf(ttStr);
										cardp.set(MagicCardField.FORTRADECOUNT, forTradeCount);
										break;
									}
									if (field == null)
										MagicLogger.log("Uknown element " + last);
									else
										cardp.set(field, StringCache.intern(ttStr));
									break;
								}
								default:
									break;
							}
						} catch (Exception e) {
							// recover what we can, do not abort
							MagicLogger.log(e);
						}
						break;
					}
					case cards:
						break;
					case list:
						break;
				}
			} catch (Exception e) {
				MagicLogger.log(e);
				throw new SAXParseException(e.getMessage(), locator);
			}
			text.delete(0, text.length());
			state = states.pop();
		}

		public void add(MagicCardPhysical mcp) {
			store.list.add(mcp);
		}

		public void add(MagicCard mc) {
			store.list.add(mc);
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
			BufferedInputStream st = new BufferedInputStream(new FileInputStream(file),
					FileUtils.DEFAULT_BUFFER_SIZE);
			CardCollectionStoreObject object = load(st);
			object.file = file;
			st.close();
			return object;
		} catch (Exception e) {
			throw new IOException("Cannot read file: " + file, e);
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
			throw new IOException(e);
		}
	}

	public static void main(String[] args) throws IOException {
		String set = "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\MagicDB\\Saviors_of_Kamigawa.xml";
		// String main =
		// "C:\\Develop\\magic\\runtime-magic.product-init\\magiccards\\Collections\\xxx.xml";
		CardCollectionStoreObject o = new MagicXmlStreamReader().load(new File(set));
		o.file = new File("c:/tmp/test1.xml");
		new MagicXmlStreamWriter().write(o);
		String x = FileUtils.readFileAsString(o.file);
		System.out.println(x);
	}
}
