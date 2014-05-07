package com.reflexit.magiccards.ui.utils;

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
	private ISelectionProvider delegate;
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			fireSelectionChanged(event.getSelection());
		}
	};
	private ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			firePostSelectionChanged(event.getSelection());
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
		if (delegate == newDelegate) {
			return;
		}
		if (delegate != null) {
			delegate.removeSelectionChangedListener(selectionListener);
			if (delegate instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) delegate).removePostSelectionChangedListener(postSelectionListener);
			}
		}
		delegate = newDelegate;
		if (newDelegate != null) {
			newDelegate.addSelectionChangedListener(selectionListener);
			if (newDelegate instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) newDelegate).addPostSelectionChangedListener(postSelectionListener);
			}
			fireSelectionChanged(newDelegate.getSelection());
			firePostSelectionChanged(newDelegate.getSelection());
		}
	}

	@Override
	public ISelection getSelection() {
		return delegate == null ? null : delegate.getSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		if (delegate != null) {
			if (delegate instanceof Viewer) {
				((Viewer) delegate).setSelection(selection, true);
			} else {
				delegate.setSelection(selection);
			}
		}
	}
}