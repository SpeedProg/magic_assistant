/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.views.nav;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.PerspectiveFactoryMagic;
import com.reflexit.magiccards.ui.commands.DeleteHandler;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.exportWizards.ImportAction;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.MyCardsView;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

public class CardsNavigatorView extends ViewPart implements ICardEventListener, IPropertyChangeListener, IShowInTarget {
	public static final String ID = CardsNavigatorView.class.getName();
	private Action doubleClickAction;
	private CardsNavigatiorManager manager;
	private Action export;
	private Action importa;
	private Action newDeckWizard;
	private Action openInDeckView;
	private Action openInMyCardsView;
	private Action showSideboards;
	private Action refresh;
	private Clipboard clipboard;
	private Composite top;
	private ModelRoot modelRoot;
	private ICardEventListener modelListener = this;

	/**
	 * The constructor.
	 */
	public CardsNavigatorView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		top.setLayout(gl);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, MagicUIActivator.helpId("viewcardnav"));
		createTable(top);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		addDragAndDrop();
	}

	private void addDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance(), MagicDeckTransfer.getInstance() };
		Transfer[] transfers2 = new Transfer[] { MagicDeckTransfer.getInstance() };
		getViewer().addDropSupport(ops, transfers, new MagicNavDropAdapter(getViewer()));
		getViewer().addDragSupport(DND.DROP_MOVE, transfers2, new MagicNavDragListener(getViewer()));
	}

	private void createTable(Composite parent) {
		this.manager = new CardsNavigatiorManager();
		Control control = this.manager.createContents(parent, SWT.MULTI);
		((Composite) control).setLayoutData(new GridData(GridData.FILL_BOTH));
		// ADD the JFace Viewer as a Selection Provider to the View site.
		getSite().setSelectionProvider(this.manager.getViewer());
	}

	public ColumnViewer getViewer() {
		return this.manager.getViewer();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				CardsNavigatorView.this.fillContextMenu(manager);
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
		setGlobalHandlers();
		clipboard = new Clipboard(getSite().getShell().getDisplay());
	}

	class CutAction extends Action {
		public CutAction() {
			super("Cut");
		}

		@Override
		public void run() {
			IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
			CardElement[] gadgets = (CardElement[]) selection.toList().toArray(new CardElement[selection.size()]);
			clipboard.setContents(new Object[] { gadgets }, new Transfer[] { MagicDeckTransfer.getInstance() });
		}

		@Override
		public boolean isEnabled() {
			return super.isEnabled();
		}
	}

	class PasteAction extends Action {
		public PasteAction() {
			super("Paste");
		}

		@Override
		public void run() {
			IStructuredSelection sel = (IStructuredSelection) getViewer().getSelection();
			CardElement parent = (CardElement) sel.getFirstElement();
			CardElement[] toDropArray = (CardElement[]) clipboard.getContents(MagicDeckTransfer.getInstance());
			if (toDropArray == null)
				return;
			try {
				if (!(parent instanceof CardOrganizer))
					throw new MagicException("Has to be folder to move to");
				getModelRoot().move(toDropArray, (CardOrganizer) parent);
			} catch (MagicException e) {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error",
						"Cannot perform this operation: " + e.getMessage());
			}
		}

		@Override
		public boolean isEnabled() {
			IStructuredSelection sel = (IStructuredSelection) getViewer().getSelection();
			CardElement parent = (CardElement) sel.getFirstElement();
			if (!(parent instanceof CardOrganizer))
				return false;
			CardElement[] toDropArray = (CardElement[]) clipboard.getContents(MagicDeckTransfer.getInstance());
			if (toDropArray == null || toDropArray.length == 0)
				return false;
			return true;
		}
	}

	class DeleteAction extends Action {
		public DeleteAction() {
			super("Delete");
		}

		@Override
		public void run() {
			IStructuredSelection sel = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
			DeleteHandler.remove(sel);
		}

		@Override
		public boolean isEnabled() {
			IStructuredSelection sel = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
			if (sel.isEmpty())
				return false;
			for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				if (el.getParent() == modelRoot)
					return false;
			}
			return true;
		}
	}

	protected void setGlobalHandlers() {
		IHandlerService service = (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", new ActionHandler(new DeleteAction()));
		service.activateHandler("org.eclipse.ui.edit.cut", new ActionHandler(new CutAction()));
		service.activateHandler("org.eclipse.ui.edit.paste", new ActionHandler(new PasteAction()));
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		manager.add(new Separator());
		manager.add(export);
		manager.add(importa);
		manager.add(new Separator());
		manager.add(showSideboards);
		manager.add(refresh);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		manager.add(new Separator());
		manager.add(export);
		manager.add(importa);
		manager.add(new Separator());
		manager.add(showSideboards);
		manager.add(openInDeckView);
		openInDeckView.setEnabled(openInDeckView.isEnabled());
		manager.add(openInMyCardsView);
		openInMyCardsView.setEnabled(openInMyCardsView.isEnabled());
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(newDeckWizard);
		manager.add(export);
		manager.add(importa);
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		// double cick
		this.doubleClickAction = new Action() {
			@Override
			public void run() {
				runDoubleClick();
			}
		};
		this.export = new ExportAction();
		this.importa = new ImportAction();
		this.newDeckWizard = new Action("New Deck") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/ideck16.png"));
			}

			@Override
			public void run() {
				// Instantiates and initializes the wizard
				NewDeckWizard wizard = new NewDeckWizard();
				wizard.init(getSite().getWorkbenchWindow().getWorkbench(),
						(IStructuredSelection) getViewer().getSelection());
				// Instantiates the wizard container with the wizard and opens
				// it
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.create();
				dialog.open();
			}
		};
		this.refresh = new Action("Refresh", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
			}

			@Override
			public void run() {
				// reset listeners just in case model root changed
				modelRoot.removeListener(modelListener);
				modelRoot = getModelRoot();
				modelRoot.addListener(modelListener);
				// refresh model and view
				modelRoot.refresh();
				getViewer().refresh(true);
			}
		};
		this.showSideboards = new Action("Show Sideboads", SWT.TOGGLE) {
			@Override
			public void run() {
				showSideboardFilter();
			}
		};
		showSideboardFilter(); // activate filter
		getViewer().addSelectionChangedListener((ISelectionChangedListener) this.export);
		getViewer().addSelectionChangedListener((ISelectionChangedListener) this.importa);
		openInDeckView = new Action("Open (Activate)") {
			@Override
			public void run() {
				if (isEnabled())
					runDoubleClick();
			}

			@Override
			public boolean isEnabled() {
				ISelection selection = getViewer().getSelection();
				if (selection.isEmpty() || ((IStructuredSelection) selection).size() > 1)
					return false;
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof CardCollection) {
					return true;
				}
				return false;
			}
		};
		openInMyCardsView = new Action("Open in My Cards View") {
			@Override
			public void run() {
				if (isEnabled()) {
					ISelection selection = getViewer().getSelection();
					Object obj = ((IStructuredSelection) selection).getFirstElement();
					MyCardsView view;
					try {
						view = (MyCardsView) getViewSite().getWorkbenchWindow().getActivePage()
								.showView(MyCardsView.ID);
						view.setLocationFilter(((CardElement) obj).getLocation());
					} catch (PartInitException e) {
						// error
					}
				}
			}

			@Override
			public boolean isEnabled() {
				ISelection selection = getViewer().getSelection();
				if (selection.isEmpty() || ((IStructuredSelection) selection).size() != 1)
					return false;
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				return (obj instanceof CardElement && !(obj instanceof MagicDbContainter));
			}
		};
	}

	private void hookDoubleClickAction() {
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				CardsNavigatorView.this.doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(getViewSite().getShell(), "Magic Cards", message);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		modelRoot = getModelRoot();
		modelRoot.addListener(modelListener);
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(this);
	}

	public ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
	}

	@Override
	public void dispose() {
		modelRoot.removeListener(modelListener);
		this.manager.dispose();
		clipboard.dispose();
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(this);
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		top.setFocus();
	}

	protected void runDoubleClick() {
		ISelection selection = getViewer().getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof MagicDbContainter) {
			try {
				getViewSite().getWorkbenchWindow().getActivePage().showView(MagicDbView.ID);
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else if (obj instanceof CardCollection) {
			// MyCardsView view = (MyCardsView)
			// getViewSite().getWorkbenchWindow().getActivePage().showView(
			// MyCardsView.ID);
			// view.setLocationFilter(((CardCollection) obj).getLocation());
			CardCollection d = (CardCollection) obj;
			openDeckView(d);
		} else if (obj instanceof CardOrganizer) {
			try {
				MyCardsView view = (MyCardsView) getViewSite().getWorkbenchWindow().getActivePage()
						.showView(MyCardsView.ID);
				view.setLocationFilter(((CardOrganizer) obj).getLocation());
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else {
			showMessage("Cannot open this object " + obj.toString());
		}
	}

	private void openDeckView(CardCollection d) {
		DeckView.openCollection(d, null);
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	@Override
	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		switch (type) {
		case CardEvent.ADD_CONTAINER:
			Object obj = event.getData();
			if (obj instanceof CardCollection) {
				WaitUtils.scheduleJob("Opening deck", () -> {
					CardCollection coll = (CardCollection) obj;
					boolean gotit = WaitUtils.waitForCondition(() -> (coll.getStorageInfo() != null), 3000, 100);
					WaitUtils.asyncExec(() -> manager.getViewer().refresh(true));
					if (gotit)
						DeckView.openCollection(coll, null);
				});
			} else {
				WaitUtils.asyncExec(() -> manager.getViewer().refresh(true));
			}
			break;
		case CardEvent.REMOVE_CONTAINER:
		case CardEvent.RENAME_CONTAINER:
		case CardEvent.UPDATE_CONTAINER:
			WaitUtils.asyncExec(() -> manager.getViewer().refresh(true));
			break;
		default:
			break;
		}
	}

	protected void showSideboardFilter() {
		Map<String, Object> prop = new HashMap<String, Object>();
		boolean state = !showSideboards.isChecked();
		prop.put(CardsNavigatorContentProvider.FILTER_SIDEBOARDS, state);
		getViewer().setFilters(new ViewerFilter[] { CardsNavigatorContentProvider.getFilter(prop) });
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		manager.refresh();
	}

	@Override
	public boolean show(ShowInContext context) {
		ISelection selection = context.getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			selection = new StructuredSelection(context.getInput());
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		Object element = iss.getFirstElement();
		if (element instanceof ILocatable) {
			Location loc = ((ILocatable) element).getLocation();
			if (loc == null || loc == Location.NO_WHERE) {
				return false;
			}
			final ModelRoot container = DataManager.getInstance().getModelRoot();
			CardCollection col = container.findCardCollectionById(loc.getName());
			if (col != null) {
				getViewer().setSelection(new StructuredSelection(col), true);
				DeckView.openCollection(col, iss);
				return true;
			}
		}
		return false;
	}
}