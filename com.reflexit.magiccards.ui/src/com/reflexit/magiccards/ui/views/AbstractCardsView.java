package com.reflexit.magiccards.ui.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog2;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.views.columns.ColumnManager;

public abstract class AbstractCardsView extends ViewPart {
	private Action showFilter;
	private Action doubleClickAction;
	protected ViewerManager manager;
	private Label statusLine;
	private MenuManager sortMenu;
	private Action showPrefs;
	private IPreferenceStore store;

	/**
	 * The constructor.
	 */
	public AbstractCardsView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		composite.setLayout(gl);
		createStatusLine(composite);
		createMainControl(composite);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		IContextService contextService = (IContextService) getSite().getService(IContextService.class);
		IContextActivation contextActivation = contextService.activateContext("com.reflexit.magiccards.ui.context");
	}
	IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			AbstractCardsView.this.propertyChange(event);
		}
	};

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.manager = doGetViewerManager(this);
		MagicUIActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this.preferenceListener);
	}

	private void createStatusLine(Composite composite) {
		this.statusLine = new Label(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 5;
		this.statusLine.setLayoutData(gd);
		this.statusLine.setText("Status");
	}

	protected void createMainControl(Composite parent) {
		Control control = this.manager.createContents(parent);
		((Composite) control).setLayoutData(new GridData(GridData.FILL_BOTH));
		// ADD the JFace Viewer as a Selection Provider to the View site.
		getSite().setSelectionProvider(this.manager.getViewer());
		// update manager columns
		IPreferenceStore store = MagicUIActivator.getDefault().getPreferenceStore();
		String value = store.getString(getPrefenceColumnsId());
		AbstractCardsView.this.manager.updateColumns(value);
	}

	public abstract ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView);

	public ColumnViewer getViewer() {
		return this.manager.getViewer();
	}

	public void setStatus(String text) {
		this.statusLine.setText(text);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractCardsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, getViewer());
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.showFilter);
		manager.add(this.sortMenu);
		manager.add(this.showPrefs);
		manager.add(new Separator());
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.showFilter);
		manager.add(this.sortMenu);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.showFilter);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}

	protected void makeActions() {
		this.showFilter = new Action() {
			@Override
			public void run() {
				runShowFilter();
			}
		};
		this.showFilter.setText("Filter...");
		this.showFilter.setToolTipText("Opens a Card Filter Dialog");
		this.showFilter.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/filter.gif"));
		// double cick
		this.doubleClickAction = new Action() {
			@Override
			public void run() {
				runDoubleClick();
			}
		};
		this.sortMenu = new MenuManager("Sort By");
		Collection columns = this.manager.getColumns();
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			final ColumnManager man = (ColumnManager) iterator.next();
			String name = man.getColumnFullName();
			Action ac = new Action(name) {
				@Override
				public void run() {
					AbstractCardsView.this.manager.sort(man.getDataIndex());
				}
			};
			this.sortMenu.add(ac);
		}
		this.showPrefs = new Action("Preferences...") {
			@Override
			public void run() {
				String id = getPreferencePageId();
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id },
				        null);
				dialog.open();
			}
		};
	}

	protected abstract String getPreferencePageId();

	private void hookDoubleClickAction() {
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				AbstractCardsView.this.doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(getViewSite().getShell(), "Magic Cards", message);
	}

	@Override
	public void dispose() {
		this.manager.dispose();
		MagicUIActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this.preferenceListener);
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		getViewer().getControl().setFocus();
	}

	protected void runDoubleClick() {
		ISelection selection = getViewer().getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		showMessage("Double-click detected on " + obj.toString());
	}

	protected void runShowFilter() {
		// CardFilter.open(getViewSite().getShell());
		Dialog cardFilterDialog = new CardFilterDialog2(getShell(), getPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			this.manager.loadData();
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	public abstract IFilteredCardStore doGetFilteredStore();

	protected void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(getPrefenceColumnsId())) {
			this.manager.updateColumns((String) event.getNewValue());
		}
		;
	}

	/**
	 * @return id of the preference for columns layout and hidings, i.e. 
	 * @see PreferenceConstants.MDBVIEW_COLS
	 */
	abstract protected String getPrefenceColumnsId();

	/**
	 * @return
	 */
	public IPreferenceStore getPreferenceStore() {
		if (this.store == null)
			this.store = new PrefixedPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore(),
			        getPreferencePageId());
		return this.store;
	}
}