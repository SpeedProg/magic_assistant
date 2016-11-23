package com.reflexit.magiccards.core.exports;

import java.net.MalformedURLException;
import java.net.URL;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sync.CardCache;

public class PrintProxyHtmlExporMobileDelegate extends PrintProxyHtmlExportDelegate {
	@Override
	protected String getCardImageUrl(IMagicCard card) {
		/*-
		<img src="http://magiccards.info/scans/en/m14/102.jpg" alt="Proxy" />		
		 */
		try {
			URL url = CardCache.createRemoteImageURL(card);
			if (url == null)
				return null;
			return url.toExternalForm();
		} catch (MalformedURLException e) {
			// oki
		}
		return null;
	}
}
