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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;

import java.util.Arrays;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * @author Alena
 *
 */
public class MagicNavDropAdapter extends ViewerDropAdapter implements DropTargetListener {
	private DropTargetEvent curEvent;

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
		if (!(idata instanceof CardCollection)) {
			return false;
		}
		IMagicCard[] toDropArray = (IMagicCard[]) data;
		if (toDropArray.length == 0)
			return false;
		try {
			Location targetLocation = ((CardElement) idata).getLocation();
			if (curEvent.detail == DND.DROP_MOVE)
				return DataManager.getCardHandler().moveCards(Arrays.asList(toDropArray), null, targetLocation);
			else
				return DataManager.getCardHandler().copyCards(Arrays.asList(toDropArray), targetLocation);
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
	public void dropAccept(DropTargetEvent event) {
		curEvent = event;
		super.dropAccept(event);
	}

	@Override
	public boolean validateDrop(Object target, int op, TransferData type) {
		if (!(target instanceof CardCollection)) {
			return false;
		}
		if (getCurrentLocation() != LOCATION_ON)
			return false;
		boolean supp = MagicCardTransfer.getInstance().isSupportedType(type);
		return supp;
	}
}
