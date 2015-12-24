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
import java.util.Arrays;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.magiccards.core.xml.MagicXmlStreamHandler;
import com.reflexit.magiccards.ui.MagicUIActivator;

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

	public IMagicCard[] fromByteArray(byte[] bytes) {
		if (bytes == null)
			return null;
		try {
			CardCollectionStoreObject object = new MagicXmlStreamHandler().load(new ByteArrayInputStream(bytes));
			return object.list.toArray(new IMagicCard[object.list.size()]);
		} catch (IOException e) {
			MagicLogger.log(e);
			return null;
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

	public byte[] toByteArray(IMagicCard[] gadgets) {
		MagicXmlStreamHandler xmlHanlder = new MagicXmlStreamHandler();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		CardCollectionStoreObject o = new CardCollectionStoreObject();
		o.list = Arrays.asList(gadgets);
		try {
			xmlHanlder.save(o, byteOut);
			try {
				byteOut.close();
			} catch (IOException e) {
				// ok
			}
		} catch (Exception e) {
			MagicUIActivator.log(e);
		}
		byte[] bytes = byteOut.toByteArray();
		return bytes;
	}

	public Object fromClipboard() {
		final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
		Object contents = cb.getContents(this);
		if (contents instanceof IMagicCard[]) {
			IMagicCard[] cards = (IMagicCard[]) contents;
			return DataManager.getInstance().resolve(Arrays.asList(cards));
		}
		return contents;
	}
}
