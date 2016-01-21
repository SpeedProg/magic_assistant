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
package com.reflexit.magiccards.ui.views.instances;

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
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ShowInContext;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractSingleControlCardsView;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.printings.PrintingsView;

/**
 * Shows different prints of the same card in different sets and per collection
 *
 */
public class InstancesView extends AbstractSingleControlCardsView implements ISelectionListener {
	public static final String ID = InstancesView.class.getName();
	private Action delete;
	private Action refresh;
	private IMagicCard card;
	private Action showPrintings;

	/**
	 * The constructor.
	 */
	public InstancesView() {
	}

	@Override
	public void createMainControl(Composite parent) {
		super.createMainControl(parent);
		((IMagicCardListControl) getMagicControl()).setStatus("Click on a card to populate the view");
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewinstances");
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		ActionHandler deleteHandler = new ActionHandler(this.delete);
		IHandlerService service = (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", deleteHandler);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		// manager.add(refresh);
		// manager.add(((IMagicCardListControl) control).getGroupMenu());
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		// manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		// drillDownAdapter.addNavigationActions(manager);
		fillShowInMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		// drillDownAdapter.addNavigationActions(manager);
		// manager.add(this.groupMenuButton);
		manager.add(((InstancesListControl) getMagicControl()).getGroupAction());
		manager.add(refresh);
		manager.add(showPrintings);
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
		showPrintings = new Action("Show Printings") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/m16.png"));
			}

			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						try {
							page.showView(PrintingsView.ID);
						} catch (PartInitException e) {
							MagicUIActivator.log(e);
						}
					}
				}
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
					new UpdateCardsFromWeb().updateStore(card, fieldMap, null, store,
							new CoreMonitorAdapter(new SubProgressMonitor(monitor, 90)));
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

	@Override
	protected void activate() {
		super.activate();
		try {
			ISelectionService s = getSite().getWorkbenchWindow().getSelectionService();
			ISelection sel = s.getSelection();
			if (sel != null)
				runLoadJob(sel);
		} catch (NullPointerException e) {
			// workbench of active window is null, just ignore then
		}
	}

	@Override
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
		((InstancesListControl) getMagicControl()).setCard(card);
		reloadData();
	}

	@Override
	protected InstancesListControl createViewControl() {
		return new InstancesListControl() {
			@Override
			public void activate() {
				// ignore
			}
		};
	}

	protected void updateViewer() {
		if (card != null)
			getSelectionProvider().setSelection(new StructuredSelection(card));
	}

	@Override
	protected String getPreferencePageId() {
		return ID;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ShowInContext getShowInContext() {
		IStructuredSelection selection = getSelection();
		return new ShowInContext(card,
				(selection.isEmpty() && card != null) ? new StructuredSelection(card) : selection);
	}
}