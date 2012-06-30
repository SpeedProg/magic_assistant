package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.contexts.IContextService;

import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class MagicControl implements IMagicControl {
	protected Composite partControl;
	private IViewSite site;
	protected Action doubleClickAction;

	/**
	 * The constructor.
	 */
	public MagicControl() {
	}

	public Control createPartControl(Composite parent) {
		partControl = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		partControl.setLayout(gl);
		createMainControl(partControl);
		partControl.setLayoutData(new GridData(GridData.FILL_BOTH));
		makeActions();
		hookDoubleClickAction();
		loadInitial();
		return partControl;
	}

	protected void makeActions() {
		// make view actions if any
		// double cick
		this.doubleClickAction = new Action() {
			@Override
			public void run() {
				runDoubleClick();
			}
		};
	}

	/**
	 * override to hook double click to something
	 */
	protected void hookDoubleClickAction() {
		//
	}

	protected IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			MagicControl.this.propertyChange(event);
		}
	};

	public void init(IViewSite site) {
		IContextService contextService = (IContextService) site.getService(IContextService.class);
		contextService.activateContext("com.reflexit.magiccards.ui.context");
		site.setSelectionProvider(getSelectionProvider());
		MagicUIActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this.preferenceListener);
		this.site = site;
	}

	public abstract ISelectionProvider getSelectionProvider();

	public IViewSite getSite() {
		return site;
	}

	public void setSite(IViewSite site) {
		this.site = site;
	}

	public abstract void createMainControl(Composite parent);

	protected void loadInitial() {
	}

	/**
	 * @param bars
	 */
	public void setGlobalHandlers(IActionBars bars) {
	}

	public void fillLocalPullDown(IMenuManager manager) {
	}

	public void fillContextMenu(IMenuManager manager) {
	}

	public void fillLocalToolBar(IToolBarManager manager) {
	}

	public void dispose() {
		MagicUIActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this.preferenceListener);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		getControl().setFocus();
	}

	protected void runDoubleClick() {
	}

	public Shell getShell() {
		return getControl().getShell();
	}

	public Control getControl() {
		return partControl;
	}

	protected void propertyChange(PropertyChangeEvent event) {
	}

	public void refresh() {
	}

	public abstract ISelection getSelection();

	public abstract void saveColumnLayout();
}