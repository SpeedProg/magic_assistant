package com.reflexit.magiccards.ui.views.collector;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.views.columns.GenColumn;

public class ProgressColumn extends GenColumn implements Listener {
	final Color barColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GREEN);
	final Color missColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
	final Color partColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_YELLOW);

	public ProgressColumn(ICardField field, String columnName) {
		super(field, columnName);
	}

	public ProgressColumn() {
		super(null, "Progress");
	}

	MessageFormat performat = new MessageFormat("{0,number,000} / {1,number,000} ({2,choice,0# bummer|0< {2,number,000}%) }");

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			if (cardGroup.size() > 0) {
				int size = getSetSize(cardGroup);
				int count = getOwnSize(cardGroup);
				float per = 0;
				if (size > 0) {
					per = count * 100 / (float) size;
				}
				cardGroup.setProperty(getColumnName() + ".per", per);
				if (per < 5 && per > 0)
					return String.format("%3d / %3d (%2.1f%%)", count, size, per);
				else if (size > 0)
					return String.format("%3d / %3d (%2d%%)", count, size, (int) per);
				else
					return String.format("%3d / ?", count);
			}
			return "";
		} else if (element instanceof MagicCardPhysical) {
			return getSizeCountText(element);
		} else if (element instanceof MagicCard) {
			return "no";
		}
		return super.getText(element);
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof MagicCard) {
			return "This card is not in any of your card collections";
		}
		if (element instanceof MagicCardPhysical) {
			MagicCardPhysical card = (MagicCardPhysical) element;
			if (card.isOwn()) {
				return "You own " + card.getCount() + "of these cards";
			} else {
				return "This means you have " + card.getCount() + " virtual cards (you don't own them)";
			}
		}
		if (element instanceof CardGroup) {
			return "X/Y (Z%) - Means you have X unique cards you own out of Y possible in this class, which represents Z%";
		}
		return null;
	}

	protected ICardStore<IMagicCard> getSetStore(CardGroup cardGroup) {
		if (cardGroup.size() == 0)
			return null;
		Location loc = Location.createLocationFromSet(cardGroup.getFirstCard().getSet());
		ICardStore<IMagicCard> store = ((AbstractMultiStore<IMagicCard>) DataManager.getCardHandler().getMagicDBStore()).getStore(loc);
		return store;
	}

	public int getOwnSize(CardGroup cardGroup) {
		int count = 0;
		Collection children = cardGroup.getChildren();
		ICardField field = cardGroup.getFieldIndex();
		String name = cardGroup.getName();
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				if (((MagicCardPhysical) object).isOwn()) {
					Object fieldValue = ((MagicCardPhysical) object).getObjectByField(field);
					if (name.equals(String.valueOf(fieldValue)))
						count++;
				}
			} else if (object instanceof CardGroup) {
				count += getOwnSize((CardGroup) object);
			}
		}
		return count;
	}

	public int getSetSize(CardGroup cardGroup) {
		ICardStore<IMagicCard> store = getSetStore(cardGroup);
		if (store == null)
			return 0;
		int count = 0;
		ICardField field = cardGroup.getFieldIndex();
		String name = cardGroup.getName();
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof IMagicCard) {
				Object fieldValue = ((IMagicCard) object).getObjectByField(field);
				if (name.equals(String.valueOf(fieldValue)))
					count++;
			}
		}
		return count;
	}

	public int getFilteredSize(Iterable store, MagicCardFilter filter) {
		if (store == null)
			return 0;
		int sum = 0;
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			Object c = iterator.next();
			if (filter != null && filter.isFiltered(c))
				continue;
			sum++;
		}
		return sum;
	}

	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.PaintItem: {
			if (event.index == this.columnIndex) {
				Item item = (Item) event.item;
				Object row = item.getData();
				Rectangle bounds;
				if (item instanceof TableItem)
					bounds = ((TableItem) item).getBounds(event.index);
				else if (item instanceof TreeItem)
					bounds = ((TreeItem) item).getBounds(event.index);
				else
					return;
				float per = 100;
				if (row instanceof CardGroup) {
					Float per1 = (Float) ((CardGroup) row).getProperty(getColumnName() + ".per");
					if (per1 == null)
						per = Float.valueOf(0);
					else
						per = per1;
				} else if (row instanceof MagicCard || row instanceof MagicCardPhysical
						&& (((MagicCardPhysical) row).getCount() == 0 || ((MagicCardPhysical) row).isOwn() == false)) {
					per = 0;
				}
				if (per > 0) {
					int width = (int) (bounds.width * per / 100);
					event.gc.setBackground(barColor);
					event.gc.setForeground(partColor);
					event.gc.setAlpha(64);
					event.gc.fillGradientRectangle(bounds.x, bounds.y, bounds.width - width, bounds.height, false);
					event.gc.fillRectangle(bounds.x + bounds.width - width, bounds.y, width, bounds.height);
				} else {
					event.gc.setBackground(missColor);
					event.gc.setForeground(partColor);
					event.gc.setAlpha(64);
					event.gc.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}
			break;
		}
		}
	}

	public String getSizeCountText(Object element) {
		if (element instanceof MagicCardPhysical) {
			int base = 0;
			if (((MagicCardPhysical) element).isOwn()) {
				base = 1;
			}
			int icount = ((MagicCardPhysical) element).getCount();
			String prefix = base == 0 ? "no" : "yes";
			if (icount != base)
				return prefix + " (" + String.valueOf(icount) + ")";
			else
				return prefix + "";
		}
		return "no";
	}
}
