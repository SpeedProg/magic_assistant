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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

/**
 * @author Alena
 *
 */
public class MagicCardDragListener implements DragSourceListener {
	private StructuredViewer viewer;
	AbstractCardsView view;
	IStructuredSelection selection;

	public MagicCardDragListener(StructuredViewer viewer, AbstractCardsView view) {
		this.viewer = viewer;
		this.view = view;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit || event.detail == DND.DROP_NONE)
			return;
		// refresh viewer maybe?
		if (event.detail == DND.DROP_MOVE) {
			this.viewer.refresh();
		}
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragSetData(DragSourceEvent event) {
		this.selection = (IStructuredSelection) this.viewer.getSelection();
		IMagicCard[] cards = (IMagicCard[]) this.selection.toList().toArray(new IMagicCard[this.selection.size()]);
		if (MagicCardTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = cards;
		} else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			byte[] data = MagicCardTransfer.getInstance().toByteArray(cards);
			event.data = new PluginTransferData("com.reflexit.ui.drop", data);
		}
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragStart(DragSourceEvent event) {
		event.doit = !this.viewer.getSelection().isEmpty();
	}
}
