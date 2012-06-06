package com.reflexit.magiccards.ui.views.collector;

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
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.views.columns.GenColumn;

public class ProgressColumn extends GenColumn implements Listener {
	final Color barColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GREEN);

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

	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			String text = (String) getProperty(cardGroup, getColumnName());
			if (text != null)
				return text;
			if (cardGroup.size() > 0) {
				int size = getSetSize(cardGroup);
				int count = getOwnSize(cardGroup);
				String scount;
				int per = 0;
				if (size == 0) {
					scount = count + " / ?";
				} else {
					scount = count + " / " + size;
					per = count * 100 / size;
					scount += " (" + per + "%)";
				}
				setProperty(cardGroup, getColumnName() + ".per", per);
				setProperty(cardGroup, getColumnName(), scount);
				return scount;
			}
			return "";
		} else if (element instanceof MagicCardPhysical) {
			if (((MagicCardPhysical) element).isOwn())
				return ((MagicCardPhysical) element).getCount() + "";
			else
				return "0";
		}
		return super.getText(element);
	}

	protected ICardStore<IMagicCard> getSetStore(CardGroup cardGroup) {
		Location loc = Location.fromCard(((IMagicCard) cardGroup.getChildAtIndex(0)).getBase());
		ICardStore<IMagicCard> store = ((AbstractMultiStore<IMagicCard>) DataManager.getCardHandler().getMagicDBStore()).getStore(loc);
		return store;
	}

	public int getOwnSize(CardGroup cardGroup) {
		int count = 0;
		Collection children = cardGroup.getChildren();
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				if (((MagicCardPhysical) object).isOwn())
					count++;
			}
		}
		return count;
	}

	public int getSetSize(CardGroup cardGroup) {
		ICardStore<IMagicCard> store = getSetStore(cardGroup);
		int size = store == null ? 0 : store.size();
		return size;
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
				}
			}
			break;
		}
		}
	}
}
