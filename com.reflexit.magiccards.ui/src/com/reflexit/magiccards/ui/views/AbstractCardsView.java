package com.reflexit.magiccards.ui.views;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.dialogs.LoadExtrasDialog;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.jobs.LoadingExtraJob;
import com.reflexit.magiccards.ui.jobs.LoadingPricesJob;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.utils.TextConvertor;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.lib.DeckView;
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
	protected Action loadExtras;
	protected ViewerManager manager;
	private Label statusLine;
	protected MenuManager sortMenu;
	protected MenuManager groupMenu;
	private IPreferenceStore store;
	private SearchControl searchControl;
	protected Runnable updateViewerRunnable;
	protected ISelection revealSelection;

	/**
	 * The constructor.
	 */
	public AbstractCardsView() {
		updateViewerRunnable = new Runnable() {
			public void run() {
				updateViewer();
			}
		};
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
	private Action groupMenuButton;

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
		getPreferenceStore().setDefault(FilterHelper.GROUP_FIELD, "");
		String field = getPreferenceStore().getString(FilterHelper.GROUP_FIELD);
		this.manager.updateGroupBy(MagicCardFieldPhysical.fieldByName(field));
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
		reloadData();
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
		this.searchControl.setSearchAsYouType(true);
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
		// this.showFind.setActionDefinitionId(FIND);
		ActionHandler findHandler = new ActionHandler(this.showFind);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler(FIND, findHandler);
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.showFilter);
		manager.add(this.sortMenu);
		manager.add(this.groupMenu);
		manager.add(this.showPrefs);
		manager.add(loadExtras);
		manager.add(new Separator());
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.showFilter);
		manager.add(this.showPrefs);
		// manager.add(loadPrices);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.groupMenuButton);
		manager.add(this.showPrefs);
		manager.add(this.showFind);
		manager.add(this.showFilter);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}

	class GroupAction extends Action {
		ICardField field;

		GroupAction(String name, ICardField field) {
			super(name, Action.AS_RADIO_BUTTON);
			this.field = field;
			String val = getPreferenceStore().getString(FilterHelper.GROUP_FIELD);
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

	protected void makeActions() {
		this.showFilter = new Action() {
			@Override
			public void run() {
				runShowFilter();
			}
		};
		this.showFilter.setText("Filter...");
		this.showFilter.setToolTipText("Opens a Card Filter Dialog");
		this.showFilter.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/filter.gif"));
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
			final AbstractColumn man = (AbstractColumn) iterator.next();
			String name = man.getColumnFullName();
			final int index = i;
			Action ac = new Action(name) {
				@Override
				public void run() {
					manager.updateSortColumn(index);
					reloadData();
				}
			};
			this.sortMenu.add(ac);
		}
		this.groupMenuButton = new Action("Group By", Action.AS_DROP_DOWN_MENU) {
			@Override
			public void run() {
				String group = getPreferenceStore().getString(FilterHelper.GROUP_FIELD);
				if (group == null || group.length() == 0)
					actionGroupBy(MagicCardField.CMC);
				else
					actionGroupBy(null);
			}

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
		};
		this.groupMenuButton.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.groupMenu = createGroupMenu();
		// this.groupMenu.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.showPrefs = new Action("Preferences...") {
			@Override
			public void run() {
				String id = getPreferencePageId();
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null);
				dialog.open();
			}
		};
		this.showPrefs.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/table.gif"));
		this.showFind = new Action("Find...") {
			@Override
			public void run() {
				AbstractCardsView.this.searchControl.setVisible(true);
				AbstractCardsView.this.showFind.setEnabled(false);
			}
		};
		this.showFind.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/search.gif"));
		this.copyText = new Action("Copy") {
			@Override
			public void run() {
				runCopy();
			}
		};
		this.loadExtras = new Action("Load Extra Fields...") {
			@Override
			public void run() {
				runLoadExtras();
			}
		};
	}

	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By");
		groupMenu.add(new GroupAction("None", null));
		groupMenu.add(new GroupAction("Color", MagicCardField.COST));
		groupMenu.add(new GroupAction("Cost", MagicCardField.CMC));
		groupMenu.add(new GroupAction("Type", MagicCardField.TYPE));
		groupMenu.add(new GroupAction("Set", MagicCardField.SET));
		groupMenu.add(new GroupAction("Rarity", MagicCardField.RARITY));
		groupMenu.add(new GroupAction("Location", MagicCardFieldPhysical.LOCATION));
		return groupMenu;
	}

	protected void runLoadExtras() {
		final IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		final LoadExtrasDialog dialog = new LoadExtrasDialog(getShell(), selection.size(), getFilteredStore().getSize(), getFilteredStore()
				.getCardStore().size());
		if (dialog.open() != Window.OK || dialog.getFields().isEmpty()) {
			return;
		}
		if (dialog.getFields().contains(MagicCardField.DBPRICE)) {
			dialog.getFields().remove(MagicCardField.DBPRICE);
			LoadingPricesJob loadingPrices = new LoadingPricesJob(this);
			loadingPrices.setSelection(selection);
			loadingPrices.setListChoice(dialog.getListChoice());
			loadingPrices.schedule();
		}
		LoadingExtraJob loadingExtras = new LoadingExtraJob(this);
		loadingExtras.setFields(dialog.getFields());
		loadingExtras.setSelection(selection);
		loadingExtras.setListChoice(dialog.getListChoice());
		loadingExtras.schedule();
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
	 * @param indexCost
	 */
	protected void actionGroupBy(ICardField field) {
		getPreferenceStore().setValue(FilterHelper.GROUP_FIELD, field == null ? "" : field.toString());
		if (field != null)
			this.manager.filter.setSortField(field);
		this.manager.filter.setAscending(false);
		this.manager.updateGroupBy(field);
		reloadData();
	}

	protected abstract String getPreferencePageId();

	private void hookDoubleClickAction() {
		this.manager.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				AbstractCardsView.this.doubleClickAction.run();
			}
		});
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
		Dialog cardFilterDialog = new CardFilterDialog(getShell(), getPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData();
	}

	public void reloadData() {
		this.manager.loadData(updateViewerRunnable);
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
		refresh();
	}

	protected void refresh() {
		reloadData();
	}

	public IFilteredCardStore getFilteredStore() {
		return this.manager.getFilteredStore();
	}

	/**
	 * Update view in UI thread after data load is finished
	 */
	protected void updateViewer() {
		if (manager.getControl().isDisposed())
			return;
		ISelection selection;
		try {
			selection = manager.getSelectionProvider().getSelection();
		} catch (Exception e) {
			selection = new StructuredSelection();
		}
		manager.updateViewer();
		updateStatus();
		if (revealSelection != null) {
			// set desired selection
			manager.getSelectionProvider().setSelection(revealSelection);
			revealSelection = null;
		} else {
			// restore selection
			manager.getSelectionProvider().setSelection(selection);
		}
	}

	protected void updateStatus() {
		setStatus(manager.getStatusMessage());
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
			this.store = new PrefixedPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore(), getPreferencePageId());
		return this.store;
	}

	public static interface IDeckAction {
		public void run(String id);
	};

	/**
	 * @param manager
	 */
	protected void fillDeckMenu(IMenuManager manager, final IDeckAction deckAction) {
		boolean any = false;
		IViewReference[] views = getViewSite().getWorkbenchWindow().getActivePage().getViewReferences();
		for (final IViewReference viewReference : views) {
			if (viewReference.getId().equals(DeckView.ID)) {
				final String deckId = viewReference.getSecondaryId();
				DeckView deckView = (DeckView) viewReference.getPart(false);
				if (deckView == null)
					continue;
				CardCollection cardCollection = deckView.getCardCollection();
				String active = "";
				if (DataManager.getCardHandler().getActiveDeckHandler().getCardStore() == cardCollection.getStore()) {
					active = " (Active)";
				}
				String name = (cardCollection.isDeck() ? "Deck - " : "Collection - ") + cardCollection.getName() + active;
				Action ac = new Action(name) {
					@Override
					public void run() {
						deckAction.run(deckId);
					}
				};
				if (deckView == this)
					ac.setEnabled(false);
				manager.add(ac);
				any = true;
			}
		}
		if (!any) {
			Action ac = new Action("No Open Decks") {
			};
			manager.add(ac);
			ac.setEnabled(false);
		}
	}
}