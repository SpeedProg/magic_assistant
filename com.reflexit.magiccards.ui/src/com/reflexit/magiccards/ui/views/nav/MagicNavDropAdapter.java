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

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;

/**
 * @author Alena
 * 
 */
public class MagicNavDropAdapter extends ViewerDropAdapter implements DropTargetListener {
	private static final DataManager DM = DataManager.getInstance();
	private DropTargetEvent curEvent;
	private Transfer transfer;
	private Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance(),
			MagicDeckTransfer.getInstance() };

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
		Object dropTarget = getCurrentTarget();
		if (MagicCardTransfer.getInstance() == transfer) {
			if (dropLocation != LOCATION_ON)
				return false;
			if (!(dropTarget instanceof CardCollection)) {
				return false;
			}
			IMagicCard[] toDropArray = (IMagicCard[]) data;
			if (toDropArray.length == 0)
				return false;
			try {
				Location targetLocation = ((CardElement) dropTarget).getLocation();
				Collection<IMagicCard> list = DM.resolve(Arrays.asList(toDropArray));
				ICardStore<IMagicCard> sto = DM.getCardStore(targetLocation);
				if (sto == null)
					throw new MagicException("Invalid drop target: Cannot open collection " + targetLocation);
				if (curEvent.detail == DND.DROP_MOVE)
					return DM.moveCards(list, sto);
				else
					return DM.copyCards(list, sto);
			} catch (MagicException e) {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error",
						"Cannot perform this operation");
				return false;
			} catch (Exception e) {
				MagicUIActivator.log(e);
				return false;
			}
		} else {
			CardElement[] toDropArray = (CardElement[]) data;
			if (dropLocation == LOCATION_NONE)
				return false;
			CardOrganizer dropParent;
			if (dropLocation == LOCATION_ON) {
				dropParent = (CardOrganizer) dropTarget;
			} else {
				dropParent = ((CardElement) dropTarget).getParent();
			}
			if (toDropArray.length == 0)
				return false;
			try {
				DM.getModelRoot().move(toDropArray, dropParent);
			} catch (MagicException e) {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error",
						"Cannot perform this operation: "
								+ e.getMessage());
				return false;
			} catch (Exception e) {
				MagicUIActivator.log(e);
				return false;
			}
		}
		return false;
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		curEvent = event;
		super.dropAccept(event);
	}

	@Override
	public boolean validateDrop(Object target, int op, TransferData type) {
		for (Transfer t : transfers) {
			if (t.isSupportedType(type)) {
				transfer = t;
				break;
			}
		}
		if (transfer == null)
			return false;
		if (MagicCardTransfer.getInstance() == transfer) {
			// card
			if (!(target instanceof CardCollection)) {
				return false;
			}
			if (getCurrentLocation() != LOCATION_ON)
				return false;
			return true;
		} else {
			// deck
			if (op == DND.DROP_COPY)
				return false;
			if (getCurrentLocation() == LOCATION_NONE)
				return false;
			if (getCurrentLocation() == LOCATION_ON && !(target instanceof CardOrganizer))
				return false;
			if (target instanceof MagicDbContainter)
				return false;
			return true;
		}
	}
}
