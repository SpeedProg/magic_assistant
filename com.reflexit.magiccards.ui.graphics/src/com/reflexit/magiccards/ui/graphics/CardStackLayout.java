package com.reflexit.magiccards.ui.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.swt.graphics.Rectangle;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.SortOrder;

public class CardStackLayout {
	private List<Collection<XFigure>> groups = new ArrayList<Collection<XFigure>>();
	private SortOrder order = new SortOrder();
	private Comparator<XFigure> comparator = new Comparator<XFigure>() {
		public int compare(XFigure arg0, XFigure arg1) {
			if (!(arg0 instanceof CardFigure))
				return 0;
			if (!(arg1 instanceof CardFigure))
				return 0;
			return order.compare(((CardFigure) arg0).getCard(), ((CardFigure) arg1).getCard());
		}
	};
	public int marginHeight = 20;
	public int marginWidth = 40;
	public int horisontalSpacing = 10;
	public int verticalSpacing = 30;
	public int width;
	public int height;

	public CardStackLayout(SortOrder order) {
		super();
		this.order = order;
	}

	public CardStackLayout() {
		super();
		order.setSortField(MagicCardField.NAME, false);
		order.setSortField(MagicCardField.CTYPE, false);
		order.setSortField(MagicCardField.CMC, false);
	}

	public void addCard(int i, XFigure fig) {
		Collection<XFigure> stack = getStack(i);
		stack.add(fig);
	}

	public void clear() {
		groups.clear();
	}

	private Collection<XFigure> getStack(int i) {
		Collection<XFigure> stack = null;
		if (i < groups.size())
			stack = groups.get(i);
		if (stack == null) {
			stack = new TreeSet<XFigure>(comparator);
			while (i >= groups.size()) {
				groups.add(null);
			}
			groups.set(i, stack);
		}
		return stack;
	}

	public Collection<XFigure> layout() {
		Collection<XFigure> zorder = new ArrayList<XFigure>();
		int x = marginWidth;
		int w = 0;
		int h = 0;
		for (Iterator<Collection<XFigure>> iterator = groups.iterator(); iterator.hasNext();) {
			Collection<XFigure> stack = iterator.next();
			int y = marginHeight;
			if (stack != null) {
				for (Iterator<XFigure> iterator2 = stack.iterator(); iterator2.hasNext();) {
					XFigure xFigure = iterator2.next();
					xFigure.setLocation(x, y);
					zorder.add(xFigure);
					Rectangle bounds = xFigure.getBounds();
					int xw = bounds.width;
					if (w < xw)
						w = xw;
					h = bounds.height;
					y += verticalSpacing;
				}
			}
			x += w;
			x += horisontalSpacing;
			if (height < y + h)
				height = y + h;
		}
		width = x;
		return zorder;
	}

	public Collection<XFigure> getTop() {
		Collection<XFigure> top = new ArrayList<XFigure>();
		for (Iterator<Collection<XFigure>> iterator = groups.iterator(); iterator.hasNext();) {
			Collection<XFigure> stack = iterator.next();
			if (stack != null) {
				XFigure last = null;
				for (Iterator<XFigure> iterator2 = stack.iterator(); iterator2.hasNext();) {
					XFigure xFigure = iterator2.next();
					last = xFigure;
				}
				top.add(last);
			}
		}
		return top;
	}
}
