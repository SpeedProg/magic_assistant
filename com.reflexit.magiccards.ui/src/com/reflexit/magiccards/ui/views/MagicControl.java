package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class MagicControl extends AbstractViewPage {
	public MagicControl() {
	}

	@Override
	public void createPageContents(Composite parent) {
		createMainControl(parent);
		loadInitial(); // XXX reloadData()?
	}

	@Override
	public void activate() {
		super.activate();
		addListeners();
		// getViewSite().setSelectionProvider(getSelectionProvider());// XXX
		refresh();
	}

	@Override
	public void deactivate() {
		removeListeners();
		super.deactivate();
	}

	protected IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			MagicControl.this.propertyChange(event);
		}
	};

	protected void addListeners() {
		MagicUIActivator.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(preferenceListener);
	}

	@Override
	public void dispose() {
		deactivate();
	}

	protected void removeListeners() {
		MagicUIActivator.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(preferenceListener);
	}

	@Override
	public abstract ISelectionProvider getSelectionProvider();

	public abstract void createMainControl(Composite parent);

	protected void loadInitial() {
		// override
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		getControl().setFocus();
	}

	protected void runDoubleClick() {
		// override
	}

	public Shell getShell() {
		return getControl().getShell();
	}

	protected void propertyChange(PropertyChangeEvent event) {
		// override
	}
}