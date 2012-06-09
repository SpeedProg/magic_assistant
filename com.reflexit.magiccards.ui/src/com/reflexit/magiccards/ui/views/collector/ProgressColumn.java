package com.reflexit.magiccards.ui.views.collector;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
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

	public ProgressColumn(ICardField field, String columnName) {
		super(field, columnName);
	}

	public ProgressColumn() {
		super(null, "Progress");
	}

	public void setProperty(CardGroup element, String key, Object value) {
		HashMap<String, Object> props = (HashMap<String, Object>) element.getData();
		if (props == null) {
			props = new HashMap<String, Object>();
			element.setData(props);
		}
		props.put(key, value);
	}

	public Object getProperty(CardGroup element, String key) {
		HashMap<String, Object> props = (HashMap<String, Object>) element.getData();
		if (props == null) {
			return null;
		}
		return props.get(key);
	}

	MessageFormat performat = new MessageFormat("{0,number,000} / {1,number,000} ({2,choice,0# bummer|0< {2,number,000}%) }");

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			if (cardGroup.size() > 0) {
				int size = getSetSize(cardGroup);
				int count = getOwnSize(cardGroup);
				int per = 0;
				if (size > 0) {
					per = count * 100 / size;
				}
				setProperty(cardGroup, getColumnName() + ".per", per);
				if (per > 0)
					return String.format("%3d / %3d (%2d%%)", count, size, per);
				else
					return String.format("%3d / ?", count);
			}
			return "";
		} else if (element instanceof MagicCardPhysical) {
			return getSizeCountText(element);
		} else if (element instanceof MagicCard) {
			return "0";
		}
		return super.getText(element);
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
				if (row instanceof CardGroup) {
					Integer per = (Integer) getProperty((CardGroup) row, getColumnName() + ".per");
					if (per == null)
						per = Integer.valueOf(0);
					Rectangle bounds;
					if (item instanceof TableItem)
						bounds = ((TableItem) item).getBounds(event.index);
					else if (item instanceof TreeItem)
						bounds = ((TreeItem) item).getBounds(event.index);
					else
						return;
					event.gc.setBackground(barColor);
					event.gc.setAlpha(64);
					int width = bounds.width * per / 100;
					event.gc.fillRectangle(bounds.x + bounds.width - width, bounds.y, width, bounds.height);
				} else if (row instanceof MagicCard || row instanceof MagicCardPhysical
						&& (((MagicCardPhysical) row).getCount() == 0 || ((MagicCardPhysical) row).isOwn() == false)) {
					event.gc.setBackground(missColor);
					event.gc.setAlpha(64);
					event.gc.fillRectangle(event.x, event.y, event.width, event.height);
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
			if (icount != base)
				return base + " (" + String.valueOf(icount) + ")";
			else
				return base + "";
		}
		return "0";
	}
}
