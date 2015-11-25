package com.reflexit.magiccards.ui.utils;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

/**
 * IPostSelectionProvider implementation that delegates to another ISelectionProvider or
 * IPostSelectionProvider. The selection provider used for delegation can be exchanged dynamically.
 * Registered listeners are adjusted accordingly. This utility class may be used in workbench parts
 * with multiple viewers.
 * 
 * @author Marc R. Hoffmann
 */
public class SelectionProviderIntermediate extends StoredSelectionProvider {
	private ArrayList<ISelectionProvider> delegates;

	public SelectionProviderIntermediate() {
		delegates = new ArrayList<>();
	}
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			setStoredSelection(selection);
			fireSelectionChanged(selection);
		}
	};
	private ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			setStoredSelection(selection);
			firePostSelectionChanged(selection);
		}
	};

	/**
	 * Sets a new selection provider to delegate to. Selection listeners registered with the
	 * previous delegate are removed before.
	 * 
	 * @param newDelegate
	 *            new selection provider
	 */
	public void setSelectionProviderDelegate(ISelectionProvider newDelegate) {
		ISelectionProvider delegate = getDelegate();
		if (delegate == newDelegate) {
			return;
		}
		removeDelegate(delegate);
		addDelegate(newDelegate);
	}

	public ISelectionProvider getDelegate() {
		ISelectionProvider delegate = null;
		if (delegates.size() > 0)
			delegate = delegates.get(delegates.size() - 1);
		return delegate;
	}

	public void addDelegate(ISelectionProvider newDelegate) {
		if (newDelegate != null) {
			delegates.add(newDelegate);
			newDelegate.addSelectionChangedListener(selectionListener);
			if (newDelegate instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) newDelegate).addPostSelectionChangedListener(postSelectionListener);
			}
			super.setSelection(newDelegate.getSelection());
		}
	}

	public void removeDelegate(ISelectionProvider delegate) {
		if (delegate != null) {
			delegate.removeSelectionChangedListener(selectionListener);
			if (delegate instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) delegate).removePostSelectionChangedListener(postSelectionListener);
			}
			delegates.remove(delegate);
		}
	}

	@Override
	public ISelection getSelection() {
		return super.getSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		ISelectionProvider delegate = getDelegate();
		if (delegate != null) {
			if (delegate instanceof Viewer) {
				((Viewer) delegate).setSelection(selection, true);
			} else {
				delegate.setSelection(selection);
			}
		}
		setStoredSelection(selection);
	}
}