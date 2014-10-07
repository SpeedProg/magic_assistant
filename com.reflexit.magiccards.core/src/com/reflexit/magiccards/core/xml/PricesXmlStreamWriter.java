package com.reflexit.magiccards.core.xml;

import gnu.trove.map.TIntFloatMap;
import gnu.trove.procedure.TIntFloatProcedure;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;

public class PricesXmlStreamWriter {
	protected MyXMLStreamWriter writer;

	public synchronized void write(IPriceProviderStore object, OutputStream st) throws IOException {
		OutputStream out = new BufferedOutputStream(st, 256 * 1024);
		try {
			writer = new MyXMLStreamWriter(st);
			try {
				writer.startEl("cards");
				writer.el("name", object.getName());
				Properties properties = object.getProperties();
				marshal(properties);
				// list
				TIntFloatMap map = object.getPriceMap();
				if (map != null) {
					writer.startEl("list");
					map.forEachEntry(new TIntFloatProcedure() {
						@Override
						public boolean execute(int key, float value) {
							try {
								if (value != 0) {
									writer.startEl("mc");
									writer.el("id", String.valueOf(key));
									writer.el("dbprice", String.valueOf(value));
									writer.endEl();
								}
							} catch (XMLStreamException e) {
								MagicLogger.log(e);
								return false;
							}
							return true;
						}
					});
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

	public synchronized void write(IPriceProviderStore object) throws IOException {
		write(object, new FileOutputStream(getPricesFile(object)));
	}

	public static File getPricesFile(IPriceProviderStore provider) {
		Location loc = Location.createLocationFromSet(provider.getName());
		File pricesDir = DataManager.getInstance().getPricesDir();
		File file = new File(pricesDir, loc.getBaseFileName());
		return file;
	}
}
