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
package com.reflexit.magiccards.ui.views.printings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.PerspectiveFactoryMagic;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

/**
 * Shows different prints of the same card in different sets and per collection
 * 
 */
public class PrintingsView extends ViewPart implements ISelectionListener {
	public static final String ID = PrintingsView.class.getName();
	private Action doubleClickAction;
	private PrintingsManager manager;
	private Action delete;
	private Action refresh;

	/**
	 * The constructor.
	 */
	public PrintingsView() {
		this.loadCardJob = new LoadPrintingsJob(IMagicCard.DEFAULT);
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
		revealCurrentSelection();
	}

	private void addDragAndDrop() {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
	}

	private void createTable(Composite parent) {
		this.manager = new PrintingsManager();
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
				PrintingsView.this.fillContextMenu(manager);
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
		manager.add(refresh);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		this.delete.setEnabled(canRemove());
		manager.add(new Separator());
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
		this.refresh = new Action("Refresh", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
			}

			@Override
			public void run() {
				try {
					DataManager.getModelRoot().refresh();
				} catch (CoreException e) {
					MagicUIActivator.log(e);
				}
				getViewer().refresh(true);
			}
		};
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
			if (MessageDialog.openQuestion(getShell(), "Removal Confirmation", "Are you sure you want to delete " + el.getName() + "?")) {
				el.remove();
			}
		} else {
			if (MessageDialog.openQuestion(getShell(), "Removal Confirmation", "Are you sure you want to delete these " + sel.size()
					+ " elements?")) {
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

	private void hookDoubleClickAction() {
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				PrintingsView.this.doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(getViewSite().getShell(), "Magic Cards", message);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
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
		//
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	private void revealCurrentSelection() {
		try {
			IWorkbenchPage page = getViewSite().getWorkbenchWindow().getActivePage();
			if (page == null)
				return;
			IViewPart dbview = page.findView(MagicDbView.ID);
			if (dbview != null) {
				ISelection sel = dbview.getSite().getSelectionProvider().getSelection();
				runLoadJob(sel);
			}
		} catch (NullPointerException e) {
			// workbench of active window is null, just ignore then
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part instanceof AbstractCardsView)
			runLoadJob(sel);
	}

	private IMagicCard getCard(ISelection sel) {
		if (sel.isEmpty()) {
			return IMagicCard.DEFAULT;
		}
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) sel;
			Object firstElement = ss.getFirstElement();
			if (firstElement instanceof IMagicCard) {
				IMagicCard card = (IMagicCard) firstElement;
				return card;
			}
		}
		return IMagicCard.DEFAULT;
	}

	private synchronized void runLoadJob(ISelection sel) {
		final IMagicCard card = getCard(sel);
		this.loadCardJob.cancel();
		this.loadCardJob = new LoadPrintingsJob(card);
		this.loadCardJob.schedule();
	}

	private LoadPrintingsJob loadCardJob;

	public class LoadPrintingsJob extends Job {
		private IMagicCard card;

		public LoadPrintingsJob(IMagicCard card) {
			super("Loading card image");
			this.card = card;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (card == IMagicCard.DEFAULT)
					return Status.OK_STATUS;
				setName("Loading card printings: " + card.getName());
				monitor.beginTask("Loading card printings for " + card.getName(), 100);
				ICardStore<IMagicCard> store = DataManager.getCardHandler().getMagicDBStore();
				final Collection<IMagicCard> res = searchInStore(store);
				res.addAll(searchInStore(DataManager.getCardHandler().getLibraryCardStore()));
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						getViewer().setInput(res);
					}
				});
				return Status.OK_STATUS;
			} finally {
				monitor.done();
			}
		}

		public Collection<IMagicCard> searchInStore(ICardStore<IMagicCard> store) {
			ArrayList<IMagicCard> res = new ArrayList<IMagicCard>();
			for (Iterator<IMagicCard> iterator = store.iterator(); iterator.hasNext();) {
				IMagicCard next = iterator.next();
				if (card.getName().equals(next.getName())) {
					res.add(next);
				}
			}
			return res;
		}
	}
}