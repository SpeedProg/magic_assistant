package com.reflexit.magiccards.ui.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.CardCache;

public class DesktopFigure extends XFigure {
	private ArrayList<CardFigure> children;
	private CardFigure active;
	private DesktopCanvas canvas;
	private IFilteredCardStore<IMagicCard> fstore;
	private final Rectangle DEFAULT_SIZE = new Rectangle(0, 0, 1200, 768);
	private ICardField currentGroup;
	private boolean mouseMove;
	private int lastActivePosition;

	public DesktopFigure(DesktopCanvas deskCanvas) {
		super(null);
		this.canvas = deskCanvas;
		children = new ArrayList<CardFigure>();
		image = new Image(Display.getCurrent(), DEFAULT_SIZE.width, DEFAULT_SIZE.height);
		canvas.setImage(image);
		redraw();
	}

	@Override
	public void redraw() {
		super.redraw();
		canvas.redraw();
	}

	public void moveUp(CardFigure card) {
		children.remove(card);
		children.add(card);
	}

	public void moveTo(int i, CardFigure card) {
		children.remove(card);
		children.add(i, card);
	}

	@Override
	public void paint(GC gc, int x, int y, int width, int height, boolean all) {
		// background
		Rectangle rect = new Rectangle(x, y, width, height);
		Rectangle bounds = getBounds();
		gc.setClipping(rect);
		// gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		gc.fillRectangle(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, bounds.height, bounds.width, 0);
		// gc.drawText("Default Image", 10, 10);
		// children
		if (all) {
			for (CardFigure child : children) {
				Rectangle cb = child.getBounds();
				if (cb.intersects(rect)) {
					// System.err.println("Repaining " + child);
					child.paint(gc, x, y, width, height, all);
				}
			}
		}
	}

	@Override
	public boolean mouseDrag(Point p) {
		mouseMove = true;
		if (active != null) {
			Rectangle cb = active.getBounds();
			if (cb.contains(p)) {
				active.mouseDrag(p);
				canvas.redraw(); // XXX
				return true;
			}
			active.mouseStopDrag(p);
			active = null;
		}
		return false;
	}

	@Override
	public boolean mouseStartDrag(Point p) {
		active = null;
		mouseMove = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			CardFigure child = children.get(i);
			if (child.getBounds().contains(p)) {
				active = child;
				break;
			}
		}
		if (active == null)
			return false;
		active.mouseStartDrag(p);
		lastActivePosition = children.indexOf(active);
		moveUp(active);
		redraw();
		return true;
	}

	@Override
	public boolean mouseStopDrag(Point p) {
		if (active == null)
			return false;
		active.mouseStopDrag(p);
		if (!mouseMove) {
			moveTo(lastActivePosition, active);
		}
		redraw();
		return true;
	}

	public void setInput(IFilteredCardStore<IMagicCard> store) {
		// if (this.fstore == store)
		// return;
		this.fstore = store;
		updateChildren();
		if (fstore.getFilter().getGroupField() != currentGroup) {
			currentGroup = fstore.getFilter().getGroupField();
			layout();
		}
		redraw();
	}

	private void layout() {
		if (children == null)
			return;
		CardGroup[] cardGroups = fstore.getCardGroups();
		HashMap<IMagicCard, Integer> map = new HashMap<IMagicCard, Integer>();
		for (int i = 0; i < cardGroups.length; i++) {
			CardGroup cardGroup = cardGroups[i];
			i = addFromGroup(cardGroup, map, i);
		}
		CardStackLayout layout = new CardStackLayout();
		int j = 0;
		for (Iterator<CardFigure> iterator = children.iterator(); iterator.hasNext(); j++) {
			CardFigure next = iterator.next();
			IMagicCard card = next.getCard();
			int gi;
			if (map.size() == 0)
				gi = j % 6;
			else
				gi = map.get(card);
			layout.addCard(gi, next);
		}
		Collection zorder = layout.layout();
		children.clear();
		children.addAll(zorder);
		System.err.println("new size: " + layout.width + "," + layout.height);
		resize(new Rectangle(0, 0, layout.width, layout.height));
	}

	private int addFromGroup(CardGroup cardGroup, HashMap<IMagicCard, Integer> map, int i) {
		for (Iterator iterator = cardGroup.getChildren().iterator(); iterator.hasNext();) {
			Object el = iterator.next();
			if (el instanceof IMagicCard)
				map.put((IMagicCard) el, i);
			else if (el instanceof CardGroup) {
				i = addFromGroup((CardGroup) el, map, ++i);
			}
		}
		return i;
	}

	public void updateChildren() {
		for (Iterator<CardFigure> iterator = children.iterator(); iterator.hasNext();) {
			CardFigure next = iterator.next();
			IMagicCard card = next.getCard();
			if (fstore.contains(card)) {
				continue;
			}
			next.dispose();
			iterator.remove();
		}
		ArrayList<CardFigure> newchildren = new ArrayList<CardFigure>();
		int i = 0;
		for (IMagicCard card : fstore) {
			CardFigure found = findCardFigure(card);
			String path = CardCache.createLocalImageFilePath(card);
			boolean imageCached = new File(path).exists();
			if (found != null && found.isImageNotFound() && imageCached) {
				children.remove(found);
			} else if (found != null) {
				continue;
			}
			// new card
			CardFigure c1;
			if (imageCached) {
				ImageData cimage = new ImageData(path);
				c1 = new CardFigure(this, cimage, card);
			} else {
				c1 = new CardFigure(this, card);
			}
			if (found != null) {
				c1.setLocation(found.getBounds().x, found.getBounds().y);
				found.dispose();
			} else {
				c1.setLocation(100 + 20 * i, 100 + 20 * i++);
			}
			newchildren.add(c1);
		}
		children.addAll(newchildren);
	}

	public CardFigure findCardFigure(IMagicCard card) {
		CardFigure found = null;
		for (Iterator<CardFigure> iterator = children.iterator(); iterator.hasNext();) {
			CardFigure next = iterator.next();
			if (card.equals(next.getCard())) {
				found = next;
				break;
			}
		}
		return found;
	}

	public void resize() {
		Rectangle newsize = DEFAULT_SIZE;
		resize(newsize);
	}

	public void resize(Rectangle newsize) {
		Rectangle client = canvas.getClientArea();
		if (newsize.width < client.width)
			newsize.width = client.width;
		if (newsize.height < client.height)
			newsize.height = client.height;
		image = new Image(Display.getCurrent(), newsize.width, newsize.height);
		canvas.setImage(image);
		super.redraw();
	}
}
