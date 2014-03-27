package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class CustomPriceProvider extends AbstractPriceProvider {
	public CustomPriceProvider(String name) {
		super(name);
	}

	@Override
	public void updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		throw new MagicException("This custom price provider " + name + " does not support interactive update");
	}
}
