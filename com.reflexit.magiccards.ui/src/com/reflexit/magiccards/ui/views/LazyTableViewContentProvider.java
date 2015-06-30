/**
 *
 */
package com.reflexit.magiccards.ui.views;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

class LazyTableViewContentProvider implements ILazyContentProvider {
	LazyTableViewContentProvider() {
	}

	private TableViewer tableViewer;
	private IFilteredCardStore root;
	private TIntObjectHashMap<Object> map = new TIntObjectHashMap<>();

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TableViewer) {
			this.tableViewer = (TableViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore) {
			this.root = (IFilteredCardStore) newInput;
			tableViewer.setItemCount(root.getSize());
		}
		else
			this.root = null;
		map.clear();
	}

	@Override
	public void dispose() {
		this.tableViewer = null;
		this.root = null;
		this.map = null;
	}

	public int getSize(Object newInput) {
		if (newInput instanceof IFilteredCardStore) {
			return ((IFilteredCardStore) newInput).getSize();
		}
		return 0;
	}

	@Override
	public void updateElement(int index) {
		if (this.root != null) {
			if (index >= root.getSize()) {
				// element is gone...
				tableViewer.setItemCount(root.getSize());
			} else {
				Object element = this.root.getElement(index);
				//Object cur = map.get(index);
				//if (cur == element) return;
				MagicLogger.trace("table update element " + index + " " + element);
				//map.put(index, element);
				if (element == null) {
					tableViewer.setItemCount(root.getSize());
					return;
				}
				this.tableViewer.replace(element, index);
			}
		}
	}
}