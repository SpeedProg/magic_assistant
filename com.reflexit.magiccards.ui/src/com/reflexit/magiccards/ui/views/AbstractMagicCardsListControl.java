package com.reflexit.magiccards.ui.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;

import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.utils.TextConvertor;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.search.ISearchRunnable;
import com.reflexit.magiccards.ui.views.search.SearchContext;
import com.reflexit.magiccards.ui.views.search.SearchControl;
import com.reflexit.magiccards.ui.views.search.TableSearch;

/**
 * Magic card list control - MagicControl that represents list of cards (tree or
 * table), and comes with actions and preferences to manipulate this list
 * 
 */
public class AbstractMagicCardsListControl extends MagicControl {
	public class GroupAction extends Action {
		ICardField field;

		public GroupAction(String name, ICardField field) {
			super(name, IAction.AS_RADIO_BUTTON);
			this.field = field;
			String val = prefStore.getString(FilterHelper.GROUP_FIELD);
			if (field == null && val.length() == 0 || field != null && field.toString().equals(val)) {
				setChecked(true);
			}
		}

		@Override
		public void run() {
			if (isChecked())
				actionGroupBy(this.field);
		}
	}

	public static final String FIND = "org.eclipse.ui.edit.findReplace";
	private final AbstractCardsView abstractCardsView;
	private MenuManager menuGroup;
	private PrefixedPreferenceStore prefStore;
	private QuickFilterControl quickFilter;
	private SearchControl searchControl;
	private Label statusLine;
	private Composite topBar;
	protected Action actionCopyText;
	protected Action actionGroupMenu;
	protected Action actionRefresh;
	protected Action actionShowFilter;
	protected Action actionShowFind;
	protected Action actionShowPrefs;
	protected ViewerManager manager;
	private MenuManager menuSort;
	protected ISelection revealSelection;
	private MagicCardFilter filter = new MagicCardFilter();
	private IFilteredCardStore<ICard> fstore;

	/**
	 * The constructor.
	 */
	public AbstractMagicCardsListControl(AbstractCardsView abstractCardsView) {
		if (abstractCardsView == null)
			throw new NullPointerException();
		this.abstractCardsView = abstractCardsView;
		prefStore = PreferenceInitializer.getLocalStore(getPreferencePageId());
		this.manager = doGetViewerManager();
		initManager();
	}

	@Override
	public void createMainControl(Composite partControl) {
		createTopBar(partControl);
		createTableControl(partControl);
		createSearchControl(partControl);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public Control createPartControl(Composite parent) {
		return super.createPartControl(parent);
	}

	@Override
	public void dispose() {
		this.manager.dispose();
		super.dispose();
	}

	public IFilteredCardStore<ICard> doGetFilteredStore() {
		return abstractCardsView.doGetFilteredStore();
	}

	public ViewerManager doGetViewerManager() {
		return abstractCardsView.doGetViewerManager();
	}

	public MagicCardFilter getFilter() {
		return filter;
	}

	public IFilteredCardStore getFilteredStore() {
		return fstore;
	}

	public Action getGroupAction() {
		return actionGroupMenu;
	}

	public MenuManager getGroupMenu() {
		return menuGroup;
	}

	/**
	 * @return
	 */
	public PrefixedPreferenceStore getLocalPreferenceStore() {
		return this.prefStore;
	}

	public IMagicColumnViewer getManager() {
		return this.manager;
	}

	@Override
	public ISelection getSelection() {
		ISelection selection;
		try {
			selection = manager.getSelectionProvider().getSelection();
		} catch (Exception e) {
			selection = new StructuredSelection();
		}
		// System.err.println("current selection 2 " +
		// manager.getSelectionProvider() + " " + selection);
		return selection;
	}

	public Action getShowFilterAction() {
		return actionShowFilter;
	}

	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		int filSize = filteredStore.getSize();
		int totalSize = cardStore.size();
		if (totalSize == 0)
			return "";
		int count = totalSize;
		if (cardStore instanceof ICardCountable) {
			count = ((ICardCountable) cardStore).getCount();
		}
		if (count != totalSize) {
			return "Total " + count + " cards, shown unique " + filSize + " of " + totalSize;
		} else {
			return "Shown unique " + filSize + " of " + totalSize;
		}
	}

	public Composite getTopBar() {
		return topBar;
	}

	public ColumnViewer getViewer() {
		return this.manager.getViewer();
	}

	public void hookContextMenu(MenuManager menuMgr) {
		manager.hookContextMenu(menuMgr);
	}

	@Override
	public void init(IViewSite site) {
		super.init(site);
	}

	public void reloadData() {
		updateFilter(getFilter());
		abstractCardsView.loadData(null);
	}

	public void runFind() {
		AbstractMagicCardsListControl.this.searchControl.setVisible(true);
		AbstractMagicCardsListControl.this.actionShowFind.setEnabled(false);
	}

	public void setFilteredCardStore(IFilteredCardStore<ICard> fstore) {
		this.fstore = fstore;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		getViewer().getControl().setFocus();
	}

	public void setNextSelection(StructuredSelection structuredSelection) {
		revealSelection = structuredSelection;
	}

	public void setStatus(String text) {
		this.statusLine.setText(text);
	}

	/**
	 * 
	 */
	public void shuffle() {
		sort(-2);
	}

	public void unsort() {
		getManager().updateSortColumn(-1);
	}

	public void updateSingle(ICard source) {
		manager.getViewer().update(source, null);
		updateStatus();
		getViewer().setSelection(new StructuredSelection(source), true);
	}

	private Composite createStatusLine(Composite composite) {
		Composite comp = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		this.statusLine = new Label(comp, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 5;
		this.statusLine.setLayoutData(gd);
		this.statusLine.setText("Status");
		return comp;
	}

	private HashMap<String, String> storeToMap(IPreferenceStore store) {
		HashMap<String, String> map = new HashMap<String, String>();
		Collection col = FilterHelper.getAllIds();
		for (Iterator iterator = col.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			String value = store.getString(id);
			if (value != null && value.length() > 0) {
				map.put(id, value);
				// System.err.println(id + "=" + value);
			}
		}
		return map;
	}

	/**
	 * @param indexCost
	 */
	protected void actionGroupBy(ICardField field) {
		prefStore.setValue(FilterHelper.GROUP_FIELD, field == null ? "" : field.toString());
		updateGroupBy(field);
		reloadData();
	}

	/**
	 * @param indexCmc
	 */
	public void updateGroupBy(ICardField field) {
		MagicCardFilter filter = getFilter();
		ICardField oldIndex = filter.getGroupField();
		if (oldIndex == field)
			return;
		boolean hasGroups = field != null;
		if (hasGroups)
			filter.setSortField(field, true);
		filter.setGroupField(field);
		manager.flip(hasGroups);
	}

	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By", MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"), null);
		groupMenu.add(new GroupAction("None", null));
		groupMenu.add(new GroupAction("Color", MagicCardField.COST));
		groupMenu.add(new GroupAction("Cost", MagicCardField.CMC));
		groupMenu.add(new GroupAction("Type", MagicCardField.TYPE));
		groupMenu.add(new GroupAction("Set", MagicCardField.SET));
		groupMenu.add(new GroupAction("Rarity", MagicCardField.RARITY));
		// groupMenu.add(new GroupAction("Name", MagicCardField.NAME));
		return groupMenu;
	}

	/**
	 * @param composite
	 * @return
	 */
	protected QuickFilterControl createQuickFilterControl(Composite composite) {
		this.quickFilter = new QuickFilterControl(composite, new Runnable() {
			public void run() {
				reloadData();
			}
		});
		return quickFilter;
	}

	/**
	 * @param composite
	 */
	protected void createSearchControl(Composite composite) {
		this.searchControl = new SearchControl(new ISearchRunnable() {
			public void run(SearchContext context) {
				runSearch(context);
			}
		});
		this.searchControl.createFindBar(composite);
		this.searchControl.setVisible(false);
		this.searchControl.setSearchAsYouType(true);
	}

	protected void createTableControl(Composite parent) {
		Control control = this.manager.createContents(parent);
		((Composite) control).setLayoutData(new GridData(GridData.FILL_BOTH));
		hookDragAndDrop();
	}

	public void hookDragAndDrop() {
		getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
		getViewer().addDropSupport(ops, transfers, new MagicCardDropAdapter(getViewer()));
	}

	protected Composite createTopBar(Composite composite) {
		topBar = new Composite(composite, SWT.BORDER);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		topBar.setLayout(layout);
		Control two = createQuickFilterControl(topBar);
		two.setLayoutData(new GridData());
		Control one = createStatusLine(topBar);
		one.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		topBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return topBar;
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.actionShowFilter);
		manager.add(this.actionShowPrefs);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.actionShowFilter);
		manager.add(this.actionShowFind);
		manager.add(this.actionShowPrefs);
		manager.add(this.menuSort);
		manager.add(this.getGroupMenu());
		manager.add(this.actionRefresh);
		manager.add(new Separator());
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				viewMenuIsAboutToShow(manager);
			}
		});
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.actionGroupMenu);
		manager.add(this.actionShowPrefs);
		manager.add(this.actionShowFind);
		manager.add(this.actionShowFilter);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}

	protected String getPreferencePageId() {
		return abstractCardsView.getPreferencePageId();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return manager.getSelectionProvider();
	}

	/**
	 * @param last
	 */
	protected void highlightCard(IMagicCard last) {
		this.manager.getViewer().setSelection(new StructuredSelection(last), true);
	}

	@Override
	protected void hookDoubleClickAction() {
		this.manager.hookDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				AbstractMagicCardsListControl.this.doubleClickAction.run();
			}
		});
	}

	/**
	 *
	 */
	protected void initManager() {
		prefStore.setDefault(FilterHelper.GROUP_FIELD, "");
		String field = prefStore.getString(FilterHelper.GROUP_FIELD);
		updateGroupBy(MagicCardFieldPhysical.fieldByName(field));
		IColumnSortAction sortAction = new IColumnSortAction() {
			public void sort(int i) {
				AbstractMagicCardsListControl.this.sort(i);
			}
		};
		this.manager.hookSortAction(sortAction);
	}

	@Override
	protected void loadInitial() {
		// update manager columns
		String value = prefStore.getString(PreferenceConstants.LOCAL_COLUMNS);
		AbstractMagicCardsListControl.this.manager.updateColumns(value);
		quickFilter.setPreferenceStore(prefStore);
		boolean qf = prefStore.getBoolean(PreferenceConstants.LOCAL_SHOW_QUICKFILTER);
		setQuickFilterVisible(qf);
		reloadData();
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.actionShowFilter = new Action() {
			@Override
			public void run() {
				runShowFilter();
			}
		};
		this.actionShowFilter.setText("Filter...");
		this.actionShowFilter.setToolTipText("Opens a Card Filter Dialog");
		this.actionShowFilter.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/filter.gif"));
		this.menuSort = new MenuManager("Sort By");
		Collection columns = getManager().getColumnsCollection().getColumns();
		int i = 0;
		for (Iterator iterator = columns.iterator(); iterator.hasNext(); i++) {
			final AbstractColumn man = (AbstractColumn) iterator.next();
			String name = man.getColumnFullName();
			final int index = i;
			Action ac = new Action(name, IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					updateSortColumn(index);
					reloadData();
				}
			};
			this.menuSort.add(ac);
		}
		this.actionGroupMenu = new Action("Group By", Action.AS_DROP_DOWN_MENU) {
			{
				setMenuCreator(new IMenuCreator() {
					private Menu listMenu;

					public void dispose() {
						if (listMenu != null)
							listMenu.dispose();
					}

					public Menu getMenu(Control parent) {
						if (listMenu != null)
							listMenu.dispose();
						listMenu = createGroupMenu().createContextMenu(parent);
						return listMenu;
					}

					public Menu getMenu(Menu parent) {
						return null;
					}
				});
			}

			@Override
			public void run() {
				String group = prefStore.getString(FilterHelper.GROUP_FIELD);
				if (group == null || group.length() == 0)
					actionGroupBy(MagicCardField.CMC);
				else
					actionGroupBy(null);
			}
		};
		this.actionGroupMenu.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.menuGroup = createGroupMenu();
		// this.groupMenu.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.actionShowPrefs = new Action("Preferences...") {
			@Override
			public void run() {
				String id = getPreferencePageId();
				if (id != null) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null);
					dialog.open();
				}
			}
		};
		this.actionShowPrefs.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/table.gif"));
		this.actionShowFind = new Action("Find...") {
			@Override
			public void run() {
				runFind();
			}
		};
		this.actionShowFind.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/search.gif"));
		this.actionCopyText = new Action("Copy") {
			@Override
			public void run() {
				runCopy();
			}
		};
		this.actionRefresh = new Action("Refresh") {
			@Override
			public void run() {
				refresh();
			}
		};
		this.actionRefresh.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
	}

	@Override
	protected void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(prefStore.toGlobal(PreferenceConstants.LOCAL_COLUMNS))) {
			this.manager.updateColumns((String) event.getNewValue());
			refresh();
		} else if (property.equals(PreferenceConstants.SHOW_GRID)) {
			refresh();
		} else if (property.equals(prefStore.toGlobal(PreferenceConstants.LOCAL_SHOW_QUICKFILTER))) {
			boolean qf = (Boolean) event.getNewValue();
			setQuickFilterVisible(qf);
		}
	}

	@Override
	protected void refresh() {
		reloadData();
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
			buf.append("--------------------------\n");
		}
		String textData = buf.toString();
		if (textData.length() > 0) {
			final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
			TextTransfer textTransfer = TextTransfer.getInstance();
			MagicCardTransfer mt = MagicCardTransfer.getInstance();
			IMagicCard[] cards = (IMagicCard[]) sel.toList().toArray(new IMagicCard[sel.size()]);
			cb.setContents(new Object[] { textData, cards }, new Transfer[] { textTransfer, mt });
		}
	}

	/**
	 * @param context
	 */
	protected void runSearch(final SearchContext context) {
		TableSearch.search(context, getFilteredStore());
		if (context.isFound()) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					highlightCard((IMagicCard) context.getLast());
				}
			});
		}
	}

	protected void runShowFilter() {
		// CardFilter.open(getViewSite().getShell());
		Dialog cardFilterDialog = new CardFilterDialog(getShell(), prefStore);
		if (cardFilterDialog.open() == IStatus.OK) {
			revealSelection = getSelection();
			reloadData();
			quickFilter.refresh();
		}
	}

	/**
	 * @param bars
	 */
	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		actionShowFind.setActionDefinitionId(FIND);
		ActionHandler findHandler = new ActionHandler(this.actionShowFind);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler(FIND, findHandler);
	}

	protected void setQuickFilterVisible(boolean qf) {
		quickFilter.setVisible(qf);
		partControl.layout(true);
	}

	protected void sort(int index) {
		updateSortColumn(index);
		abstractCardsView.loadData(null);
	}

	protected void updateFilter(MagicCardFilter filter) {
		IPreferenceStore store = getLocalPreferenceStore();
		HashMap<String, String> map = storeToMap(store);
		filter.update(map);
		filter.setOnlyLastSet(store.getBoolean(EditionsFilterPreferencePage.LAST_SET));
	}

	protected void updateStatus() {
		setStatus(getStatusMessage());
	}

	/**
	 * Update view in UI thread after data load is finished
	 */
	protected void updateViewer() {
		if (manager.getControl().isDisposed())
			return;
		ISelection selection = getSelection();
		IFilteredCardStore filteredStore = getFilteredStore();
		manager.updateViewer(filteredStore);
		if (revealSelection != null) {
			// set desired selection
			getSelectionProvider().setSelection(revealSelection);
			revealSelection = null;
		} else {
			// restore selection
			getSelectionProvider().setSelection(selection);
		}
		updateStatus();
	}

	protected void viewMenuIsAboutToShow(IMenuManager manager) {
		actionShowFind.setEnabled(!searchControl.isVisible());
	}

	protected void updateSortColumn(final int index) {
		manager.updateSortColumn(index);
		if (index > 0) {
			int sortDirection = manager.getSortDirection();
			AbstractColumn man = (AbstractColumn) getViewer().getLabelProvider(index);
			getFilter().setSortField(man.getSortField(), sortDirection == SWT.DOWN);
		} else {
			getFilter().setNoSort();
		}
	}
}