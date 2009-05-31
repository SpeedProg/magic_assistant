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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.nav.DecksContainer;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.PerspectiveFactoryMagic;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.dnd.MagicNavDropAdapter;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.exportWizards.ImportAction;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.MyCardsView;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

public class CardsNavigatorView extends ViewPart implements ICardEventListener {
	public static final String ID = CardsNavigatorView.class.getName();
	private Action doubleClickAction;
	private CardsNavigatiorManager manager;
	private Action delete;
	private Action export;
	private Action importa;

	/**
	 * The constructor.
	 */
	public CardsNavigatorView() {
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
		createTable(composite);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		addDragAndDrop();
	}

	private void addDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDropSupport(ops, transfers, new MagicNavDropAdapter(getViewer()));
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
	}

	protected void setGlobalHandlers() {
		ActionHandler deleteHandler = new ActionHandler(this.delete);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", deleteHandler);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		manager.add(new Separator());
		manager.add(export);
		manager.add(importa);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		this.delete.setEnabled(canRemove());
		manager.add(new Separator());
		manager.add(export);
		manager.add(importa);
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @return
	 */
	private boolean canRemove() {
		IStructuredSelection sel = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
		if (sel.isEmpty())
			return false;
		for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			if (el.getParent() == DataManager.getModelRoot())
				return false;
			if (el instanceof CardOrganizer && ((CardOrganizer) el).hasChildren())
				return false;
		}
		return true;
	}

	private void fillLocalToolBar(IToolBarManager manager) {
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
		this.delete = new Action("Delete") {
			@Override
			public void run() {
				actionDelete();
			}
		};
		this.export = new ExportAction();
		this.importa = new ImportAction();
		getViewer().addSelectionChangedListener((ISelectionChangedListener) this.export);
		getViewer().addSelectionChangedListener((ISelectionChangedListener) this.importa);
	}

	/**
	 * 
	 */
	protected void actionDelete() {
		IStructuredSelection sel = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
		if (sel.isEmpty())
			return;
		if (sel.size() == 1) {
			CardElement el = (CardElement) sel.getFirstElement();
			if (MessageDialog.openQuestion(getShell(), "Removal Confirmation", "Are you sure you want to delete "
			        + el.getName() + "?")) {
				el.remove();
			}
		} else {
			if (MessageDialog.openQuestion(getShell(), "Removal Confirmation", "Are you sure you want to delete these "
			        + sel.size() + " elements?")) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					CardElement el = (CardElement) iterator.next();
					el.remove();
				}
			}
		}
	}

	protected void addNewDeck() {
		// Grab the selection out of the tree and convert it to a
		// StructuredSelection for use by the wizard.
		StructuredSelection currentSelection = (StructuredSelection) this.manager.getViewer().getSelection();
		// get the wizard from the child class.
		IWorkbenchWizard wizard = new NewDeckWizard();
		// Get the workbench and initialize, the wizard.
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, currentSelection);
		// Open the wizard dialog with the given wizard.
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}

	public static void createNewDeckAction(DecksContainer parent, String name, IWorkbenchPage page) {
		String filename = name + ".xml";
		Deck d = parent.addDeck(filename);
		try {
			openDeckView(d, page);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private DecksContainer getDeckContainer() {
		ISelection selection = this.manager.getViewer().getSelection();
		DecksContainer parent = DataManager.getModelRoot().getDeckContainer();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			if (!iss.isEmpty()) {
				CardElement el = (CardElement) iss.getFirstElement();
				if (el instanceof DecksContainer) {
					parent = (DecksContainer) el;
				} else if (el instanceof Deck) {
					parent = (DecksContainer) ((Deck) el).getParent();
				}
			}
		}
		return parent;
	}

	private void hookDoubleClickAction() {
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				CardsNavigatorView.this.doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(getViewSite().getShell(), "Magic Cards", message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		DataManager.getModelRoot().addListener(this);
	}

	@Override
	public void dispose() {
		DataManager.getModelRoot().removeListener(this);
		this.manager.dispose();
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
		if (obj instanceof MagicDbContainter) {
			try {
				getViewSite().getWorkbenchWindow().getActivePage().showView(MagicDbView.ID);
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else if (obj instanceof Deck) {
			try {
				Deck d = (Deck) obj;
				openDeckView(d, getViewSite().getWorkbenchWindow().getActivePage());
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else if (obj instanceof CardCollection) {
			try {
				MyCardsView view = (MyCardsView) getViewSite().getWorkbenchWindow().getActivePage().showView(
				        MyCardsView.ID);
				view.setLocationFilter(((CardCollection) obj).getLocation());
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else if (obj instanceof CardOrganizer) {
			try {
				MyCardsView view = (MyCardsView) getViewSite().getWorkbenchWindow().getActivePage().showView(
				        MyCardsView.ID);
				view.setLocationFilter(((CardOrganizer) obj).getLocation());
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else {
			showMessage("Cannot open this object " + obj.toString());
		}
	}

	public static void openDeckView(Deck d, IWorkbenchPage page) throws PartInitException {
		page.showView(DeckView.ID, d.getFileName(), IWorkbenchPage.VIEW_ACTIVATE);
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	public void handleEvent(final CardEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				CardsNavigatorView.this.manager.getViewer().refresh(true);
			}
		});
	}
}