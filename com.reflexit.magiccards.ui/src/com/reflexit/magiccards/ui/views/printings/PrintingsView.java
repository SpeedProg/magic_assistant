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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ShowInContext;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractSingleControlCardsView;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;

/**
 * Shows different prints of the same card in different sets and per collection
 *
 */
public class PrintingsView extends AbstractSingleControlCardsView implements ISelectionListener {
	public static final String ID = PrintingsView.class.getName();
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
		((IMagicCardListControl) getMagicControl()).setStatus("Click on a card to populate the view");
		loadInitial();
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewprintings");
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		// manager.add(refresh);
		// manager.add(sync);
		// manager.add(((IMagicCardListControl) control).getGroupMenu());
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		fillShowInMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		// drillDownAdapter.addNavigationActions(manager);
		manager.add(sync);
		// manager.add(this.groupMenuButton);
		manager.add(refresh);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.refresh = new RefreshAction(this::updateViewer);
		this.sync = new Action("Update printings from web", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/software_update.png"));
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
		((PrintingListControl) getMagicControl()).setCard(card);
		new Job("Printings reload") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				reloadData();
				return Status.OK_STATUS;
			}
		}.schedule();
		MagicLogger.trace("printings selection done");
	}

	@Override
	protected PrintingListControl createViewControl() {
		return new PrintingListControl();
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