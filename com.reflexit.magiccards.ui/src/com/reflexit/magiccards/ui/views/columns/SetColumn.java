package com.reflexit.magiccards.ui.views.columns;

import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.widgets.ComboStringEditingSupport;

public class SetColumn extends GenColumn {
	public class SetEditingSupport extends ComboStringEditingSupport {
		public SetEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof MagicCardPhysical)
				return true;
			else
				return false;
		}

		@Override
		public int getStyle() {
			return SWT.NONE;
		}

		@Override
		public String[] getItems(Object element) {
			IMagicCardPhysical card = (IMagicCardPhysical) element;
			Collection<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
			int len = cards.size();
			if (card.getCardId() == 0) {
				len++;
			}
			String sets[] = new String[len];
			int i = 0;
			for (Iterator iterator = cards.iterator(); iterator.hasNext(); i++) {
				IMagicCard mCard = (IMagicCard) iterator.next();
				sets[i] = mCard.getSet();
			}
			if (card.getCardId() == 0) {
				sets[i] = card.getSet();
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
				Collection<IMagicCard> cards = DataManager.getMagicDBStore().getCandidates(card.getName());
				String set = (String) value;
				String oldSet = card.getSet();
				if (oldSet != null && oldSet.equals(set))
					return;
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard base = (IMagicCard) iterator.next();
					if (base.getSet().equals(set)) {
						card.setMagicCard((MagicCard) base);
						updateOnEdit(getViewer(), card);
						return;
					}
				}
				MagicUIActivator.log("Cannot set new set for " + card + " of value " + set);
			}
		}
	}

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
	public Color getForeground(Object element) {
		IMagicCard card = (IMagicCard) element;
		if (card.getCardId() == 0 && element instanceof MagicCardPhysical)
			return Display.getDefault().getSystemColor(SWT.COLOR_RED);
		return super.getForeground(element);
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
		return new SetEditingSupport(viewer);
	}
}
