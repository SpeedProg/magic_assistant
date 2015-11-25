package com.reflexit.magiccards.ui.utils;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.events.ListenerList;

/**
 * Provides default implementation of storedSelection provider which manager a single
 * storedSelection and sectionListeners (and post storedSelection listeners)
 * 
 * @author Alena
 * 
 */
public class StoredSelectionProvider implements IPostSelectionProvider {
	private ISelection storedSelection = StructuredSelection.EMPTY;
	private final ListenerList selectionListeners = new ListenerList();
	private final ListenerList postSelectionListeners = new ListenerList();

	@Override
	public void setSelection(ISelection selection) {
		setStoredSelection(selection);
		fireSelectionChanged(storedSelection);
		firePostSelectionChanged(storedSelection);
	}

	public void setStoredSelection(ISelection selection) {
		if (selection == null)
			storedSelection = StructuredSelection.EMPTY;
		else
			storedSelection = selection;
	}

	@Override
	public ISelection getSelection() {
		return storedSelection;
	}

	protected void fireSelectionChanged(ISelection selection) {
		fireSelectionChanged(selectionListeners, selection);
	}

	protected void firePostSelectionChanged(ISelection selection) {
		fireSelectionChanged(postSelectionListeners, selection);
	}

	private void fireSelectionChanged(ListenerList list, ISelection selection) {
		if (list.isEmpty())
			return;
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
		Object[] listeners = list.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
			try {
				listener.selectionChanged(event);
			} catch (Throwable t) {
				MagicLogger.log(t);
			}
		}
	}

	// IPostSelectionProvider Implementation
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	@Override
	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		postSelectionListeners.add(listener);
	}

	@Override
	public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
		postSelectionListeners.remove(listener);
	}
}