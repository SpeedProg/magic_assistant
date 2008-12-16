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

import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * @author Alena
 *
 */
public class MagicNavDropAdapter extends ViewerDropAdapter implements DropTargetListener {
	/**
	 * @param viewer
	 * @param view 
	 */
	public MagicNavDropAdapter(Viewer viewer) {
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data) {
		int dropLocation = getCurrentLocation();
		if (dropLocation != LOCATION_ON)
			return false;
		Object idata = getCurrentTarget();
		if (!(idata instanceof CardCollection) && !(idata instanceof Deck)) {
			return false;
		}
		IMagicCard[] toDropArray = (IMagicCard[]) data;
		if (toDropArray.length == 0)
			return false;
		ICardStore<IMagicCard> cardStore;
		if (idata instanceof Deck) {
			cardStore = ((Deck) idata).getStore();
			cardStore.addAll(Arrays.asList(toDropArray));
			return true;
		}
		IFilteredCardStore target = DataManager.getCardHandler().getMagicLibraryHandler();
		String location = ((CardCollection) idata).getLocation();
		try {
			//TableViewer viewer = (TableViewer) getViewer();
			cardStore = target.getCardStore();
			if (cardStore instanceof ILocatable) {
				ILocatable ms = (ILocatable) cardStore;
				String old = ms.getLocation();
				ms.setLocation(location);
				cardStore.addAll(Arrays.asList(toDropArray));
				ms.setLocation(old);
			} else {
				cardStore.addAll(Arrays.asList(toDropArray));
			}
			//viewer.reveal(toDropArray[0]);
			return true;
		} catch (MagicException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error",
			        "Cannot perform this operation");
			return false;
		} catch (Exception e) {
			MagicUIActivator.log(e);
			return false;
		}
	}

	@Override
	public boolean validateDrop(Object target, int op, TransferData type) {
		if (!(target instanceof CardCollection) && !(target instanceof Deck)) {
			return false;
		}
		if (getCurrentLocation() != LOCATION_ON)
			return false;
		boolean supp = MagicCardTransfer.getInstance().isSupportedType(type);
		return supp;
	}
}
