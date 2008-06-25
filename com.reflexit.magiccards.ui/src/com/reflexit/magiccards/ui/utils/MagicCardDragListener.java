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
package com.reflexit.magiccards.ui.utils;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * @author Alena
 *
 */
public class MagicCardDragListener implements DragSourceListener {
	private StructuredViewer viewer;

	public MagicCardDragListener(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit)
			return;
		//if the gadget was moved, remove it from the source viewer
		if (event.detail == DND.DROP_MOVE) {
			IStructuredSelection selection = (IStructuredSelection) this.viewer.getSelection();
			//			for (Iterator it = selection.iterator(); it.hasNext();) {
			//				// TODO remove
			//			}
			this.viewer.refresh();
		}
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) this.viewer.getSelection();
		IMagicCard[] gadgets = (IMagicCard[]) selection.toList().toArray(new IMagicCard[selection.size()]);
		if (MagicCardTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = gadgets;
		} else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			byte[] data = MagicCardTransfer.getInstance().toByteArray(gadgets);
			event.data = new PluginTransferData("org.eclipse.ui.examples.gdt.gadgetDrop", data);
		}
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragStart(DragSourceEvent event) {
		event.doit = !this.viewer.getSelection().isEmpty();
	}
}
