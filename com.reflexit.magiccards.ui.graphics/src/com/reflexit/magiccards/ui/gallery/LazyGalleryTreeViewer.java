/*******************************************************************************
 * Copyright (c) 2015 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors :
 *    Alena Laskavaia - initial implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.gallery;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.nebula.jface.galleryviewer.GalleryTreeViewer;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import com.reflexit.magiccards.ui.utils.ImageCreator;

/**
 * This is super lazy viewer which works with non-lazy provider. It will not create tree items or ask for labels of
 * element until they are visible.
 * 
 * @author elaskavaia
 *
 */
public class LazyGalleryTreeViewer extends GalleryTreeViewer {
	private static final String CHILDREN = "c";
	private int expandToLevel;

	public LazyGalleryTreeViewer(Composite parent) {
		super(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
		setUseHashlookup(true);
		gallery.setVirtualGroups(true);
		gallery.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				associate((GalleryItem) event.item);
			}
		});
		setGroupsVisible(true);
	}

	public void setGroupsVisible(boolean visible) {
		if (visible)
			gallery.setGroupRenderer(new DefaultGalleryGroupRenderer());
		else
			gallery.setGroupRenderer(new NoGroupRenderer());
		Image v = ImageCreator.getInstance().getCardNotFoundImageTemplate();
		AbstractGridGroupRenderer groupRenderer = (AbstractGridGroupRenderer) gallery.getGroupRenderer();
		groupRenderer.setItemSize(v.getBounds().width + 8, v.getBounds().height + 20 + 8);
		groupRenderer.setMinMargin(4);
	}

	public void associate(GalleryItem item) {
		int index;
		GalleryItem parentItem = item.getParentItem();
		Widget widget;
		if (parentItem != null) {
			index = parentItem.indexOf(item);
			widget = parentItem;
		} else {
			index = gallery.indexOf(item);
			widget = gallery;
		}
		Object[] children = (Object[]) widget.getData(CHILDREN);
		if (children == null) {
			return;
		}
		Object element = children[index];
		// System.out.println("setData index " + index + " of " + element);
		associateAndUpdate(item, element);
	}

	protected void associateAndUpdate(Item item, Object element) {
		associate(element, item);
		updateItem(item, element);
		createChildren(item);
	}

	public LazyGalleryTreeViewer getViewer() {
		return this;
	}

	public ISelectionProvider getSelectionProvider() {
		return this;
	}

	@Override
	protected void disassociate(Item item) {
		if (item.isDisposed())
			return;
		Object element = item.getData();
		Assert.isNotNull(element);
		// Clear the map before we clear the data
		unmapElement(element, item);
		item.setData(null);
		item.setData(CHILDREN, null);
		if (usingElementMap()) {
			disassociateChildren(item);
		}
	}

	protected void disassociateChildren(Widget widget) {
		Item[] items = getChildren(widget);
		for (int i = 0; i < items.length; i++) {
			Item child = items[i];
			if (child != null && child.getData() != null) {
				disassociate(child);
			}
		}
	}

	@Override
	protected void internalRefresh(Widget widget, Object element, boolean doStruct, boolean updateLabels) {
		if (element == null)
			return;
		Widget w = findItem(element);
		if (w instanceof GalleryItem) {
			disassociate((GalleryItem) w);
			((GalleryItem) w).clearAll();
			associateAndUpdate((GalleryItem) w, element);
		} else if (w == gallery) {
			gallery.setData(element);
			if (doStruct) {
				if (usingElementMap()) {
					disassociateChildren(gallery);
				}
				gallery.removeAll();
				createChildren(gallery);
			}
		}
	}

	@Override
	protected void internalInitializeTree(Control tree) {
		// this is overriden so we can call getAutoExpandLevel which we override
		createChildren(tree);
		internalExpandToLevel(tree, getAutoExpandLevel());
	}

	@Override
	public void setAutoExpandLevel(int level) {
		// have to override this because parent class does not allow to do it
		expandToLevel = level;
	}

	@Override
	public int getAutoExpandLevel() {
		return expandToLevel;
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		gallery.setRedraw(false);
		try {
			Object[] expanded = getExpandedElements();
			super.inputChanged(input, oldInput);
			setExpandedElements(expanded);
			GalleryItem[] selection = gallery.getSelection();
			if (selection.length > 0) {
				showItem(selection[selection.length - 1]);
			} else {
				if (expanded.length > 0) {
					Object elementOrTreePath = expanded[0];
					Widget w = internalExpand(elementOrTreePath, true);
					if (w instanceof Item) {
						Item child = getChild(w, 0);
						if (child != null)
							showItem(child);
					}
				} else {
					Item child = getChild(gallery, 0);
					if (child != null)
						setExpanded(child, true);
				}
				// can use gallery.translation
				// gallery.getVerticalBar().setSelection(vpos);
				// gallery.redraw();
			}
		} finally {
			gallery.setRedraw(true);
		}
		gallery.redraw();
	}

	@Override
	public void setExpandedElements(Object[] elements) {
		assertElementsNotNull(elements);
		if (checkBusy()) {
			return;
		}
		for (int i = 0; i < elements.length; ++i) {
			Object element = elements[i];
			// Ensure item exists for element.
			Widget w = internalExpand(element, false);
			if (w == null)
				w = doFindItem(element);
			if (w instanceof Item)
				setExpanded((Item) w, true);
		}
	}

	@Override
	protected Widget doFindItem(Object element) {
		// this will only find group elements but its ok
		return doFindItem(gallery, element);
	}

	protected Widget doFindItem(Widget parent, Object element) {
		Object[] data = (Object[]) parent.getData(CHILDREN);
		if (data == null)
			return null;
		int i;
		for (i = 0; i < data.length; i++) {
			Object object = data[i];
			if (object.equals(element))
				break;
		}
		if (i < data.length) {
			Item item = getChild(parent, i);
			if (item == null) {
				return materializeItem(parent, element, i);
			} else
				return item;
		}
		return null;
	}

	@Override
	protected Widget internalGetWidgetToSelect(Object elementOrTreePath) {
		if (elementOrTreePath instanceof TreePath) {
			TreePath treePath = (TreePath) elementOrTreePath;
			int segments = treePath.getSegmentCount();
			if (segments == 0) {
				return getControl();
			}
			Object top = treePath.getFirstSegment();
			Widget topW = findItem(top);
			if (topW == null) {
				topW = doFindItem(gallery, top);
			}
			if (segments == 1)
				return topW;
			Object bottom = treePath.getLastSegment();
			Widget[] candidates = findItems(bottom);
			if (candidates.length == 0 && topW == null) {
				return null;
			}
			for (int i = 0; i < candidates.length; i++) {
				Widget candidate = candidates[i];
				if (!(candidate instanceof Item)) {
					continue;
				}
				if (treePath.equals(getTreePathFromItem((Item) candidate), getComparer())) {
					return candidate;
				}
			}
			Widget bottomW = doFindItem(topW, bottom);
			return bottomW;
		}
		return findItem(elementOrTreePath);
	}

	@Override
	public Object[] getExpandedElements() {
		ArrayList<Object> result = new ArrayList<Object>();
		Item[] items = gallery.getItems();
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			if (item != null) {
				Object data = item.getData();
				if (data != null && getExpanded(item))
					result.add(data);
			}
		}
		return result.toArray();
	}

	@Override
	protected void createChildren(final Widget widget) {
		Object element = widget.getData();
		Object[] children = getSortedChildren(element);
		widget.setData(CHILDREN, children);
		if (widget == gallery) {
			int nonlazy = getNonLazyCategoryCount();
			if (children.length < nonlazy)
				nonlazy = children.length;
			for (int i = 0; i < nonlazy; i++) {
				Object child = children[i];
				materializeItem(widget, child, i);
			}
			gallery.setItemCount(children.length); // lazy
		} else {
			// do not actually create items, we are ui lazy, but we create count
			((GalleryItem) widget).setItemCount(children.length);
		}
	}

	/**
	 * How many of category items (top level) we create non-lazily. If all lazy it its lagging on first repaint
	 * 
	 * @return
	 */
	protected int getNonLazyCategoryCount() {
		return 30;
	}

	private Item materializeItem(final Widget parent, Object element, int i) {
		Item item = newItem(parent, SWT.NULL, i);
		associateAndUpdate(item, element);
		return item;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		try {
			super.setSelection(selection, reveal);
		} catch (NullPointerException e) {
			// sadly. This will happend if item was not instantiated
		}
	}
}
