package com.reflexit.magiccards.core.exports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.magiccards.core.xml.MagicXmlStreamHandler;

public class XmlExportDelegate extends AbstractExportDelegatePerLine<IMagicCard> {
	@Override
	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException {
		if (monitor == null)
			monitor = ICoreProgressMonitor.NONE;
		monitor.beginTask("Exporting xml", 100);
		try {
			if (store.getLocation() == Location.NO_WHERE) {
				MagicXmlStreamHandler xmlHanlder = new MagicXmlStreamHandler();
				CardCollectionStoreObject o = new CardCollectionStoreObject();
				o.list = Arrays.asList(store.getElements());
				try {
					xmlHanlder.save(o, stream);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			} else {
				File path = store.getLocation().getFile();
				FileInputStream in = new FileInputStream(path);
				FileUtils.copyStream(in, stream);
				in.close();
			}
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}

	@Override
	public boolean isMultipleLocationSupported() {
		return false;
	}

	@Override
	public boolean isSideboardSupported() {
		return false;
	}
}
