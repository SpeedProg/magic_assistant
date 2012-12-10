package com.reflexit.magiccards.ui.views.columns;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.graphics.Image;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.widgets.ComboStringEditingSupport;

public class SetColumn extends GenColumn {
	private boolean showImage = false;

	public boolean isShowImage() {
		return showImage;
	}

	public void setShowImage(boolean showImage) {
		this.showImage = showImage;
	}

	public SetColumn() {
		super(MagicCardField.SET, "Set");
	}

	public SetColumn(boolean showImage) {
		this();
		this.showImage = showImage;
	}

	@Override
	public int getColumnWidth() {
		return 150;
	}

	@Override
	public Image getImage(Object element) {
		if (isShowImage()) {
			if (element instanceof IMagicCard) {
				IMagicCard card = (IMagicCard) element;
				return ImageCreator.getInstance().getSetImage(card);
			}
		}
		return super.getImage(element);
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new ComboStringEditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof MagicCardPhysical)
					return true;
				else
					return false;
			}

			@Override
			public String[] getItems(Object element) {
				IMagicCardPhysical card = (IMagicCardPhysical) element;
				List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
				if (cards.size() <= 1)
					return null;
				String sets[] = new String[cards.size()];
				int i = 0;
				for (Iterator iterator = cards.iterator(); iterator.hasNext(); i++) {
					IMagicCard iMagicCard = (IMagicCard) iterator.next();
					sets[i] = iMagicCard.getSet();
				}
				return sets;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhysical) {
					IMagicCardPhysical card = (IMagicCardPhysical) element;
					return card.getSet();
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhysical) {
					MagicCardPhysical card = (MagicCardPhysical) element;
					// set
					IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
					ICardStore<IMagicCard> cardStore = target.getCardStore();
					List<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
					String set = (String) value;
					for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
						IMagicCard iMagicCard = (IMagicCard) iterator.next();
						if (iMagicCard.getSet().equals(set)) {
							card.setMagicCard((MagicCard) iMagicCard);
							break;
						}
					}
					DataManager.reconcile();
					// update
					cardStore.update(card);
					// viewer.update(element, null);
				}
			}
		};
	}
}
