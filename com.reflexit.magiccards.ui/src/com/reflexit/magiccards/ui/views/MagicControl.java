package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class MagicControl implements IMagicControl {
	protected Composite partControl;
	private IViewSite site;

	public MagicControl() {
	}

	@Override
	public Control createPartControl(Composite parent) {
		partControl = new Composite(parent, SWT.NONE);
		partControl.setLayout(GridLayoutFactory.fillDefaults().create());
		partControl.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		// partControl.setBackground(partControl.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		createMainControl(partControl);
		site.setSelectionProvider(getSelectionProvider());
		makeActions();
		loadInitial();
		return partControl;
	}

	protected void makeActions() {
		// make view actions if any
	}

	protected IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			MagicControl.this.propertyChange(event);
		}
	};

	@Override
	public void init(IViewSite site) {
		this.site = site;
		MagicUIActivator.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(preferenceListener);
	}

	@Override
	public void dispose() {
		MagicUIActivator.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(preferenceListener);
	}

	@Override
	public abstract ISelectionProvider getSelectionProvider();

	public IViewSite getSite() {
		return site;
	}

	public void setSite(IViewSite site) {
		this.site = site;
	}

	public abstract void createMainControl(Composite parent);

	protected void loadInitial() {
		// override
	}

	/**
	 * @param bars
	 */
	@Override
	public void setGlobalControlHandlers(IActionBars bars) {
		// override
	}

	@Override
	public void fillLocalPullDown(IMenuManager manager) {
		// override
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		// override
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
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

	@Override
	public Control getControl() {
		return partControl;
	}

	protected void propertyChange(PropertyChangeEvent event) {
		// override
	}

	@Override
	public void refresh() {
		// override
	}

	public abstract ISelection getSelection();
}