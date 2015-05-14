package com.reflexit.magiccards.ui.views;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
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
import com.reflexit.magiccards.ui.dialogs.BrowserOpenAcknoledgementDialog;
import com.reflexit.magiccards.ui.dialogs.BuyCardsConfirmationDialog;
import com.reflexit.magiccards.ui.dialogs.LoadExtrasDialog;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.jobs.LoadingExtraJob;
import com.reflexit.magiccards.ui.jobs.LoadingPricesJob;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.instances.InstancesView;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public abstract class AbstractCardsView extends ViewPart {
	protected Action loadExtras;
	protected IMagicControl control;
	private Composite partControl;
	private Action actionRefresh;
	protected Action actionCopy;
	protected Action actionPaste;
	protected Action showInstances;
	protected Action buyCards;

	/**
	 * The constructor.
	 */
	public AbstractCardsView() {
		control = doGetViewControl();
	}

	protected abstract AbstractMagicCardsListControl doGetViewControl();

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		partControl = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		partControl.setLayout(gl);
		createMainControl(partControl);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		getSite().setSelectionProvider(getSelectionProvider());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, getHelpId());
	}

	public abstract String getHelpId();

	protected void createMainControl(Composite parent) {
		control.createPartControl(parent);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		control.init(site);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				AbstractCardsView.this.fillContextMenu(manager);
			}
		});
		control.hookContextMenu(menuMgr);
		getSite().registerContextMenu(menuMgr, getSelectionProvider());
	}

	protected ISelectionProvider getSelectionProvider() {
		return control.getSelectionProvider();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		setGlobalHandlers(bars);
		bars.updateActionBars();
	}

	/**
	 * @param bars
	 */
	protected void setGlobalHandlers(IActionBars bars) {
		activateActionHandler(actionCopy, "org.eclipse.ui.edit.copy");
		activateActionHandler(actionPaste, "org.eclipse.ui.edit.paste");
		setGlobalControlHandlers(bars);
	}

	protected void setGlobalControlHandlers(IActionBars bars) {
		control.setGlobalControlHandlers(bars);
	}

	private HashMap<String, IHandlerActivation> activations = new HashMap<String, IHandlerActivation>();

	public IHandlerActivation activateActionHandler(Action action, String actionId) {
		IHandlerActivation activation = activations.get(actionId);
		if (activation != null) {
			deactivateActionHandler(activation);
		}
		action.setActionDefinitionId(actionId);
		ActionHandler handler = new ActionHandler(action);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		activation = service.activateHandler(actionId, handler);
		// System.err.println("activating " + activation.getCommandId());
		activations.put(actionId, activation);
		return activation;
	}

	public void deactivateActionHandler(IHandlerActivation activation) {
		// stem.err.println("deactivating " + activation.getCommandId());
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.deactivateHandler(activation);
		activations.remove(activation);
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		control.fillLocalPullDown(manager);
		manager.add(this.loadExtras);
		manager.add(this.actionRefresh);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillContextMenu(IMenuManager manager) {
		control.fillContextMenu(manager);
		manager.add(this.loadExtras);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		control.fillLocalToolBar(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void makeActions() {
		// this.groupMenu.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		this.actionCopy = new Action("Copy") {
			@Override
			public void run() {
				try {
					runCopy();
				} catch (Exception e) {
					MessageDialog.openError(getShell(), "Error", e.getMessage());
				}
			}
		};
		this.actionPaste = new Action("Paste") {
			@Override
			public void run() {
				try {
					runPaste();
				} catch (Exception e) {
					MessageDialog.openError(getShell(), "Error", e.getMessage());
				}
			}
		};
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
		this.actionRefresh = new Action("Refresh") {
			@Override
			public void run() {
				// this should force refresh update
				getFilteredStore().getCardStore().updateList(null, null);
				reloadData();
			}
		};
		this.actionRefresh
				.setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
		showInstances = new Action("Show All Instances") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/hand16.png"));
			}

			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						runShowInstances(page);
					}
				}
			}
		};
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
						boolean ok = MessageDialog
								.openConfirm(
										getShell(),
										"Note",
										"Cards are copied to clipboard, use Paste command to add cards into mass entry input form when Browser comes up.\nPress OK to open a Browser.");
						if (!ok)
							return;
					}
					MagicLogger.log("Redirecting to " + url);
					new BrowserOpenAcknoledgementDialog(getShell(),
							"Browser is being open, continue with the browser to complete your order", url)
							.open();
				} else {
					if (!MessageDialog
							.openConfirm(
									getShell(),
									"Error",
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

	protected void saveColumnLayout() {
		String id = getPreferencePageId();
		if (id != null && control instanceof MagicControl) {
			((MagicControl) control).saveColumnLayout();
		}
	}

	public void refreshView() {
		WaitUtils.syncExec(() -> control.refresh());
	}

	protected void runLoadExtras() {
		final IStructuredSelection selection = getSelection();
		IFilteredCardStore filteredStore = getFilteredStore();
		final LoadExtrasDialog dialog = new LoadExtrasDialog(getShell(), selection.size(),
				filteredStore.getUniqueCount(),
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

	/**
	 *
	 */
	protected void runCopy() {
		control.runCopy();
	}

	protected void runPaste() {
		control.runPaste();
	}

	protected abstract String getPreferencePageId();

	@Override
	public void dispose() {
		control.dispose();
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		getControl().setFocus();
	}

	protected Control getControl() {
		return control.getControl();
	}

	protected void runDoubleClick() {
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) control).getFilteredStore();
	}

	/**
	 * Update view in UI thread after data load is finished
	 */
	protected void updateViewer() {
		control.updateViewer();
	}

	/**
	 * @return
	 */
	public PrefixedPreferenceStore getLocalPreferenceStore() {
		if (control instanceof IMagicCardListControl)
			return ((IMagicCardListControl) control).getLocalPreferenceStore();
		return null;
	}

	public PrefixedPreferenceStore getFilterPreferenceStore() {
		if (control instanceof IMagicCardListControl)
			return ((IMagicCardListControl) control).getFilterPreferenceStore();
		return getLocalPreferenceStore();
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
				ICardStore activeHandler = DataManager.getInstance().getCardHandler()
						.getActiveStore();
				if (activeHandler != null && activeHandler == cardCollection.getStore()) {
					active = " (Active)";
				}
				String name = (cardCollection.isDeck() ? "Deck - " : "Collection - ")
						+ cardCollection.getName()
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

	public void reloadData() {
		control.reloadData();
	}

	public abstract String getId();

	public MagicCardFilter getFilter() {
		return getFilteredStore().getFilter();
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		saveColumnLayout();
	}

	protected void runShowInstances(IWorkbenchPage page) {
		try {
			InstancesView view = (InstancesView) page.showView(InstancesView.ID);
			view.selectionChanged(AbstractCardsView.this, getSelection());
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}
}