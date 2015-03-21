package com.reflexit.magiccards.ui.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class DesktopFigure extends XFigure {
	private ArrayList<CardFigure> children;
	private CardFigure selected;
	private DesktopCanvas canvas;
	private IFilteredCardStore<IMagicCard> fstore;
	private static final Rectangle DEFAULT_SIZE = new Rectangle(0, 0, 1200, 768);
	private ICardField currentGroup;
	private boolean mouseMove;
	private int lastSelectedZOrder;
	private Image backgroungImage;
	private boolean floatingLayer;
	private boolean init;

	public DesktopFigure(DesktopCanvas deskCanvas) {
		super(null, DEFAULT_SIZE.width, DEFAULT_SIZE.height);
		this.canvas = deskCanvas;
		children = new ArrayList<CardFigure>();
		canvas.setImage(getImage());
		redraw();
	}

	@Override
	public synchronized void redraw() {
		super.redraw();
		canvas.redraw();
	}

	@Override
	public synchronized void redraw(int x, int y, int width, int height) {
		super.redraw(x, y, width, height);
		canvas.redraw();
	}

	public void moveUp(CardFigure card) {
		remove(card);
		add(card);
	}

	public void add(CardFigure card) {
		children.add(card);
	}

	public void remove(CardFigure card) {
		children.remove(card);
	}

	public void moveTo(int i, CardFigure card) {
		remove(card);
		children.add(i, card);
	}

	@Override
	public void paint(GC gc, int x, int y, int width, int height) {
		Rectangle rect = new Rectangle(x, y, width, height);
		gc.setClipping(rect);
		if (!floatingLayer) {
			// System.err.println("paint regular");
			paintDesktopBackground(gc, x, y, width, height);
			// children
			for (CardFigure child : children) {
				Rectangle cb = child.getBounds();
				if (cb.intersects(rect)) {
					// System.err.println("Repaining " + child);
					child.paint(gc, x, y, width, height);
				}
			}
		} else {
			// System.err.println("paint floating");
			paintBg(gc, x, y, width, height);
			paintActive(gc, x, y, width, height);
		}
	}

	protected void paintDesktopBackground(GC gc, int x, int y, int width, int height) {
		// background
		Rectangle bounds = getBounds();
		// gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		gc.fillRectangle(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, bounds.height, bounds.width, 0);
		// gc.drawText("Default Image", 10, 10);
		return;
	}

	public void paintActive(GC gc, int x, int y, int width, int height) {
		CardFigure child = selected;
		if (child != null) {
			Rectangle cb = child.getBounds();
			Rectangle rect = new Rectangle(x, y, width, height);
			if (cb.intersects(rect)) {
				// System.err.println("Repaining " + child);
				child.paint(gc, x, y, width, height);
			}
		}
	}

	public void paintBg(GC gc, int x, int y, int width, int height) {
		// background
		if (backgroungImage != null && floatingLayer) {
			if (!backgroungImage.getBounds().equals(getBounds())) {
				backgroungImage.dispose();
				makeFloatingLayer();
			}
			gc.drawImage(backgroungImage, x, y, width, height, x, y, width, height);
		}
	}

	@Override
	public boolean mouseDrag(Point p) {
		mouseMove = true;
		if (selected != null && floatingLayer) {
			Rectangle cb = selected.getBounds();
			if (cb.contains(p)) {
				selected.mouseDrag(p);
				Rectangle area = cb.union(selected.getBounds()).intersection(getBounds());
				redraw(area.x, area.y, area.width, area.height);
				return true;
			}
			selected.mouseStopDrag(p);
			floatingLayer = false;
		}
		return false;
	}

	@Override
	public synchronized boolean mouseStartDrag(Point p) {
		if (selected != null) {
			selected.setSelected(false);
		}
		selected = null;
		mouseMove = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			CardFigure child = children.get(i);
			if (child.getBounds().contains(p)) {
				selected = child;
				break;
			}
		}
		fireSelectionChanged(getSelection());
		if (selected == null)
			return false;
		selected.mouseStartDrag(p);
		selected.setSelected(true);
		lastSelectedZOrder = children.indexOf(selected);
		remove(selected);
		makeFloatingLayer();
		redraw();
		return true;
	}

	protected void makeFloatingLayer() {
		floatingLayer = false;
		super.redraw();
		floatingLayer = true;
		backgroungImage = new Image(canvas.getDisplay(), getImage(), SWT.IMAGE_COPY);
	}

	@Override
	public synchronized boolean mouseStopDrag(Point p) {
		if (selected == null)
			return false;
		selected.mouseStopDrag(p);
		if (!mouseMove)
			moveTo(lastSelectedZOrder, selected);
		else
			add(selected);
		floatingLayer = false;
		if (backgroungImage != null)
			backgroungImage.dispose();
		backgroungImage = null;
		addNewFigureIfNotFound(selected.getCard()); // refresh the image
		redraw();
		return true;
	}

	public IFilteredCardStore<IMagicCard> getInput() {
		return fstore;
	}

	public synchronized void setInput(IFilteredCardStore<IMagicCard> store) {
		// if (this.fstore == store)
		// return;
		this.fstore = store;
		updateChildren();
		if (fstore.getFilter().getGroupField() != currentGroup || init == false) {
			currentGroup = fstore.getFilter().getGroupField();
			layout();
			init = true;
		}
		redraw();
	}

	private void layout() {
		if (children == null)
			return;
		HashMap<IMagicCard, Integer> map = new HashMap<IMagicCard, Integer>();
		ICardGroup root = fstore.getCardGroupRoot();
		addFromGroup(root, map, -1);
		CardStackLayout layout = new CardStackLayout();
		int j = 0;
		for (Iterator<CardFigure> iterator = children.iterator(); iterator.hasNext(); j++) {
			CardFigure next = iterator.next();
			IMagicCard card = next.getCard();
			int gi;
			if (map.size() == 0)
				gi = j % 4;
			else {
				if (map.get(card) != null)
					gi = map.get(card);
				else
					gi = 0;
			}
			layout.addCard(gi, next);
		}
		Collection zorder = layout.layout();
		children.clear();
		children.addAll(zorder);
		// System.err.println("new size: " + layout.width + "," + layout.height);
		resize(new Rectangle(0, 0, layout.width, layout.height));
		Collection<XFigure> top = layout.getTop();
		for (Iterator iterator = top.iterator(); iterator.hasNext();) {
			CardFigure cardf = (CardFigure) iterator.next();
			// System.err.println("Top:" + cardf);
			cardf.loadImage(true);
		}
	}

	private int addFromGroup(ICardGroup cardGroup, HashMap<IMagicCard, Integer> map, int i) {
		int gi = i;
		for (ICard el : cardGroup) {
			if (el instanceof ICardGroup) {
				CardGroup gr = (CardGroup) el;
				if (gr.getFieldIndex() != MagicCardField.NAME)
					i++;
				i = addFromGroup(gr, map, i);
			} else if (el instanceof IMagicCard)
				map.put((IMagicCard) el, gi);
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
		for (IMagicCard card : fstore) {
			addNewFigureIfNotFound(card);
		}
	}

	protected CardFigure addNewFigureIfNotFound(IMagicCard card) {
		CardFigure found = findCardFigure(card);
		if (found != null) {
			// refresh image
			found.loadImage(false);
			return found;
		}
		// new card
		CardFigure cf = new CardFigure(this, card);
		Rectangle bounds = getBounds();
		cf.setLocation(new Random().nextInt(bounds.width - cf.getBounds().width),
				new Random().nextInt(bounds.height - cf.getBounds().height));
		children.add(cf);
		return cf;
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

	@Override
	public synchronized void resize(Rectangle newsize) {
		Rectangle client = canvas.getClientArea();
		Rectangle res = newsize.union(client);
		super.resize(res);
		canvas.setImage(getImage());
		super.redraw();
	}

	@Override
	public IStructuredSelection getSelection() {
		if (selected != null)
			return new StructuredSelection(selected.getCard());
		else
			return new StructuredSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		IMagicCard firstElement = (IMagicCard) ((IStructuredSelection) selection).getFirstElement();
		CardFigure findCardFigure = findCardFigure(firstElement);
		if (findCardFigure == null)
			return;
		this.selected = findCardFigure;
		super.setSelection(selection);
	}
}
