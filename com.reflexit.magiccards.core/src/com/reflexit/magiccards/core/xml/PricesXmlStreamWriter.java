package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.seller.IPriceProvider;

public class PricesXmlStreamWriter extends MagicXmlStreamWriter {
	@Override
	public void marshal(MagicCard card) throws XMLStreamException {
		writer.ela(MagicCardField.ID.getJavaField().getName(), String.valueOf(card.getCardId()));
		MagicCardField field = MagicCardField.DBPRICE;
		Float o = card.getDbPrice();
		if (o == null)
			return; // skip this
		if (o.floatValue() == 0)
			return;
		String text = String.valueOf(o);
		writer.ela(field.getJavaField().getName(), text);
	}

	public void save(Iterable<IMagicCard> store, IPriceProvider provider) throws IOException {
		CardCollectionStoreObject o = new CardCollectionStoreObject();
		o.name = provider.getName();
		File file = getPricesFile(provider);
		o.type = "dbprice";
		o.list = new ArrayList();
		for (IMagicCard card : store) {
			if (card.getDbPrice() > 0)
				o.list.add(card);
		}
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
