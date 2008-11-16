package com.reflexit.magiccards.ui.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog2;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.utils.TextConvertor;
import com.reflexit.magiccards.ui.views.columns.ColumnManager;
import com.reflexit.magiccards.ui.views.search.ISearchRunnable;
import com.reflexit.magiccards.ui.views.search.SearchContext;
import com.reflexit.magiccards.ui.views.search.SearchControl;
import com.reflexit.magiccards.ui.views.search.TableSearch;

public abstract class AbstractCardsView extends ViewPart {
	protected Action showFilter;
	protected Action doubleClickAction;
	protected Action showPrefs;
	protected Action showFind;
	protected Action copyText;
	protected ViewerManager manager;
	private Label statusLine;
	protected MenuManager sortMenu;
	protected MenuManager groupMenu;
	private IPreferenceStore store;
	private SearchControl searchControl;

	/**
	 * The constructor.
	 */
	public AbstractCardsView() {
	}

	public ViewerManager getManager() {
		return this.manager;
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
		createSearchControl(composite);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		IContextService contextService = (IContextService) getSite().getService(IContextService.class);
		IContextActivation contextActivation = contextService.activateContext("com.reflexit.magiccards.ui.context");
		// ADD the JFace Viewer as a Selection Provider to the View site.
		getSite().setSelectionProvider(this.manager.getSelectionProvider());
		loadInitial();
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
		initManager();
		MagicUIActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this.preferenceListener);
	}

	/**
	 * 
	 */
	protected void initManager() {
		getPreferenceStore().setDefault(FilterHelper.GROUP_INDEX, -1);
		int index = getPreferenceStore().getInt(FilterHelper.GROUP_INDEX);
		this.manager.updateGroupBy(index);
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
	}

	protected void loadInitial() {
		// update manager columns
		IPreferenceStore store = MagicUIActivator.getDefault().getPreferenceStore();
		String value = store.getString(getPrefenceColumnsId());
		AbstractCardsView.this.manager.updateColumns(value);
		this.manager.loadData();
	}

	/**
	 * @param composite
	 */
	protected void createSearchControl(Composite composite) {
		this.searchControl = new SearchControl(new ISearchRunnable() {
			public void run(SearchContext context) {
				runSearch(context);
			}
		}) {
			@Override
			public void setVisible(boolean vis) {
				super.setVisible(vis);
				if (AbstractCardsView.this.showFind != null)
					AbstractCardsView.this.showFind.setEnabled(!vis);
			}
		};
		this.searchControl.createFindBar(composite);
		this.searchControl.setVisible(false);
	}

	/**
	 * @param context
	 */
	protected void runSearch(SearchContext context) {
		TableSearch.search(context, getFilteredStore());
		if (context.status) {
			highlightCard((IMagicCard) context.last);
		}
	}

	/**
	 * @param last
	 */
	protected void highlightCard(IMagicCard last) {
		this.manager.getViewer().setSelection(new StructuredSelection(last), true);
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
		this.manager.hookContextMenu(menuMgr);
		getSite().registerContextMenu(menuMgr, this.manager.getSelectionProvider());
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		setGlobalHandlers(bars);
		bars.updateActionBars();
	}
	public static final String FIND = "org.eclipse.ui.edit.findReplace";

	/**
	 * @param bars
	 */
	protected void setGlobalHandlers(IActionBars bars) {
		//	this.showFind.setActionDefinitionId(FIND);
		ActionHandler findHandler = new ActionHandler(this.showFind);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler(FIND, findHandler);
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.showFilter);
		manager.add(this.sortMenu);
		manager.add(this.groupMenu);
		manager.add(this.showPrefs);
		manager.add(new Separator());
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.showFilter);
		manager.add(this.copyText);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.showFind);
		manager.add(this.showFilter);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}
	class GroupAction extends Action {
		int index;

		GroupAction(String name, int index) {
			super(name, Action.AS_RADIO_BUTTON);
			this.index = index;
			int gindex = getPreferenceStore().getInt(FilterHelper.GROUP_INDEX);
			if (index == gindex) {
				setChecked(true);
			}
		}

		@Override
		public void run() {
			if (isChecked())
				actionGroupBy(this.index);
		}
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
		int i = 0;
		for (Iterator iterator = columns.iterator(); iterator.hasNext(); i++) {
			final ColumnManager man = (ColumnManager) iterator.next();
			String name = man.getColumnFullName();
			final int index = i;
			Action ac = new Action(name) {
				@Override
				public void run() {
					AbstractCardsView.this.manager.sort(index);
				}
			};
			this.sortMenu.add(ac);
		}
		this.groupMenu = new MenuManager("Group By");
		this.groupMenu.add(new GroupAction("None", -1));
		this.groupMenu.add(new GroupAction("Color", IMagicCard.INDEX_COST));
		this.groupMenu.add(new GroupAction("Cost", IMagicCard.INDEX_CMC));
		this.showPrefs = new Action("Preferences...") {
			@Override
			public void run() {
				String id = getPreferencePageId();
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id },
				        null);
				dialog.open();
			}
		};
		this.showFind = new Action("Find...") {
			@Override
			public void run() {
				AbstractCardsView.this.searchControl.setVisible(true);
				AbstractCardsView.this.showFind.setEnabled(false);
			}
		};
		this.showFind.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/search.gif"));
		this.copyText = new Action("Copy") {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			@Override
			public void run() {
				runCopy();
			}
		};
	}

	/**
	 * 
	 */
	protected void runCopy() {
		IStructuredSelection sel = (IStructuredSelection) getViewer().getSelection();
		if (sel.isEmpty())
			return;
		StringBuffer buf = new StringBuffer();
		for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			buf.append(TextConvertor.toText(card));
			buf.append("--------------------------");
		}
		String textData = buf.toString();
		if (textData.length() > 0) {
			final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
			TextTransfer textTransfer = TextTransfer.getInstance();
			MagicCardTransfer mt = MagicCardTransfer.getInstance();
			cb.setContents(new Object[] { textData, sel.getFirstElement() }, new Transfer[] { textTransfer, mt });
		}
	}

	/**
	 * @param indexCost
	 */
	protected void actionGroupBy(int index) {
		getPreferenceStore().setValue(FilterHelper.GROUP_INDEX, index);
		if (index != -1)
			this.manager.filter.setSortIndex(index);
		this.manager.filter.setAscending(false);
		this.manager.updateGroupBy(index);
		this.manager.loadData();
	}

	protected abstract String getPreferencePageId();

	private void hookDoubleClickAction() {
		this.manager.addDoubleClickListener(new IDoubleClickListener() {
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
	}

	protected void runShowFilter() {
		// CardFilter.open(getViewSite().getShell());
		Dialog cardFilterDialog = new CardFilterDialog2(getShell(), getPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData();
	}

	public void reloadData() {
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

	public IFilteredCardStore getFilteredStore() {
		return this.manager.getFilteredStore();
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