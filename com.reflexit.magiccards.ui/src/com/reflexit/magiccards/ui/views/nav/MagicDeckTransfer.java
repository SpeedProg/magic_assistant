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
package com.reflexit.magiccards.ui.views.nav;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardElement;

/**
 * Class for serializing decks to/from a byte array
 */
public class MagicDeckTransfer extends ByteArrayTransfer {
	private static MagicDeckTransfer instance = new MagicDeckTransfer();
	private static final String TYPE_NAME = "magic-deck-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Returns the singleton gadget transfer instance.
	 */
	public static MagicDeckTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private MagicDeckTransfer() {
	}

	public CardElement[] fromByteArray(byte[] bytes) {
		BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes),
				FileUtils.CHARSET_UTF_8));
		try {
			String name;
			Map<Location, CardElement> locationsMap = DataManager.getInstance().getModelRoot()
					.getLocationsMap();
			ArrayList<CardElement> list = new ArrayList<CardElement>();
			while ((name = in.readLine()) != null) {
				CardElement cardElement = locationsMap.get(Location.valueOf(name));
				list.add(cardElement);
			}
			return list.toArray(new CardElement[list.size()]);
		} catch (IOException e) {
			// hmm
			MagicLogger.log(e);
			return null;
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
		byte[] bytes = toByteArray((CardElement[]) object);
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

	public byte[] toByteArray(CardElement[] decks) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		PrintStream st;
		try {
			st = new PrintStream(byteOut, true, FileUtils.UTF8);
			for (CardElement cardElement : decks) {
				st.print(cardElement.getLocation().getPath());
				st.print('\n');
			}
			st.close();
			// ok
			byte[] bytes = byteOut.toByteArray();
			return bytes;
		} catch (UnsupportedEncodingException e) {
			// ignore
			MagicLogger.log(e);
			return null;
		}
	}
}
