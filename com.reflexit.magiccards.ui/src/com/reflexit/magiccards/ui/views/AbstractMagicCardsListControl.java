package com.reflexit.magiccards.ui.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.commands.ShowFilterHandler;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.utils.TextConvertor;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.search.ISearchRunnable;
import com.reflexit.magiccards.ui.views.search.SearchContext;
import com.reflexit.magiccards.ui.views.search.SearchControl;
import com.reflexit.magiccards.ui.views.search.TableSearch;
import com.reflexit.magiccards.ui.widgets.QuickFilterControl;

/**
 * Magic card list control - MagicControl that represents list of cards (tree or
 * table), and comes with actions and preferences to manipulate this list
 *
 */
public abstract class AbstractMagicCardsListControl extends MagicControl implements IMagicCardListControl,
		ICardEventListener {
	private static final DataManager DM = DataManager.getInstance();

	public class GroupAction extends Action {
		ICardField field[];

		public GroupAction(String name, ICardField fields[], boolean checked) {
			super(name, IAction.AS_RADIO_BUTTON);
			this.field = fields;
			if (checked) {
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
	protected final AbstractCardsView abstractCardsView;
	private MenuManager menuGroup;
	private PrefixedPreferenceStore prefStore;
	private QuickFilterControl quickFilter;
	private SearchControl searchControl;
	private Label statusLine;
	private Composite topBar;
	protected Action actionGroupMenu;
	protected Action actionShowFilter;
	protected Action actionResetFilter;
	protected Action actionShowFind;
	protected Action actionShowPrefs;
	protected Action actionUnsort;
	protected IMagicColumnViewer manager;
	private MenuManager menuSort;
	protected ISelection revealSelection;
	protected IFilteredCardStore<ICard> fstore;
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			// selection changes on own view
			setStatus(getStatusMessage());
		}
	};
	private Label warning;

	/**
	 * The constructor.
	 */
	public AbstractMagicCardsListControl(AbstractCardsView abstractCardsView) {
		this.abstractCardsView = abstractCardsView;
		prefStore = PreferenceInitializer.getLocalStore(getPreferencePageId());
		this.manager = createViewerManager();
		if (abstractCardsView != null)
			setSite(abstractCardsView.getViewSite());
	}

	@Override
	public void createMainControl(Composite partControl) {
		createTopBar(partControl);
		createTableControl(partControl);
		createSearchControl(partControl);
		getSelectionProvider().addSelectionChangedListener(selectionListener);
	}

	public GroupAction createGroupAction(ICardField field) {
		return createGroupAction(field.getLabel(), field);
	}

	public GroupAction createGroupAction(String name, ICardField[] fields) {
		String val = getLocalPreferenceStore().getString(FilterField.GROUP_FIELD.toString());
		String vname = createGroupName(fields);
		boolean checked = vname.equals(val);
		return new GroupAction(name, fields, checked);
	}

	public GroupAction createGroupAction(String name, ICardField field) {
		return createGroupAction(name, new ICardField[] { field });
	}

	public GroupAction createGroupActionNone() {
		String val = getLocalPreferenceStore().getString(FilterField.GROUP_FIELD.toString());
		return new GroupAction("None", null, val == null || val.length() == 0);
	}

	protected String createGroupName(ICardField[] fields) {
		String res = "";
		for (int i = 0; i < fields.length; i++) {
			ICardField field = fields[i];
			if (i != 0) {
				res += "/";
			}
			res += field.toString();
		}
		return res;
	}

	private ICardField[] getGroupFieldsByName(String name) {
		if (name == null || name.length() == 0)
			return null;
		String sfields[] = name.split("/");
		ICardField[] res = new ICardField[sfields.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = MagicCardField.fieldByName(sfields[i]);
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.reflexit.magiccards.ui.views.IMagicCardListControl#createPartControl
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPartControl(Composite parent) {
		initManager();
		return super.createPartControl(parent);
	}

	public abstract IMagicColumnViewer createViewerManager();

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicCardListControl#getFilter()
	 */
	@Override
	public MagicCardFilter getFilter() {
		if (getFilteredStore() == null)
			return null;
		return getFilteredStore().getFilter();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.reflexit.magiccards.ui.views.IMagicCardListControl#getFilteredStore()
	 */
	@Override
	public IFilteredCardStore getFilteredStore() {
		if (fstore == null)
			fstore = doGetFilteredStore();
		return fstore;
	}

	public Action getGroupAction() {
		return actionGroupMenu;
	}

	@Override
	public MenuManager getGroupMenu() {
		return menuGroup;
	}

	/**
	 * @return
	 */
	@Override
	public PrefixedPreferenceStore getLocalPreferenceStore() {
		return this.prefStore;
	}

	@Override
	public PrefixedPreferenceStore getFilterPreferenceStore() {
		return this.prefStore;
	}

	public IMagicColumnViewer getManager() {
		return this.manager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.reflexit.magiccards.ui.views.IMagicCardListControl#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		ISelection selection[] = new ISelection[] { new StructuredSelection() };
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					selection[0] = manager.getSelectionProvider().getSelection();
				} catch (Exception e) {
					MagicUIActivator.log(e);
				}
			}
		});
		// System.err.println("current selection 2 " +
		// manager.getSelectionProvider() + " " +
		// selection);
		return selection[0];
	}

	public Action getShowFilterAction() {
		return actionShowFilter;
	}

	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		int shownSize = filteredStore.getUniqueCount();
		int storeSize = cardStore.size();
		if (storeSize == 0)
			return "";
		int storeCount = storeSize;
		if (cardStore instanceof ICardCountable) {
			storeCount = ((ICardCountable) cardStore).getCount();
		}
		int shownCount = shownSize;
		if (storeCount != storeSize) // collection not db
			shownCount = filteredStore.getCount();
		String mainMessage = "Total " + cardsUnique(shownCount, storeCount, shownSize, storeSize) + " cards";
		// if (shownSize != storeSize) { // filter is active
		// mainMessage += ". Filtered " + (storeCount - shownCount);
		// }
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if (sel != null && !sel.isEmpty()) { // selection
			int selCount = CardStoreUtils.countCards(sel.toList());
			int selSize = sel.size();
			mainMessage += ". Selected " + cardsUnique(selCount, selCount, selSize, selSize);
		}
		return mainMessage;
	}

	private String cardsUnique(int a, int ta, int b, int tb) {
		if (a != b)
			return countOf(a, ta) + " (unique " + countOf(b, tb) + ")";
		else
			return countOf(a, ta) + "";
	}

	private String countOf(int a, int ta) {
		if (a != ta)
			return a + " of " + ta;
		else
			return a + "";
	}

	public Composite getTopBar() {
		return topBar;
	}

	protected ColumnViewer getViewer() {
		return this.manager.getViewer();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.reflexit.magiccards.ui.views.IMagicCardListControl#hookContextMenu
	 * (org.eclipse.jface.action.MenuManager)
	 */
	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		manager.hookContextMenu(menuMgr);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.reflexit.magiccards.ui.views.IMagicCardListControl#init(org.eclipse
	 * .ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) {
		super.init(site);
		addStoreChangeListener();
	}

	protected void addStoreChangeListener() {
		new Thread("Offline listeners") {
			@Override
			public void run() {
				if (DM.waitForInit(60)) {
					DM.getLibraryCardStore().addListener(AbstractMagicCardsListControl.this);
					DM.getMagicDBStore().addListener(AbstractMagicCardsListControl.this);
				} else {
					MagicLogger.log("Timeout on waiting for db init. Listeners are not installed.");
				}
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.IMagicCardListControl#dispose()
	 */
	@Override
	public void dispose() {
		// this.manager.dispose(); TODO
		removeStoreChangeListener();
		getSelectionProvider().removeSelectionChangedListener(selectionListener);
		super.dispose();
	}

	protected void removeStoreChangeListener() {
		DataManager.getInstance().getLibraryCardStore().removeListener(this);
	}

	@Override
	public void reloadData() {
		setNextSelection(getSelection());
		update();
		loadData(null);
	}

	public void runFind() {
		searchControl.setVisible(true);
		// actionShowFind.setEnabled(false);
	}

	// public void setFilteredCardStore(IFilteredCardStore<ICard> fstore) {
	// this.fstore = fstore;
	// }
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		manager.getControl().setFocus();
	}

	@Override
	public void setNextSelection(ISelection structuredSelection) {
		revealSelection = structuredSelection;
	}

	@Override
	public void setStatus(String text) {
		if (statusLine.isDisposed())
			return;
		if (statusLine.getText().equals(text))
			return;
		this.statusLine.setText(text);
		this.statusLine.setToolTipText(text);
		if (text.isEmpty()) {
			statusLine.setVisible(false);
		} else {
			statusLine.setVisible(true);
		}
		statusLine.getParent().layout(true, true);
	}

	public void setWarning(boolean war) {
		if (warning.isDisposed())
			return;
		warning.setVisible(war);
		warning.setToolTipText("There are " + getFiltered() + " hidden cards!\nChange filter to see more");
		warning.getParent().layout(true, true);
	}

	@Override
	public void updateSingle(ICard source) {
		ColumnViewer viewer = getViewer();
		if (viewer == null)
			return;
		viewer.update(source, null);
		updateStatus();
		// getSelectionProvider().setSelection(new StructuredSelection(source));
	}

	private Label createStatusLine(Composite composite) {
		Label statusLine = new Label(composite, SWT.NONE);
		statusLine.setText("Status");
		return statusLine;
	}

	private HashMap<String, String> storeToMap(IPreferenceStore store) {
		HashMap<String, String> map = new HashMap<String, String>();
		Collection col = FilterField.getAllIds();
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
	protected void actionGroupBy(ICardField[] fields) {
		getLocalPreferenceStore().setValue(FilterField.GROUP_FIELD.toString(),
				fields == null ? "" : createGroupName(fields));
		updateGroupBy(fields);
		reloadData();
	}

	/**
	 * @param indexCmc
	 */
	public void updateGroupBy(ICardField[] fields) {
		MagicCardFilter filter = getFilter();
		if (filter == null)
			return;
		ICardField[] oldIndex = filter.getGroupFields();
		if (Arrays.equals(oldIndex, fields))
			return;
		if (fields != null) {
			filter.setSortField(fields[0], true);
			filter.setGroupFields(fields);
			manager.flip(true);
		} else {
			filter.setGroupFields(null);
			manager.flip(false);
		}
	}

	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By",
				MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"), null);
		groupMenu.add(createGroupActionNone());
		groupMenu.add(createGroupAction("Color", MagicCardField.COST));
		groupMenu.add(createGroupAction("Cost", MagicCardField.CMC));
		groupMenu.add(createGroupAction(MagicCardField.TYPE));
		groupMenu.add(createGroupAction("Core/Block/Set/Rarity", new ICardField[] { MagicCardField.SET_CORE,
				MagicCardField.SET_BLOCK,
				MagicCardField.SET, MagicCardField.RARITY }));
		groupMenu.add(createGroupAction(MagicCardField.SET));
		groupMenu.add(createGroupAction("Set/Rarity", new ICardField[] { MagicCardField.SET,
				MagicCardField.RARITY }));
		groupMenu.add(createGroupAction(MagicCardField.RARITY));
		groupMenu.add(createGroupAction(MagicCardField.NAME));
		groupMenu.setRemoveAllWhenShown(false);
		// groupMenu.add(new GroupAction("Name", MagicCardField.NAME));
		return groupMenu;
	}

	/**
	 * @param composite
	 * @return
	 */
	protected QuickFilterControl createQuickFilterControl(Composite composite) {
		QuickFilterControl quickFilter = new QuickFilterControl(composite, new Runnable() {
			@Override
			public void run() {
				reloadData();
			}
		}, false);
		return quickFilter;
	}

	/**
	 * @param composite
	 */
	protected void createSearchControl(Composite composite) {
		this.searchControl = new SearchControl(new ISearchRunnable() {
			@Override
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
		this.manager.hookContext("com.reflexit.magiccards.ui.context");
	}

	protected Composite createTopBar(Composite composite) {
		topBar = new Composite(composite, SWT.NONE);
		topBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		topBar.setLayout(layout);
		quickFilter = createQuickFilterControl(topBar);
		quickFilter.setLayoutData(new GridData());
		statusLine = createStatusLine(topBar);
		statusLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		warning = new Label(topBar, SWT.NONE);
		warning.setImage(MagicUIActivator.getImageDescriptor("icons/clcl16/exclamation.gif").createImage());
		warning.setToolTipText("There are filtered cards!");
		warning.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				actionShowFilter.run();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// nothing
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				actionShowFilter.run();
			}
		});
		return topBar;
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		manager.add(this.actionShowFilter);
		manager.add(this.actionResetFilter);
		manager.add(this.actionShowPrefs);
	}

	@Override
	public void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.actionShowFilter);
		manager.add(this.actionResetFilter);
		manager.add(this.actionShowFind);
		manager.add(this.actionShowPrefs);
		manager.add(this.menuSort);
		manager.add(this.getGroupMenu());
		manager.add(new Separator());
		manager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				viewMenuIsAboutToShow(manager);
			}
		});
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		if (actionGroupMenu != null)
			manager.add(this.actionGroupMenu);
		manager.add(this.actionShowPrefs);
		manager.add(this.actionShowFind);
		manager.add(this.actionShowFilter);
		manager.add(this.actionResetFilter);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}

	protected String getPreferencePageId() {
		if (abstractCardsView != null)
			return abstractCardsView.getPreferencePageId();
		return getClass().getName();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return manager.getSelectionProvider();
	}

	/**
	 * @param last
	 */
	protected void highlightCard(Object last) {
		ISelectionProvider selectionProvider = getSelectionProvider();
		StructuredSelection selection = new StructuredSelection(last);
		if (selectionProvider instanceof Viewer) {
			((Viewer) selectionProvider).setSelection(selection, true);
		} else {
			selectionProvider.setSelection(selection);
		}
	}

	@Override
	protected void hookDoubleClickAction() {
		this.manager.hookDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				AbstractMagicCardsListControl.this.doubleClickAction.run();
			}
		});
	}

	/**
	 *
	 */
	protected void initManager() {
		String field = getLocalPreferenceStore().getString(FilterField.GROUP_FIELD.toString());
		updateGroupBy(getGroupFieldsByName(field));
		IColumnSortAction sortAction = new IColumnSortAction() {
			@Override
			public void sort(int i) {
				AbstractMagicCardsListControl.this.sort(i);
			}
		};
		this.manager.hookSortAction(sortAction);
	}

	@Override
	protected void loadInitial() {
		PrefixedPreferenceStore ps = getLocalPreferenceStore();
		// update manager columns
		String value = ps.getString(PreferenceConstants.LOCAL_COLUMNS);
		AbstractMagicCardsListControl.this.manager.updateColumns(value);
		quickFilter.setPreferenceStore(getFilterPreferenceStore());
		boolean qf = ps.getBoolean(PreferenceConstants.LOCAL_SHOW_QUICKFILTER);
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
		this.actionShowFilter.setImageDescriptor(MagicUIActivator
				.getImageDescriptor("icons/clcl16/filter_ps.png"));
		this.actionResetFilter = new Action() {
			@Override
			public void run() {
				runResetFilter();
			}
		};
		this.actionResetFilter.setText("Reset Filter");
		this.actionResetFilter.setToolTipText("Resets the filter to default values");
		this.actionResetFilter.setImageDescriptor(MagicUIActivator
				.getImageDescriptor("icons/clcl16/reset_filter.gif"));
		this.actionUnsort = new Action("Unsort") {
			@Override
			public void run() {
				unsort();
				reloadData();
			}

			@Override
			public String getToolTipText() {
				return "Remove current sorting order";
			}
		};
		this.menuSort = new MenuManager("Sort By");
		this.menuSort.add(actionUnsort);
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
		createGroupAction();
		this.menuGroup = createGroupMenu();
		// this.groupMenu.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.actionShowPrefs = new Action("Preferences...") {
			@Override
			public void run() {
				String id = getPreferencePageId();
				if (id != null) {
					saveColumnLayout();
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id,
							new String[] { id }, null);
					dialog.open();
				}
			}
		};
		this.actionShowPrefs
				.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/table.gif"));
		this.actionShowFind = new Action("Find...") {
			@Override
			public void run() {
				runFind();
			}
		};
		this.actionShowFind
				.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/search.png"));
	}

	public class GroupByToolBarAction extends Action {
		public GroupByToolBarAction() {
			super("Group By", IAction.AS_DROP_DOWN_MENU);
			setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
			setMenuCreator(new IMenuCreator() {
				private Menu listMenu;

				@Override
				public void dispose() {
					if (listMenu != null)
						listMenu.dispose();
				}

				@Override
				public Menu getMenu(Control parent) {
					if (listMenu != null)
						listMenu.dispose();
					listMenu = createGroupMenu().createContextMenu(parent);
					return listMenu;
				}

				@Override
				public Menu getMenu(Menu parent) {
					return null;
				}
			});
		}

		@Override
		public void run() { // group button itself
			String group = getLocalPreferenceStore().getString(FilterField.GROUP_FIELD.toString());
			if (group == null || group.length() == 0)
				actionGroupBy(new ICardField[] { MagicCardField.CMC });
			else
				actionGroupBy(null);
		}
	}

	protected void createGroupAction() {
		this.actionGroupMenu = new GroupByToolBarAction();
	}

	@Override
	protected void propertyChange(PropertyChangeEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (manager.getViewer() == null || manager.getViewer().getControl() == null)
					return;
				String property = event.getProperty();
				PrefixedPreferenceStore ps = getLocalPreferenceStore();
				Object newValue = event.getNewValue();
				if (property.equals(ps.toGlobal(PreferenceConstants.LOCAL_COLUMNS))) {
					manager.updateColumns((String) newValue);
					refresh();
				} else if (property.equals(PreferenceConstants.SHOW_GRID)) {
					refresh();
				} else if (property.equals(ps.toGlobal(PreferenceConstants.LOCAL_SHOW_QUICKFILTER))) {
					boolean qf = Boolean.valueOf(newValue.toString());
					setQuickFilterVisible(qf);
				} else if (newValue instanceof FontData[] || newValue instanceof RGB) {
					refresh();
				}
			}
		});
	}

	@Override
	public void refresh() {
		manager.refresh();
	}

	/**
	 *
	 */
	@Override
	public void runCopy() {
		Control fc = partControl.getDisplay().getFocusControl();
		if (fc instanceof Text)
			((Text) fc).copy();
		else if (fc instanceof Combo)
			((Combo) fc).copy();
		else if (fc instanceof Tree || fc instanceof Table) {
			IStructuredSelection sel = (IStructuredSelection) getSelectionProvider().getSelection();
			if (sel.isEmpty())
				return;
			StringBuffer buf = new StringBuffer();
			for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
				Object line = iterator.next();
				buf.append(TextConvertor.toText(line));
			}
			String textData = buf.toString();
			if (textData.length() > 0) {
				final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				MagicCardTransfer mt = MagicCardTransfer.getInstance();
				Collection list = DataManager.expandGroups(sel.toList());
				IMagicCard[] cards = (IMagicCard[]) list.toArray(new IMagicCard[sel.size()]);
				cb.setContents(new Object[] { textData, cards }, new Transfer[] { textTransfer, mt });
			}
		} else {
			MagicUIActivator.log("Copy from " + fc + "?");
		}
	}

	/**
	 * @param context
	 */
	protected void runSearch(final SearchContext context) {
		TableSearch.search(context, getFilteredStore());
		if (context.isFound()) {
			final Object last = context.getLast();
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					highlightCard(last);
				}
			});
		}
	}

	protected void runShowFilter() {
		if (ShowFilterHandler.execute()) {
			reloadData();
			syncQuickFilter();
		}
		// CardFilter.open(getViewSite().getShell());
		// Dialog cardFilterDialog = new CardFilterDialog(getShell(),
		// getFilterPreferenceStore());
		// if (cardFilterDialog.open() == IStatus.OK) {
		// reloadData();
		// quickFilter.refresh();
		// }
	}

	protected void runResetFilter() {
		getSelectionProvider().setSelection(new StructuredSelection()); // remove selection
		Collection<String> allIds = FilterField.getAllIds();
		for (Iterator<String> iterator = allIds.iterator(); iterator.hasNext();) {
			String id = iterator.next();
			getFilterPreferenceStore().setToDefault(id);
		}
		getFilterPreferenceStore().setToDefault(EditionsFilterPreferencePage.LAST_SET);
		reloadData();
		syncQuickFilter();
	}

	public void syncQuickFilter() {
		boolean sup = quickFilter.isSuppressUpdates();
		quickFilter.setSuppressUpdates(true);
		try {
			quickFilter.refresh();
		} finally {
			quickFilter.setSuppressUpdates(sup);
		}
	}

	/**
	 * @param bars
	 */
	@Override
	public void setGlobalControlHandlers(IActionBars bars) {
		if (abstractCardsView != null) {
			abstractCardsView.activateActionHandler(actionShowFind, FIND);
		}
	}

	protected void setQuickFilterVisible(boolean qf) {
		quickFilter.setVisible(qf);
		partControl.layout(true);
	}

	protected void sort(int index) {
		updateSortColumn(index);
		loadData(null);
	}

	public void unsort() {
		updateSortColumn(-1);
	}

	protected void update() {
		MagicCardFilter filter = getFilter();
		if (filter == null)
			return;
		IPreferenceStore store = getFilterPreferenceStore();
		HashMap<String, String> map = storeToMap(store);
		filter.update(map);
		filter.setOnlyLastSet(store.getBoolean(EditionsFilterPreferencePage.LAST_SET));
	}

	protected void updateStatus() {
		setStatus(getStatusMessage());
		setWarning(getFiltered() != 0);
	}

	protected int getFiltered() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore != null) {
			ICardStore cardStore = filteredStore.getCardStore();
			int shownSize = filteredStore.getUniqueCount();
			int storeSize = cardStore.size();
			return storeSize - shownSize;
		}
		return 0;
	}

	/**
	 * Update view in UI thread after data load is finished
	 */
	@Override
	public void updateViewer() {
		MagicLogger.traceStart("updateViewer");
		try {
			if (manager.getControl() == null || manager.getControl().isDisposed())
				return;
			ISelection selection = getSelection();
			getSelectionProvider().setSelection(new StructuredSelection());
			IFilteredCardStore filteredStore = getFilteredStore();
			manager.updateViewer(filteredStore);
			MagicLogger.trace("updateViewer", "setSelection");
			if (revealSelection != null) {
				// set desired selection
				getSelectionProvider().setSelection(revealSelection);
				revealSelection = null;
			} else {
				// restore selection
				getSelectionProvider().setSelection(selection);
			}
			updateStatus();
		} catch (Exception e) {
			MagicLogger.log("Exception during update operation");
			MagicLogger.log(e);
		} finally {
			MagicLogger.traceEnd("updateViewer");
		}
	}

	protected void viewMenuIsAboutToShow(IMenuManager manager) {
		actionShowFind.setEnabled(!searchControl.isVisible());
	}

	protected void updateSortColumn(final int index) {
		if (index >= 0) {
			AbstractColumn man = (AbstractColumn) getViewer().getLabelProvider(index);
			ICardField sortField = man.getSortField();
			if (sortField == null && man instanceof GroupColumn)
				sortField = getFilter().getGroupField();
			if (sortField == null)
				sortField = MagicCardField.NAME;
			boolean acc = true;
			SortOrder sortOrder = getFilter().getSortOrder();
			if (sortOrder.isTop(sortField)) {
				boolean oldAcc = sortOrder.isAccending(sortField);
				acc = !oldAcc;
			}
			getFilter().setSortField(sortField, acc);
			manager.setSortColumn(index, acc ? 1 : -1);
		} else {
			manager.setSortColumn(-1, 0);
			getFilter().setNoSort();
		}
	}

	@Override
	public void runPaste() {
		MagicCardTransfer mt = MagicCardTransfer.getInstance();
		Object contents = mt.fromClipboard();
		if (contents instanceof Collection) {
			DM.copyCards(DM.resolve((Collection) contents), getFilteredStore().getCardStore());
		} else {
			Control fc = partControl.getDisplay().getFocusControl();
			CopySupport.runPaste(fc);
		}
	}

	@Override
	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		if (type == CardEvent.UPDATE) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (event.getSource() instanceof ICard)
						updateSingle((ICard) event.getSource());
				}
			});
		} else if (type == CardEvent.ADD) {
			if (event.getData() instanceof List) {
				List arr = (List) event.getData();
				if (arr.size() == 1)
					setNextSelection(new StructuredSelection(arr));
			} else if (event.getData() instanceof IMagicCard) {
				setNextSelection(new StructuredSelection(event.getData()));
			}
			// System.err.println("Card added: " + revealSelection + " on " +
			// getPartName());
			reloadData();
		} else if (type == CardEvent.REMOVE || type == CardEvent.UPDATE_LIST) {
			// todo set selection to next element
			reloadData();
		}
	}

	@Override
	public void saveColumnLayout() {
		final String value = manager.getColumnLayoutProperty();
		if (value == null || value.isEmpty())
			return;
		MagicUIActivator.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this.preferenceListener);
		try {
			getLocalPreferenceStore().setValue(PreferenceConstants.LOCAL_COLUMNS, value);
		} finally {
			MagicUIActivator.getDefault().getPreferenceStore()
					.addPropertyChangeListener(this.preferenceListener);
		}
	}

	private Object jobFamility = new Object();
	private Job loadingJob;

	public void loadData(final Runnable postLoad) {
		Job[] jobs = Job.getJobManager().find(jobFamility);
		if (jobs.length >= 2) {
			// System.err.println(jobs.length +
			// " already running skipping refresh");
			return;
		}
		final Display display = PlatformUI.getWorkbench().getDisplay();
		loadingJob = new Job("Loading cards") {
			@Override
			public boolean belongsTo(Object family) {
				return family == jobFamility;
			}

			@Override
			public boolean shouldSchedule() {
				Job[] jobs = Job.getJobManager().find(jobFamility);
				if (jobs.length >= 2)
					return false;
				return super.shouldSchedule();
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (jobFamility) {
					try {
						setName("Loading cards");
						checkInit();
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						monitor.subTask("Loading cards...");
						populateStore(monitor);
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						if (getFilteredStore() == null)
							return Status.OK_STATUS;
						getFilteredStore().update();
					} catch (final Exception e) {
						display.syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openError(display.getActiveShell(), "Error", e.getMessage());
							}
						});
						MagicUIActivator.log(e);
						return Status.OK_STATUS;
					}
					// asyncUpdateViewer();
					return Status.OK_STATUS;
				}
			}
		};
		// loadingJob.setRule(jobRule);
		loadingJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (display.isDisposed())
					return;
				if (postLoad != null)
					display.syncExec(postLoad);
				else
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							updateViewer();
						}
					});
				super.done(event);
			}
		});
		loadingJob.schedule(100);
	}

	private void checkInit() {
		try {
			DM.waitForInit(20);
		} catch (MagicException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void populateStore(IProgressMonitor monitor) {
		if (fstore == null) {
			fstore = doGetFilteredStore();
		}
	}

	protected abstract IFilteredCardStore<ICard> doGetFilteredStore();
}