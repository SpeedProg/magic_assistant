/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Alena
 *
 */
public class ImageCache {
	public static URL getCachedURL(URL externalURL, String localFile) throws IOException {
		URL localUrl = new File(localFile).toURL();
		if (new File(localFile).exists()) {
			return localUrl;
		}
		try {
			InputStream st = externalURL.openStream();
			ImageCache.saveStream(localFile, st);
			st.close();
			return localUrl;
		} catch (IOException e) {
			throw e;
		}
	}

	public static void saveStream(String file, InputStream openStream) throws IOException {
		new File(file).getParentFile().mkdirs();
		OutputStream st = new FileOutputStream(file);
		byte[] bytes = new byte[1024 * 4];
		int k;
		while ((k = openStream.read(bytes)) > 0) {
			st.write(bytes, 0, k);
		}
		st.close();
	}
}
