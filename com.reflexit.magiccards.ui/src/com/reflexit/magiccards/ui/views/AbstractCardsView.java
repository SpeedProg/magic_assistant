package com.reflexit.magiccards.ui.views;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.actions.MagicCopyAction;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.dialogs.BrowserOpenAcknoledgementDialog;
import com.reflexit.magiccards.ui.dialogs.BuyCardsConfirmationDialog;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.dialogs.LoadExtrasDialog;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.jobs.LoadingExtraJob;
import com.reflexit.magiccards.ui.jobs.LoadingPricesJob;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public abstract class AbstractCardsView extends ViewPart implements IShowInTarget, IShowInSource {
	private Composite partControl;
	protected Action loadExtras;
	protected Action actionRefresh;
	protected Action actionCopy;
	protected Action buyCards;
	private HashMap<String, IHandlerActivation> activations = new HashMap<>();

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		partControl = new Composite(parent, SWT.NONE);
		partControl.setLayout(GridLayoutFactory.fillDefaults().create());
		createMainControl(partControl);
		makeActions();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, getHelpId());
		activate();
	}

	protected Composite getPartControl() {
		return partControl;
	}

	protected void activate() {
		clearActionBars();
		contributeToActionBars();
		registerSelectionProvider();
	}

	protected void registerSelectionProvider() {
		getSite().setSelectionProvider(getSelectionProvider());
	}

	public abstract String getHelpId();

	protected abstract void createMainControl(Composite parent);

	protected void hookContextMenu() {
		// registerContextMenu(hookContextMenu(createContextMenuManager()));
	}

	protected void close() {
		try {
			getViewSite().getPage().hideView(this);
		} catch (Exception e) {
			// ignore
		}
	}

	protected MenuManager createContextMenuManager() {
		MenuManager menuMgr = new MenuManager("#PopupMenu-View");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				AbstractCardsView.this.fillContextMenu(manager);
			}
		});
		return menuMgr;
	}

	protected void registerContextMenu(MenuManager menuMgr) {
		if (menuMgr != null)
			getSite().registerContextMenu(menuMgr, getSelectionProvider());
	}

	protected abstract ISelectionProvider getSelectionProvider();

	protected void clearActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager viewMenuManager = bars.getMenuManager();
		viewMenuManager.removeAll();
		viewMenuManager.updateAll(true);
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.removeAll();
		toolBarManager.update(true);
		// bars.updateActionBars();
	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		bars.getMenuManager().updateAll(true);
		fillLocalToolBar(bars.getToolBarManager());
		bars.getToolBarManager().update(true);
		setGlobalHandlers(bars);
		bars.updateActionBars();
		hookContextMenu();
	}

	protected void setGlobalHandlers(IActionBars bars) {
		bars.setGlobalActionHandler(actionCopy.getId(), actionCopy);
		bars.setGlobalActionHandler(actionRefresh.getId(), actionRefresh);
	}

	public IHandlerActivation activateActionHandler(Action action, String actionId) {
		IHandlerActivation activation = activations.get(actionId);
		if (activation != null) {
			deactivateActionHandler(actionId, activation);
		}
		action.setActionDefinitionId(actionId);
		ActionHandler handler = new ActionHandler(action);
		IHandlerService service = (getSite()).getService(IHandlerService.class);
		activation = service.activateHandler(actionId, handler);
		// System.err.println("activating " + activation.getCommandId());
		activations.put(actionId, activation);
		return activation;
	}

	public void deactivateActionHandler(String actionId, IHandlerActivation activation) {
		// stem.err.println("deactivating " + activation.getCommandId());
		IHandlerService service = (getSite()).getService(IHandlerService.class);
		if (service != null)
			service.deactivateHandler(activation);
		activations.remove(actionId);
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.loadExtras);
		manager.add(this.actionRefresh);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.loadExtras);
		fillShowInMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillShowInMenu(IMenuManager manager) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IContributionItem showViewItem = ContributionItemFactory.VIEWS_SHOW_IN.create(window);
		ImageDescriptor eyeImage = MagicUIActivator.getImageDescriptor("icons/clcl16/eye.png");
		IMenuManager showInMenu = new MenuManager("Show In", eyeImage, null);
		showInMenu.add(showViewItem);
		manager.add(showInMenu);
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.remove(actionRefresh.getId());
		manager.add(this.actionRefresh);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void makeActions() {
		// this.groupMenu.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.actionCopy = new MagicCopyAction(getSelectionProvider());
		this.loadExtras = new Action("Load Extra Fields...") {
			@Override
			public void run() {
				runLoadExtras();
			}
		};
		this.buyCards = new Action("Buy cards...") {
			@Override
			public void run() {
				runBuyCards();
			}
		};
		this.actionRefresh = new RefreshAction(() -> {
			// this should force refresh update
			getFilteredStore().getCardStore().updateList(null, null);
			reloadData();
		});
	}

	protected void runBuyCards() {
		final IStructuredSelection selection = getSelection();
		final BuyCardsConfirmationDialog dialog = new BuyCardsConfirmationDialog(getShell(), selection,
				getFilteredStore());
		if (dialog.open() == Window.OK) {
			try {
				IPriceProvider provider = DataManager.getDBPriceStore().getProvider();
				System.setProperty("clipboard", "");
				URL url = provider.buy(dialog.getListAsIterable());
				if (url != null) {
					String text = System.getProperty("clipboard");
					if (text != null && !text.isEmpty()) {
						CopySupport.runCopy(text);
						boolean ok = MessageDialog.openConfirm(getShell(), "Note",
								"Cards are copied to clipboard, use Paste command to add cards into mass entry input form when Browser comes up.\nPress OK to open a Browser.");
						if (!ok)
							return;
					}
					MagicLogger.log("Redirecting to " + url);
					new BrowserOpenAcknoledgementDialog(getShell(),
							"Browser is being open, continue with the browser to complete your order", url).open();
				} else {
					if (!MessageDialog.openConfirm(getShell(), "Error",
							"This provider does not support direct cart population.\nPress OK to open a Browser on the main page and enter cards manually"))
						return;
					new BrowserOpenAcknoledgementDialog(getShell(),
							"Browser is being open, continue with the browser to complete your order",
							provider.getURL()).open();
				}
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "Error", e.getLocalizedMessage());
			}
		}
	}

	public void refreshView() {
		// override if needed
	}

	protected void runLoadExtras() {
		final IStructuredSelection selection = getSelection();
		IFilteredCardStore filteredStore = getFilteredStore();
		final LoadExtrasDialog dialog = new LoadExtrasDialog(getShell(), selection.size(), filteredStore.getFlatSize(),
				filteredStore.getCardStore().size());
		if (dialog.open() != Window.OK || dialog.getFields().isEmpty()) {
			return;
		}
		Iterable list = null;
		switch (dialog.getListChoice()) {
		case LoadExtrasDialog.USE_SELECTION:
			list = selection.toList();
			break;
		case LoadExtrasDialog.USE_FILTER:
			list = filteredStore;
			break;
		case LoadExtrasDialog.USE_ALL:
			list = filteredStore.getCardStore();
			break;
		}
		if (dialog.getFields().contains(MagicCardField.DBPRICE)) {
			dialog.getFields().remove(MagicCardField.DBPRICE);
			LoadingPricesJob loadingPrices = new LoadingPricesJob(list);
			loadingPrices.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					refreshView();
				}
			});
			loadingPrices.schedule();
		}
		if (dialog.getFields().size() > 0) {
			LoadingExtraJob loadingExtras = new LoadingExtraJob(this);
			loadingExtras.setFields(dialog.getFields());
			loadingExtras.setSelection(selection);
			loadingExtras.setListChoice(dialog.getListChoice());
			if (dialog.getFields().contains(MagicCardField.LANG)) {
				loadingExtras.setLanguage(dialog.getLanguage());
			}
			loadingExtras.schedule();
		}
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) getSelectionProvider().getSelection();
	}

	protected abstract String getPreferencePageId();

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		getControl().setFocus();
	}

	protected Control getControl() {
		return partControl;
	}

	protected void runDoubleClick() {
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	public static interface IDeckAction {
		public void run(String id);
	}

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
				ICardStore activeHandler = DataManager.getInstance().getCardHandler().getActiveStore();
				if (activeHandler != null && activeHandler == cardCollection.getStore()) {
					active = " (Active)";
				}
				String name = (cardCollection.isDeck() ? "Deck - " : "Collection - ") + cardCollection.getName()
						+ active;
				Action ac = new Action(name) {
					@Override
					public void run() {
						try {
							deckAction.run(deckId);
						} catch (Exception e) {
							MessageDialog.openError(getShell(), "Error", e.getMessage());
						}
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

	public abstract void reloadData();

	public abstract String getId();

	public abstract IFilteredCardStore getFilteredStore();

	public MagicCardFilter getFilter() {
		return getFilteredStore().getFilter();
	}

	@Override
	public boolean show(ShowInContext context) {
		getSelectionProvider().setSelection(context.getSelection());
		return true;
	}

	@Override
	public ShowInContext getShowInContext() {
		return new ShowInContext(null, getSelection());
	}

	public abstract IPreferenceStore getFilterPreferenceStore();

	public CardFilterDialog getCardFilterDialog() {
		return new CardFilterDialog(getShell(), getFilterPreferenceStore());
	}
}