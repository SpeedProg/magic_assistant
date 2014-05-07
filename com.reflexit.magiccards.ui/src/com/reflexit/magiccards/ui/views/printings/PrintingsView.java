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

import java.io.IOException;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.instances.InstancesView;

/**
 * Shows different prints of the same card in different sets and per collection
 * 
 */
public class PrintingsView extends AbstractCardsView implements ISelectionListener {
	public static final String ID = PrintingsView.class.getName();
	private Action delete;
	private Action refresh;
	private Action sync;
	private IMagicCard card;

	/**
	 * The constructor.
	 */
	public PrintingsView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		((IMagicCardListControl) control).setStatus("Click on a card to populate the view");
		loadInitial();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MagicUIActivator.helpId("viewprintings"));
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		ActionHandler deleteHandler = new ActionHandler(this.delete);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", deleteHandler);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		// manager.add(refresh);
		// manager.add(sync);
		// manager.add(((IMagicCardListControl) control).getGroupMenu());
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		// manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		// drillDownAdapter.addNavigationActions(manager);
		manager.add(sync);
		// manager.add(this.groupMenuButton);
		manager.add(showInstances);
		manager.add(refresh);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
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
				updateViewer();
			}
		};
		this.sync = new Action("Update printings from web", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/web_sync.gif"));
			}

			@Override
			public void run() {
				LoadCardJob job = new LoadCardJob();
				job.setUser(true);
				job.schedule();
			}
		};
	}

	class LoadCardJob extends Job {
		public LoadCardJob() {
			super("Loading card sets");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Loading printings", 100);
			try {
				HashSet<ICardField> fieldMap = new HashSet<ICardField>();
				fieldMap.add(MagicCardField.SET);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				try {
					ICardStore store = DataManager.getCardHandler().getMagicDBStore();
					new UpdateCardsFromWeb().updateStore(card, fieldMap, null, store, new CoreMonitorAdapter(new SubProgressMonitor(
							monitor, 90)));
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					reloadData();
				} catch (IOException e) {
					return MagicUIActivator.getStatus(e);
				}
				return Status.OK_STATUS;
			} finally {
				monitor.done();
			}
		}
	}

	/**
	 *
	 */
	protected void actionDelete() {
		// TODO
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	protected void loadInitial() {
		try {
			IWorkbenchPage page = getViewSite().getWorkbenchWindow().getActivePage();
			if (page == null)
				return;
			ISelection sel = page.getSelection();
			if (sel == null || sel.isEmpty()) {
				IViewPart dbview = page.findView(MagicDbView.ID);
				if (dbview != null) {
					sel = dbview.getSite().getSelectionProvider().getSelection();
				}
			}
			if (sel != null)
				runLoadJob(sel);
		} catch (NullPointerException e) {
			// workbench of active window is null, just ignore then
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part instanceof AbstractCardsView && part != this && !sel.isEmpty())
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
		IMagicCard cardSel = getCard(sel);
		if (cardSel == card)
			return;
		this.card = cardSel;
		// System.err.println("Printings for " + card);
		((PrintingListControl) control).setCard(card);
		reloadData();
	}

	@Override
	protected PrintingListControl doGetViewControl() {
		return new PrintingListControl(this);
	}

	@Override
	protected void updateViewer() {
		super.updateViewer();
		if (card != null)
			getSelectionProvider().setSelection(new StructuredSelection(card));
	}

	@Override
	protected String getPreferencePageId() {
		return null;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void runShowInstances(IWorkbenchPage page) {
		try {
			StructuredSelection sel = card == null ? new StructuredSelection() : new StructuredSelection(card);
			InstancesView view = (InstancesView) page.showView(InstancesView.ID);
			view.selectionChanged(this, sel);
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}
}