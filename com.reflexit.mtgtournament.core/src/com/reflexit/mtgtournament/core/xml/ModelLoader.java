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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
		xstream.alias("playerdummy", Player.DUMMY.getClass());
		xstream.alias("pRound", PlayerRoundInfo.class);
		xstream.alias("pTour", PlayerTourInfo.class);
		xstream.alias("table", TableInfo.class);
		xstream.alias("round", Round.class);
		xstream.alias("tournament", Tournament.class);
		xstream.alias("playerList", PlayerList.class);
		xstream.setClassLoader(ModelLoader.class.getClassLoader());
		xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
		//xstream.useAttributeFor(Player.class, "id");
		//xstream.aliasSystemAttribute("refid", "id");
	}

	public static Object load(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		Object object = load(is);
		is.close();
		return object;
	}

	public static Object load(InputStream is) {
		Object object = xstream.fromXML(new InputStreamReader(is, Charset.forName("utf-8")));
		return object;
	}

	public static Object load(String str) {
		Object object = xstream.fromXML(new InputStreamReader(new ByteArrayInputStream(str.getBytes()),
				Charset.forName("utf-8")));
		return object;
	}

	public static void save(Object o, File file) throws FileNotFoundException {
		OutputStream out = new FileOutputStream(file);
		save(o, out);
		try {
			out.close();
		} catch (IOException e) {
			// ignore
		}
	}

	public static void save(Object o, OutputStream out) {
		OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("utf-8"));
		xstream.toXML(o, writer);
	}

	public static String saveToString(Object o) {
		ByteArrayOutputStream ar;
		save(o, ar = new ByteArrayOutputStream());
		return ar.toString();
	}
}
