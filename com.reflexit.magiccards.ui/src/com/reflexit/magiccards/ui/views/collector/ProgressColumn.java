package com.reflexit.magiccards.ui.views.collector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.ui.views.columns.AbstractImageColumn;

public class ProgressColumn extends AbstractImageColumn {
	protected MagicCardField getPercentKey() {
		return MagicCardField.PERCENT_COMPLETE;
	}

	final Color barColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GREEN);
	final Color missColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
	final Color partColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_YELLOW);

	public ProgressColumn(ICardField field, String columnName) {
		super(field, columnName);
	}

	public ProgressColumn() {
		super(MagicCardField.PERCENT_COMPLETE, "Progress");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int size = getTotal(cardGroup);
			int count = getProgressSize(cardGroup);
			float per = cardGroup.getFloat(getPercentKey());
			if (per < 5 && per > 0)
				return String.format("%3d / %3d (%2.1f%%)", count, size, per);
			else if (size > 0)
				return String.format("%3d / %3d (%2d%%)", count, size, (int) per);
			else
				return String.format("%3d / ?", count);
		} else if (element instanceof IMagicCard) {
			return getSizeCountText(element);
		}
		return super.getText(element);
	}

	public int getTotal(ICard element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int size = getSetSize(cardGroup);
			return size;
		}
		return 1;
	}

	public int getProgressSize(ICard element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int count = cardGroup.getOwnUnique();
			return count;
		}
		return 0;
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof MagicCard) {
			if (((MagicCard) element).getPhysicalCards().size() == 0)
				return "This card is not in any of your card collections";
			int ownCount = ((MagicCard) element).getOwnCount();
			if (ownCount == 0)
				return "This means you have some virtual cards (you don't own them)";
			return "You own " + ownCount + " of these cards";
		}
		if (element instanceof MagicCardPhysical) {
			MagicCardPhysical card = (MagicCardPhysical) element;
			if (card.isOwn()) {
				return "You own " + card.getCount() + " of these cards";
			} else {
				return "This means you have " + card.getCount() + " virtual cards (you don't own them)";
			}
		}
		if (element instanceof ICardGroup) {
			return "X/Y (Z%) - Means you have X unique cards you own out of Y possible in this class, which represents Z%";
		}
		return null;
	}

	public int getSetSize(CardGroup cardGroup) {
		return cardGroup.getUniqueCount();
	}

	@Override
	protected void handleEraseEvent(Event event) {
		// use standard text paint
		// super.handleEraseEvent(event);
	}

	@Override
	public void handlePaintEvent(Event event) {
		Item item = (Item) event.item;
		Object row = item.getData();
		int x = event.x;
		int y = event.y;
		Rectangle bounds = getBounds(event);
		x = bounds.x;
		int w = bounds.width;
		int h = bounds.height;
		float per = 100;
		if (row instanceof ICardGroup) {
			Float per1 = (Float) ((CardGroup) row).get(getPercentKey());
			if (per1 == null)
				per = Float.valueOf(0);
			else
				per = per1;
		} else if (row instanceof MagicCard && ((MagicCard) row).getOwnCount() == 0) {
			per = 0;
		} else if (row instanceof MagicCardPhysical
				&& (((MagicCardPhysical) row).getCount() == 0 || ((IMagicCardPhysical) row).isOwn() == false)) {
			per = 0;
		}
		GC gc = event.gc;
		if (per > 0) {
			int width = (int) (w * (per > 60 ? 60 : per) / 100);
			gc.setBackground(barColor);
			gc.setForeground(partColor);
			gc.setAlpha(64);
			gc.fillGradientRectangle(x, y, w - width, h, false);
			gc.fillRectangle(x + w - width, y, width, h);
		} else {
			gc.setBackground(missColor);
			gc.setForeground(partColor);
			gc.setAlpha(64);
			gc.fillRectangle(x, y, w, h);
		}
	}

	public String getSizeCountText(Object element) {
		int base = 0;
		int virtual = 0;
		int count = 0;
		if (element instanceof MagicCard) {
			MagicCard mc = (MagicCard) element;
			base = mc.getOwnUnique();
			virtual = mc.getPhysicalCards().size();
			count = mc.getOwnCount();
		}
		if (element instanceof MagicCardPhysical) {
			count = ((MagicCardPhysical) element).getCount();
			if (((IMagicCardPhysical) element).isOwn()) {
				base = 1;
			} else {
				virtual = 1;
				count = 0;
			}
		}
		if (base == 0 && virtual == 0)
			return "no";
		String prefix = base == 0 ? "no" : "yes";
		if (count > 1)
			return prefix + " (" + String.valueOf(count) + ")";
		else
			return prefix + "";
	}
}
