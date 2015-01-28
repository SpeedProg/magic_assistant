package com.reflexit.magiccards.ui.views.nav;

import java.util.Iterator;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

public class MagicNavDragListener implements DragSourceListener {
	private ColumnViewer viewer;
	private IStructuredSelection selection;

	public MagicNavDragListener(ColumnViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit || event.detail == DND.DROP_NONE)
			return;
		this.viewer.refresh();
	}

	/**
	 * Method declared on DragSourceListener
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		event.doit = isEnabled();
	}

	public boolean isEnabled() {
		IStructuredSelection sel = (IStructuredSelection) this.viewer.getSelection();
		if (sel.isEmpty())
			return false;
		ModelRoot root = DataManager.getInstance().getModelRoot();
		for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			if (el.getParent() == root)
				return false;
			if (el == root.getDefaultLib() || el == root.getDeckContainer()
					|| el == root.getCollectionsContainer())
				return false;
		}
		return true;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		this.selection = (IStructuredSelection) this.viewer.getSelection();
		CardElement[] decks = (CardElement[]) this.selection.toList().toArray(
				new CardElement[this.selection.size()]);
		if (MagicDeckTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = decks;
		} else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			byte[] data = MagicDeckTransfer.getInstance().toByteArray(decks);
			event.data = new PluginTransferData("com.reflexit.ui.dropdeck", data);
		}
	}
}
