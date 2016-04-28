package com.reflexit.magiccards.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardComparator;
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
import com.reflexit.magiccards.ui.PerspectiveFactoryMagic;
import com.reflexit.magiccards.ui.actions.GroupByAction;
import com.reflexit.magiccards.ui.actions.ImageAction;
import com.reflexit.magiccards.ui.actions.SearchCardAction;
import com.reflexit.magiccards.ui.actions.ShowPreferencesAction;
import com.reflexit.magiccards.ui.actions.SortAction;
import com.reflexit.magiccards.ui.actions.SortByAction;
import com.reflexit.magiccards.ui.actions.UnsortAction;
import com.reflexit.magiccards.ui.actions.ViewAsAction;
import com.reflexit.magiccards.ui.commands.ShowFilterHandler;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.gallery.SplitGalleryViewer;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
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
public abstract class AbstractMagicCardsListControl extends AbstractViewPage
		implements IMagicCardListControl, ICardEventListener {
	protected static final DataManager DM = DataManager.getInstance();
	private QuickFilterControl quickFilter;
	private SearchControl searchControl;
	private Label statusLine;
	private Composite topToolBar;
	protected GroupByAction actionGroupBy;
	protected ViewAsAction actionViewAs;
	protected Action actionDoubleClick;
	protected Action actionShowFilter;
	protected Action actionResetFilter;
	protected Action actionShowFind;
	protected Action actionShowPrefs;
	protected SortByAction actionSortBy;
	protected IMagicViewer viewer;
	protected ISelection revealSelection;
	protected IFilteredCardStore<ICard> fstore;
	private Presentation presentation = Presentation.TABLE;
	private final boolean fixedPresentation;
	private ISelectionChangedListener statusSelectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			// selection changes on own view
			updateStatus();
		}
	};
	protected IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			AbstractMagicCardsListControl.this.propertyChange(event);
		}
	};
	private Label warning;
	private String statusMessage = "";
	private boolean isFiltered = false;
	private boolean isGroupped = false;
	private Composite mainControl;

	public AbstractMagicCardsListControl(Presentation pres) {
		this.presentation = pres;
		this.fixedPresentation = true;
	}

	public AbstractMagicCardsListControl(boolean fixed) {
		this.presentation = Presentation.TABLE;
		this.fixedPresentation = fixed;
	}

	public void setPresentation(Presentation presentation) {
		if (fixedPresentation) {
			MagicUIActivator.log(new Exception("Cannot call set presentation on static presentation view"));
			return;
		}
		this.presentation = presentation;
	}

	public Presentation getPresentation() {
		return presentation;
	}

	protected void switchPresentation(Presentation selected) {
		if (fixedPresentation) {
			MagicUIActivator.log(new Exception("Cannot call switch presentation on static presentation view"));
			return;
		}
		setPresentation(selected);
		createTableControl(mainControl);
		if (getGroupAction() != null) {
			boolean cangroup = getPresentation() != Presentation.TABLE;
			getGroupAction().setEnabled(cangroup);
		}
		mainControl.layout(true, true);
		refreshViewer();
	}

	public void createMainControl(Composite area) {
		if (!fixedPresentation) {
			String cur = getColumnsPreferenceStore().getString(PreferenceConstants.PRESENTATION_VIEW);
			if (cur != null) {
				try {
					Presentation pres = Presentation.valueOf(cur);
					setPresentation(pres);
				} catch (RuntimeException e) {
					// ignore
				}
			}
		}
		mainControl = new Composite(area, SWT.NONE);
		mainControl.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
		mainControl.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		// partControl.setBackground(partControl.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		createTopBar(mainControl);
		createTableControl(mainControl);
		createSearchControl(mainControl);
		getSelectionProvider().addSelectionChangedListener(statusSelectionListener);
	}

	public IMagicViewer createViewer(Composite parent) {
		MagicColumnCollection columns = new MagicColumnCollection(getPreferencePageId());
		if (presentation == Presentation.TABLE) {
			LazyTableViewer v = new LazyTableViewer(parent, columns);
			return v;
		}
		if (presentation == Presentation.TREE) {
			ExtendedTreeViewer v = new ExtendedTreeViewer(parent, columns);
			// v.setContentProvider(new RootTreeViewerContentProvider());
			return v;
		}
		if (presentation == Presentation.SPLITTREE)
			return new SplitViewer(parent, getPreferencePageId());
		if (presentation == Presentation.GALLERY)
			return new SplitGalleryViewer(parent, getPreferencePageId());
		throw new IllegalArgumentException(presentation.name());
	}

	@Override
	public MagicCardFilter getFilter() {
		if (getFilteredStore() == null)
			return null;
		return getFilteredStore().getFilter();
	}

	@Override
	public synchronized IFilteredCardStore getFilteredStore() {
		if (fstore == null) {
			fstore = doGetFilteredStore();
			if (fstore != null) {
				if (actionSortBy != null)
					actionSortBy.setFilter(fstore.getFilter());
				if (actionGroupBy != null)
					actionGroupBy.setFilter(fstore.getFilter());
			}
		}
		return fstore;
	}

	public Action getGroupAction() {
		return actionGroupBy;
	}

	@Override
	public IPersistentPreferenceStore getColumnsPreferenceStore() {
		return PreferenceInitializer.getLocalStore(getPreferencePageId());
	}

	@Override
	public IPersistentPreferenceStore getElementPreferenceStore() {
		return PreferenceInitializer.getFilterStore(getPreferencePageId());
	}

	public IPersistentPreferenceStore getPresentaionPreferenceStore() {
		return getColumnsPreferenceStore();
	}

	public IMagicViewer getManager() {
		return this.viewer;
	}

	@Override
	public ISelection getSelection() {
		ISelection selection[] = new ISelection[] { new StructuredSelection() };
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					selection[0] = viewer.getSelectionProvider().getSelection();
				} catch (Exception e) {
					MagicUIActivator.log(e);
				}
			}
		});
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
		int shownSize = filteredStore.getFlatSize();
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
		return topToolBar;
	}

	protected Viewer getViewer() {
		return this.viewer.getViewer();
	}

	@Override
	public boolean hookContextMenu(MenuManager menuMgr) {
		return viewer.hookContextMenu(menuMgr);
	}

	protected void addListeners() {
		MagicUIActivator.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(preferenceListener);
		addStoreChangeListener();
		getColumnsPreferenceStore().addPropertyChangeListener(preferenceListener);
	}

	protected void removeListeners() {
		removeStoreChangeListener();
		getColumnsPreferenceStore().removePropertyChangeListener(preferenceListener);
		MagicUIActivator.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(preferenceListener);
	}

	@Override
	public void dispose() {
		if (viewer != null) {
			getSelectionProvider().removeSelectionChangedListener(statusSelectionListener);
			this.viewer.dispose();
		}
		try {
			getColumnsPreferenceStore().save();
			getElementPreferenceStore().save();
			getPresentaionPreferenceStore().save();
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
		deactivate();
		super.dispose();
	}

	protected void addStoreChangeListener() {
		DM.getLibraryCardStore().addListener(AbstractMagicCardsListControl.this);
		DM.getMagicDBStore().addListener(AbstractMagicCardsListControl.this);
	}

	protected void removeStoreChangeListener() {
		DM.getLibraryCardStore().removeListener(this);
		DM.getMagicDBStore().removeListener(AbstractMagicCardsListControl.this);
	}

	public void reGroup() {
		refresh();
	}

	@Override
	public void refresh() {
		MagicLogger.trace("reload data " + getClass());
		setNextSelection(getSelection());
		syncFilter();
		loadData(null);
	}

	protected void syncSortColumnIndicator() {
		if (viewer instanceof IMagicColumnViewer) {
			IMagicColumnViewer cviewer = (IMagicColumnViewer) viewer;
			SortOrder o = getFilter().getSortOrder();
			if (o.isEmpty()) {
				cviewer.setSortColumn(-1, 0);
			} else {
				MagicCardComparator top = o.peek();
				ICardField field = top.getField();
				AbstractColumn column = cviewer.getColumnsCollection().getColumn(field);
				if (column == null && field == MagicCardField.CMC) {
					column = cviewer.getColumnsCollection().getColumn(MagicCardField.COST);
				}
				if (column != null) {
					int index = column.getColumnIndex();
					cviewer.setSortColumn(index, o.isAccending(field) ? -1 : 1);
				} else {
					cviewer.setSortColumn(-1, 0);
				}
			}
		}
	}

	public void refilterData() {
		MagicLogger.trace("refilter data " + getClass());
		setNextSelection(null);
		syncFilter();
		loadData(new Runnable() {
			@Override
			public void run() {
				refreshViewer();
				// select first visible element
				if (fstore.getSize() == 0)
					return;
				Object element = fstore.getElement(0);
				getSelectionProvider().setSelection(new StructuredSelection(element));
			}
		});
	}

	public void runFind() {
		searchControl.setVisible(true);
		searchControl.getControl().setFocus();
	}

	@Override
	public void setNextSelection(ISelection structuredSelection) {
		revealSelection = structuredSelection;
	}

	@Override
	public void setStatus(String text) {
		if (statusLine == null)
			return;
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
		statusLine.getParent().getParent().layout(true, true);
	}

	public void setWarning(boolean war) {
		if (warning == null)
			return;
		if (warning.isDisposed())
			return;
		warning.setVisible(war);
		warning.setToolTipText("There are " + getFiltered() + " hidden cards!\nChange filter to see more");
		warning.getParent().layout(true, true);
	}

	private Label createStatusLine(Composite composite) {
		Label statusLine = new Label(composite, SWT.NONE);
		statusLine.setText("Status");
		return statusLine;
	}

	private HashMap<String, String> storeToMap(IPreferenceStore store) {
		HashMap<String, String> map = new HashMap<>();
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
	 * @param composite
	 * @return
	 */
	protected QuickFilterControl createQuickFilterControl(Composite composite) {
		QuickFilterControl quickFilter = new QuickFilterControl(composite, new Runnable() {
			@Override
			public void run() {
				refilterData();
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
		// searchControl.getControl().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
	}

	protected Control createTableControl(Composite parent) {
		if (viewer != null) {
			viewer.getControl().dispose();
		}
		this.viewer = createViewer(parent);
		Control control = viewer.getControl();
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		// control.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		this.viewer.hookContext(PerspectiveFactoryMagic.TABLES_CONTEXT);
		this.viewer.hookSortAction(this::sort);
		if (searchControl != null)
			this.viewer.getControl().moveAbove(searchControl.getControl());
		return control;
	}

	protected Composite createTopBar(Composite composite) {
		topToolBar = new Composite(composite, SWT.NONE);
		topToolBar.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		topToolBar.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		quickFilter = createQuickFilterControl(topToolBar);
		quickFilter.setLayoutData(new GridData());
		statusLine = createStatusLine(topToolBar);
		statusLine.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create());
		warning = new Label(topToolBar, SWT.NONE);
		warning.setImage(MagicUIActivator.getImage("icons/clcl16/exclamation.gif"));
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
		return topToolBar;
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		// manager.add(this.actionShowFind);
		// manager.add(this.actionShowFilter);
		// manager.add(this.actionResetFilter);
		manager.add(this.actionShowPrefs);
	}

	@Override
	public void fillLocalPullDown(IMenuManager manager) {
		if (actionSortBy != null)
			manager.add(this.actionSortBy.createMenuManager());
		if (actionGroupBy != null)
			manager.add(this.actionGroupBy.createMenuManager());
		manager.add(this.actionShowFilter);
		manager.add(this.actionResetFilter);
		manager.add(this.actionShowPrefs);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		if (actionViewAs != null)
			manager.add(this.actionViewAs);
		if (actionGroupBy != null)
			manager.add(this.actionGroupBy);
		if (actionSortBy != null)
			manager.add(this.actionSortBy);
		if (actionShowFind != null)
			manager.add(this.actionShowFind);
		manager.add(this.actionShowFilter);
		manager.add(this.actionResetFilter);
		manager.add(this.actionShowPrefs);
	};

	protected String getViewPreferencePageId() {
		if (getMagicCardsView() != null)
			return getMagicCardsView().getPreferencePageId();
		return null;
	};

	protected abstract String getPreferencePageId();

	@Override
	public ISelectionProvider getSelectionProvider() {
		return viewer.getSelectionProvider();
	}

	/**
	 * @param last
	 */
	protected void highlightCard(Object last) {
		ISelectionProvider selectionProvider = getSelectionProvider();
		StructuredSelection selection;
		if (last instanceof TreePath) {
			selection = new TreeSelection((TreePath) last);
		} else {
			selection = new StructuredSelection(last);
		}
		if (selectionProvider instanceof Viewer) {
			((Viewer) selectionProvider).setSelection(selection, true);
		} else {
			selectionProvider.setSelection(selection);
		}
	}

	protected void hookDoubleClickAction() {
		// override to hook
		if (actionDoubleClick != null)
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					actionDoubleClick.run();
				}
			});
	}

	protected String getName() {
		if (fstore == null)
			return "";
		Location loc = fstore.getLocation();
		if (loc == null)
			return "";
		return loc.getName();
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.actionShowFilter = new ImageAction("Filter...", "icons/clcl16/filter_ps.png", "Opens a Card Filter Dialog",
				this::runShowFilter);
		this.actionResetFilter = new ImageAction("Reset Filter", "icons/clcl16/reset_filter.gif",
				"Resets the filter to default values", this::runResetFilter);
		this.actionSortBy = new SortByAction(getSortColumnCollection(), null, getPresentaionPreferenceStore(),
				this::refresh);
		this.actionGroupBy = new GroupByAction(getGroups(), null, getPresentaionPreferenceStore(), this::reGroup);
		this.actionShowPrefs = new ShowPreferencesAction(getPreferencePageId()) {
			@Override
			public void before() {
				saveColumnLayout();
			}
		};
		this.actionShowPrefs.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/gear.png"));
		this.actionShowFind = new SearchCardAction(this::runFind);
		if (!fixedPresentation)
			this.actionViewAs = new ViewAsAction(Arrays.asList(Presentation.values()), getColumnsPreferenceStore(),
					this::switchPresentation);
		// double click
		hookDoubleClickAction();
		// disable group by
		if (getPresentation() == Presentation.TABLE)
			getGroupAction().setEnabled(false);
	}

	public ColumnCollection getSortColumnCollection() {
		if (viewer instanceof IMagicColumnViewer) {
			return ((IMagicColumnViewer) viewer).getColumnsCollection();
		}
		return new MagicColumnCollection("");
	}

	protected Collection<GroupOrder> getGroups() {
		ArrayList<GroupOrder> res = new ArrayList<>();
		res.add(new GroupOrder());
		res.add(new GroupOrder("Color", MagicCardField.COST));
		res.add(new GroupOrder("Cost", MagicCardField.CMC));
		res.add(new GroupOrder(MagicCardField.TYPE));
		res.add(new GroupOrder("Core/Block/Set/Rarity", //
				MagicCardField.SET_CORE, MagicCardField.SET_BLOCK, MagicCardField.SET, MagicCardField.RARITY));
		res.add(new GroupOrder(MagicCardField.SET));
		res.add(new GroupOrder(MagicCardField.SET, MagicCardField.RARITY));
		res.add(new GroupOrder(MagicCardField.RARITY));
		res.add(new GroupOrder(MagicCardField.NAME));
		return res;
	}

	protected void propertyChange(PropertyChangeEvent event) {
		if (viewer.getViewer() == null || viewer.getViewer().getControl() == null)
			return;
		String property = event.getProperty();
		Object newValue = event.getNewValue();
		if (property.equals(PreferenceConstants.LOCAL_COLUMNS)) {
			if (viewer instanceof IMagicColumnViewer) {
				WaitUtils.syncExec(() -> {
					synchronized (AbstractMagicCardsListControl.this) {
						((IMagicColumnViewer) viewer).updateColumns((String) newValue);
					}
				});
				scheduleRefreshViewer();
			}
		} else if (property.equals(PreferenceConstants.SHOW_GRID)) {
			scheduleRefreshViewer();
		} else if (property.equals(PreferenceConstants.LOCAL_SHOW_QUICKFILTER)) {
			boolean qf = Boolean.valueOf(newValue.toString());
			WaitUtils.asyncExec(() -> setQuickFilterVisible(qf));
		} else if (newValue instanceof FontData[] || newValue instanceof RGB) {
			scheduleRefreshViewer();
		}
	}

	protected void scheduleRefreshViewer() {
		UIJob uiJob = new UIJob("Refresh") {
			@Override
			public boolean shouldSchedule() {
				Job[] jobs = getJobManager().find(getControl());
				for (Job job : jobs) {
					if (job.getState() == Job.WAITING)
						return false;
				}
				if (jobs.length >= 2)
					return false;
				return super.shouldSchedule();
			}

			@Override
			public boolean shouldRun() {
				Job[] jobs = getJobManager().find(getControl());
				for (Job job : jobs) {
					if (job.getState() == Job.WAITING)
						return false;
				}
				return true;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == getControl();
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				refreshViewer();
				return Status.OK_STATUS;
			}
		};
		uiJob.schedule(10);
	}

	public void runCopy() {
		Control fc = getControl().getDisplay().getFocusControl();
		CopySupport.runCopy(fc);
	}

	/**
	 * @param context
	 */
	protected void runSearch(final SearchContext context) {
		TableSearch.search(context, getFilteredStore());
		if (context.isFound()) {
			final Object last = context.getLast();
			WaitUtils.syncExec(() -> highlightCard(last));
		}
	}

	protected void runShowFilter() {
		if (ShowFilterHandler.execute()) {
			syncQuickFilter();
			refilterData();
		}
	}

	protected void runResetFilter() {
		getSelectionProvider().setSelection(new StructuredSelection()); // remove
																		// selection
		PreferenceInitializer.setToDefault(getElementPreferenceStore());
		syncQuickFilter();
		refilterData();
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

	protected AbstractCardsView getMagicCardsView() {
		if (getViewPart() instanceof AbstractCardsView)
			return (AbstractCardsView) getViewPart();
		return null;
	}

	/**
	 * @param bars
	 */
	@Override
	public void setGlobalHandlers(IActionBars bars) {
		if (getMagicCardsView() != null) {
			getMagicCardsView().activateActionHandler(actionShowFind, actionShowFind.getActionDefinitionId());
		}
	}

	protected void setQuickFilterVisible(boolean qf) {
		if (quickFilter != null)
			quickFilter.setVisible(qf);
	}

	protected void sort(int index, int dir) {
		updateSortColumn(index);
		loadData(null);
	}

	public void unsort() {
		updateSortColumn(-1);
	}

	public void syncFilter() {
		MagicCardFilter filter = getFilter();
		if (filter == null)
			return;
		IPreferenceStore store = getElementPreferenceStore();
		if (quickFilter != null)
			quickFilter.setPreferenceStore(store);
		HashMap<String, String> map = storeToMap(store);
		filter.update(map);
		filter.setOnlyLastSet(store.getBoolean(EditionsFilterPreferencePage.LAST_SET));
		IPersistentPreferenceStore viewSettings = getPresentaionPreferenceStore();
		String fields = viewSettings.getString(PreferenceConstants.GROUP_FIELD);
		GroupOrder groupOrder = new GroupOrder(fields);
		filter.setGroupOrder(groupOrder);
		filter.setSortOrder(SortOrder.valueOf(viewSettings.getString(PreferenceConstants.SORT_ORDER)));
		isGroupped = filter.isGroupped();
	}

	protected void loadInitial() {
		IPreferenceStore ps = getColumnsPreferenceStore();
		if (viewer instanceof IMagicColumnViewer) {
			IMagicColumnViewer cviewer = (IMagicColumnViewer) viewer;
			// update manager columns
			String value = ps.getString(PreferenceConstants.LOCAL_COLUMNS);
			cviewer.updateColumns(value);
		}
		boolean qf = ps.getBoolean(PreferenceConstants.LOCAL_SHOW_QUICKFILTER);
		setQuickFilterVisible(qf);
	}

	public boolean isGroupped() {
		return isGroupped;
	}

	protected void updateStatus() {
		new Job("Status update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				statusMessage = getStatusMessage();
				isFiltered = (getFiltered() != 0);
				WaitUtils.asyncExec(() -> {
					setStatus(statusMessage);
					setWarning(isFiltered);
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	protected int getFiltered() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore != null) {
			ICardStore cardStore = filteredStore.getCardStore();
			int shownSize = filteredStore.getFlatSize();
			int storeSize = cardStore.size();
			return storeSize - shownSize;
		}
		return 0;
	}

	/**
	 * Update view in UI thread after data load is finished
	 */
	public void refreshViewer() {
		IFilteredCardStore filteredStore = getFilteredStore();
		Location location = filteredStore.getLocation();
		Object object = location == null ? getClass() : location;
		final String key = "updateViewer " + object;
		try {
			MagicLogger.traceStart(key);
			if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
				return;
			ISelection selection = getSelection();
			getSelectionProvider().setSelection(new StructuredSelection());
			viewer.setInput(filteredStore);
			restoreSelection(selection);
			updateStatus();
			syncSortColumnIndicator();
		} catch (Exception e) {
			MagicLogger.log(e);
		} finally {
			MagicLogger.traceEnd(key);
		}
	}

	private void restoreSelection(ISelection selection) {
		MagicLogger.trace("restoreSelection " + revealSelection);
		if (revealSelection != null) {
			// set desired selection
			selection = revealSelection;
			revealSelection = null;
		}
		// System.err.println("set selection " + selection + " in " +
		// getFilteredStore().getLocation());
		if (!selection.isEmpty())
			getSelectionProvider().setSelection(selection);
		// MagicLogger.traceEnd("restoreSelection");
	}

	protected void updateSortColumn(final int index) {
		if (viewer instanceof IMagicColumnViewer) {
			IMagicColumnViewer cviewer = (IMagicColumnViewer) viewer;
			GroupOrder groupOrder = null; // do not sort by group order
											// automatically
			if (index >= 0) {
				AbstractColumn man = (AbstractColumn) cviewer.getColumnViewer().getLabelProvider(index);
				ICardField sortField = man != null ? man.getSortField() : null;
				if (sortField == null && man instanceof GroupColumn)
					sortField = getFilter().getGroupField();
				if (sortField == null)
					return;
				final ICardField so = sortField;
				new SortAction(sortField.getLabel(), sortField, getFilter().getSortOrder(), groupOrder, (o) -> {
					cviewer.setSortColumn(index, o.isAccending(so) ? -1 : 1);
				}).force();
			} else {
				new UnsortAction(getFilter().getSortOrder(), groupOrder, (o) -> {
					cviewer.setSortColumn(-1, 0);
				}).force();
			}
		}
	}

	public void runPaste() {
		MagicCardTransfer mt = MagicCardTransfer.getInstance();
		Object contents = mt.fromClipboard();
		if (contents instanceof Collection) {
			DM.copyCards(DM.resolve((Collection) contents), getFilteredStore().getCardStore());
		} else {
			Control fc = getControl().getDisplay().getFocusControl();
			CopySupport.runPaste(fc);
		}
	}

	public void mcpEventHandler(final CardEvent event) {
		Object data = event.getFirstDataElement();
		int type = event.getType();
		if (data instanceof MagicCard) {
			switch (type) {
			case CardEvent.UPDATE:
				WaitUtils.asyncExec(() -> viewer.refresh());
				break;
			case CardEvent.ADD:
				break;
			case CardEvent.REMOVE:
				break;
			default:
				break;
			}
		} else {
			switch (type) {
			case CardEvent.UPDATE:
				WaitUtils.asyncExec(() -> viewer.refresh());
				break;
			case CardEvent.ADD:
				setNextSelection(new StructuredSelection(data));
				loadData(null);
				break;
			case CardEvent.REMOVE:
				loadData(null);
				break;
			default:
				break;
			}
		}
	}

	public void mcEventHandler(final CardEvent event) {
		Object data = event.getFirstDataElement();
		int type = event.getType();
		if (data instanceof MagicCard) {
			switch (type) {
			case CardEvent.UPDATE:
				WaitUtils.asyncExec(() -> viewer.refresh());
				break;
			case CardEvent.ADD:
				setNextSelection(new StructuredSelection(data));
				loadData(null);
				break;
			case CardEvent.REMOVE:
				loadData(null);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		saveColumnLayout();
	}

	public void saveColumnLayout() {
		if (!(viewer instanceof IMagicColumnViewer))
			return;
		IMagicColumnViewer cviewer = (IMagicColumnViewer) viewer;
		final String value = cviewer.getColumnLayoutProperty();
		if (value == null || value.isEmpty())
			return;
		IPersistentPreferenceStore store = getColumnsPreferenceStore();
		if (value.equals(store.getString(PreferenceConstants.LOCAL_COLUMNS)))
			return;
		synchronized (this) {
			store.removePropertyChangeListener(this.preferenceListener);
			// System.err.println("saving layout " + this.getClass() + " " +
			// getName() + " " + value);
			try {
				store.setValue(PreferenceConstants.LOCAL_COLUMNS, value);
			} finally {
				store.addPropertyChangeListener(this.preferenceListener);
			}
			try {
				store.save();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private Object jobFamility = new Object();
	private Job loadingJob;

	public void loadData(final Runnable postLoad) {
		synchronized (jobFamility) {
			if (loadingJob != null) {
				loadingJob.cancel();
			}
			loadingJob = WaitUtils.scheduleJob("Loading cards for " + AbstractMagicCardsListControl.this,
					(monitor) -> loadDataInJob(postLoad, monitor));
		}
	}

	private void checkInit() {
		try {
			WaitUtils.waitForDb();
		} catch (MagicException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void populateStore(IProgressMonitor monitor) {
		getFilteredStore();
	}

	protected abstract IFilteredCardStore<ICard> doGetFilteredStore();

	public IStatus loadDataInJob(final Runnable postLoad, IProgressMonitor monitor) {
		final Display display = Display.getDefault();
		try {
			monitor.beginTask("Loading for " + getName(), 100);
			checkInit();
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			synchronized (AbstractMagicCardsListControl.this) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				populateStore(monitor);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (getFilteredStore() == null)
					return Status.OK_STATUS;
				monitor.worked(10);
				Location location = getFilteredStore().getLocation();
				monitor.setTaskName("Loading cards for " + location);
				getFilteredStore().update();
			}
			// refresh ui
			if (postLoad != null)
				display.asyncExec(postLoad);
			else
				display.asyncExec(() -> refreshViewer());
		} catch (final Exception e) {
			// display.asyncExec(() ->
			// MessageDialog.openError(display.getActiveShell(), "Error",
			// e.getMessage()));
			MagicUIActivator.log(e);
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected int getCount(Object element) {
		if (element == null)
			return 0;
		int count = ((element instanceof ICardCountable) ? ((ICardCountable) element).getCount() : 1);
		return count;
	}

	@Override
	public void createPageContents(Composite parent) {
		createMainControl(parent);
		loadInitial(); // XXX reloadData()?
	}

	@Override
	public void activate() {
		contributeToActionBars();
		addListeners();
		// getViewSite().setSelectionProvider(getSelectionProvider());// XXX
		refresh();
	}

	@Override
	public void deactivate() {
		removeListeners();
		super.deactivate();
	}

	public Shell getShell() {
		return getControl().getShell();
	}
}
