package com.reflexit.magiccards.ui.views.columns;

import java.util.Collections;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class ForTradeCountColumn extends CountColumn {
	public ForTradeCountColumn() {
		super(MagicCardField.FORTRADECOUNT, "For Trade");
	}

	@Override
	protected void setElementValue(Object element, Object value) {
		MagicCardPhysical card = (MagicCardPhysical) element;
		int oldFCount = card.getForTrade();
		boolean wasForTrade = card.isForTrade();
		int newFCount = value == null ? 0 : Integer.parseInt(value.toString());
		if (oldFCount == newFCount && (newFCount == 0 && !wasForTrade || newFCount > 0 && wasForTrade))
			return;
		int oldCount = card.getCount();
		if (newFCount >= oldCount || wasForTrade || oldFCount == newFCount || oldCount == 0) {
			card.tradeSplit(newFCount, newFCount);
		} else if (newFCount == 0) {
			card.tradeSplit(oldCount, newFCount);
		} else {
			int newCount = oldCount - newFCount;
			boolean yes = MessageDialog.openQuestion(getShell(), "?",
					"New format no longer supports combining for trade cards together with not for trade cards in one pile.\n"
							+ "Split card into two piles " + newCount + " not for trace / " + newFCount
							+ " for trade?");
			if (yes) {
				MagicCardPhysical clone = card.tradeSplit(oldCount, newFCount);
				if (clone != null)
					DataManager.getInstance().add(clone);
			}
		}
		// save
		DataManager.getInstance().update(card, Collections.singleton(MagicCardField.COUNT));
	}

	public Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
}
