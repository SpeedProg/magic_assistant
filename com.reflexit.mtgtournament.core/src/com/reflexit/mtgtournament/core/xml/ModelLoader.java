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
package com.reflexit.mtgtournament.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerList;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.thoughtworks.xstream.XStream;

public class ModelLoader {
	public transient static XStream xstream;
	static {
		xstream = new XStream();
		xstream.alias("player", Player.class);
		xstream.alias("pRound", PlayerRoundInfo.class);
		xstream.alias("pTour", PlayerTourInfo.class);
		xstream.alias("table", TableInfo.class);
		xstream.alias("round", Round.class);
		xstream.alias("tournament", Tournament.class);
		xstream.alias("playerList", PlayerList.class);
		xstream.setClassLoader(ModelLoader.class.getClassLoader());
	}

	public static Object load(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		Charset encoding = Charset.forName("utf-8");
		Object object = xstream.fromXML(new InputStreamReader(is, encoding));
		is.close();
		return object;
	}

	public static void save(Object o, File file) throws FileNotFoundException {
		OutputStream out = new FileOutputStream(file);
		xstream.toXML(o, new OutputStreamWriter(out, Charset.forName("utf-8")));
		try {
			out.close();
		} catch (IOException e) {
			// ignore
		}
	}
}