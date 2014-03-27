package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.net.URL;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.exports.ClassicNoXExportDelegate;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class AbstractPriceProvider implements IPriceProvider {
	protected String name;

	public AbstractPriceProvider(String name) {
		this.name = name;
	}

	@Override
	public void updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		throw new MagicException("This price provider " + name + " does not support interactive update");
	}

	@Override
	public URL getURL() {
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public URL buy(Iterable<IMagicCard> cards) {
		return null;
	}

	public String export(Iterable<IMagicCard> cards) {
		String res = new ClassicNoXExportDelegate().export(cards);
		return res;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPriceProvider other = (AbstractPriceProvider) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
