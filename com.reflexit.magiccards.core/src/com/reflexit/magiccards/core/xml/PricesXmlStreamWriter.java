package com.reflexit.magiccards.core.xml;

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
import com.reflexit.magiccards.core.seller.IPriceProvider;

public class PricesXmlStreamWriter {
	protected MyXMLStreamWriter writer;

	public synchronized void write(PriceProviderStoreObject object) throws IOException {
		write(object, new FileOutputStream(object.file));
	}

	public synchronized void write(PriceProviderStoreObject object, OutputStream st) throws IOException {
		OutputStream out = new BufferedOutputStream(st, 256 * 1024);
		try {
			writer = new MyXMLStreamWriter(st);
			try {
				writer.startEl("cards");
				writer.el("name", object.name);
				Properties properties = object.properties;
				marshal(properties);
				// list
				if (object.map != null) {
					writer.startEl("list");
					object.map.forEachEntry(new TIntFloatProcedure() {
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

	public void save(IPriceProvider provider) throws IOException {
		PriceProviderStoreObject o = new PriceProviderStoreObject();
		o.name = provider.getName();
		File file = getPricesFile(provider);
		o.map = DataManager.getDBPriceStore().getPriceMap(provider);
		FileOutputStream stream = new FileOutputStream(file);
		write(o, stream);
		stream.close();
	}

	public static File getPricesFile(IPriceProvider provider) {
		Location loc = Location.createLocationFromSet(provider.getName());
		File pricesDir = getPricesDir();
		File file = new File(pricesDir, loc.getBaseFileName());
		return file;
	}

	public static File getPricesDir() {
		File dir = DataManager.getModelRoot().getMagicDBContainer().getFile();
		File pricesDir = new File(dir, "prices");
		if (!pricesDir.exists())
			pricesDir.mkdirs();
		return pricesDir;
	}
}
