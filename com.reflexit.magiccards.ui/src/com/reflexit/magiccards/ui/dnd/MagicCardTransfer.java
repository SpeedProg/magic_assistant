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
package com.reflexit.magiccards.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.thoughtworks.xstream.XStream;

/**
 * Class for serializing gadgets to/from a byte array
 */
public class MagicCardTransfer extends ByteArrayTransfer {
	private static MagicCardTransfer instance = new MagicCardTransfer();
	private static final String TYPE_NAME = "magic-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Returns the singleton gadget transfer instance.
	 */
	public static MagicCardTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private MagicCardTransfer() {
	}

	protected IMagicCard[] fromByteArray(byte[] bytes) {
		InputStreamReader in;
		try {
			in = new InputStreamReader(new ByteArrayInputStream(bytes), FileUtils.UTF8);
		} catch (UnsupportedEncodingException e) {
			MagicUIActivator.log(e);
			return new MagicCard[0];
		}
		try {
			XStream xs = DataManager.getXStream();
			LinkedHashMap<IMagicCard, Location> res = (LinkedHashMap<IMagicCard, Location>) xs.fromXML(in);
			IMagicCard[] arr = new IMagicCard[res.size()];
			int i = 0;
			for (Iterator iterator = res.keySet().iterator(); iterator.hasNext(); i++) {
				IMagicCard card = (IMagicCard) iterator.next();
				if (card instanceof MagicCardPhisical) {
					((MagicCardPhisical) card).setLocation(res.get(card));
				}
				arr[i] = card;
			}
			return arr;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// ok
			}
		}
	}

	/*
	 * Method declared on Transfer.
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/*
	 * Method declared on Transfer.
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	/*
	 * Method declared on Transfer.
	 */
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = toByteArray((IMagicCard[]) object);
		if (bytes != null)
			super.javaToNative(bytes, transferData);
	}

	/*
	 * Method declared on Transfer.
	 */
	@Override
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return fromByteArray(bytes);
	}

	protected byte[] toByteArray(IMagicCard[] gadgets) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			Writer out = new OutputStreamWriter(byteOut, FileUtils.UTF8);
			XStream xs = DataManager.getXStream();
			LinkedHashMap<IMagicCard, Location> cards = new LinkedHashMap<IMagicCard, Location>();
			for (IMagicCard c : gadgets) {
				Location loc = Location.NO_WHERE;
				if (c instanceof MagicCardPhisical)
					loc = ((MagicCardPhisical) c).getLocation();
				cards.put(c, loc);
			}
			xs.toXML(cards, out);
			try {
				out.close();
			} catch (IOException e) {
				// ok
			}
		} catch (Exception e) {
			MagicUIActivator.log(e);
		}
		byte[] bytes = byteOut.toByteArray();
		return bytes;
	}
}
