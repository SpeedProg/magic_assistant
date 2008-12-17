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

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
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
		IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
		String filter = null;
		if (target instanceof ILocatable) {
			filter = ((ILocatable) target).getLocation();
		}
		for (int i = 0; i < toDropArray.length; i++) {
			IMagicCard magicCard = toDropArray[i];
			if (magicCard instanceof MagicCardPhisical) {
				MagicCardPhisical phi = new MagicCardPhisical(magicCard);
				phi.setLocation(null);
				toDropArray[i] = phi;
			}
		}
		try {
			ICardStore<IMagicCard> cardStore = target.getCardStore();
			if (cardStore instanceof ILocatable && filter != null) {
				ILocatable ms = (ILocatable) cardStore;
				String old = ms.getLocation();
				ms.setLocation(filter);
				cardStore.addAll(Arrays.asList(toDropArray));
				ms.setLocation(old);
			} else {
				cardStore.addAll(Arrays.asList(toDropArray));
			}
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
		return MagicCardTransfer.getInstance().isSupportedType(type);
	}
}
