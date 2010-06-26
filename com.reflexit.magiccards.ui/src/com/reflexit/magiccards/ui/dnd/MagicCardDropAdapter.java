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
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

/**
 * @author Alena
 *
 */
public class MagicCardDropAdapter extends ViewerDropAdapter implements DropTargetListener {
	private AbstractCardsView view;
	private DropTargetEvent curEvent;

	/**
	 * @param viewer
	 * @param view 
	 */
	public MagicCardDropAdapter(Viewer viewer, AbstractCardsView view) {
		super(viewer);
		this.view = view;
	}

	@Override
	public boolean performDrop(Object data) {
		IMagicCard[] toDropArray = (IMagicCard[]) data;
		if (toDropArray.length == 0)
			return false;
		try {
			Location targetLocation = determineLocation();
			if (targetLocation == null)
				throw new MagicException("Invalid drop target");
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

	private Location determineLocation() {
		IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
		Location targetLocation = ((ILocatable) target).getLocation();
		return targetLocation;
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		curEvent = event;
		super.dropAccept(event);
	}

	@Override
	public boolean validateDrop(Object target, int op, TransferData type) {
		return MagicCardTransfer.getInstance().isSupportedType(type);
	}
}
