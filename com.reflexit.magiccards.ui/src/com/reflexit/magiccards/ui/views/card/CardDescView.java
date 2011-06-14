package com.reflexit.magiccards.ui.views.card;

import java.io.IOException;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.sync.ParseGathererRulings;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.printings.PrintingsView;

public class CardDescView extends ViewPart implements ISelectionListener {
	public static final String ID = CardDescView.class.getName();
	private CardDescComposite panel;
	private Label message;
	private LoadCardJob loadCardJob;
	private Action sync;

	public class LoadCardJob extends Job {
		private IMagicCard card;
		private boolean forceUpdate;

		public LoadCardJob(IMagicCard card) {
			super("Loading card image");
			this.card = card;
		}

		public LoadCardJob() {
			super("Sync with web");
			this.card = panel.getCard();
			this.forceUpdate = true;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			setName("Loading card info: " + card.getName());
			monitor.beginTask("Loading info for " + card.getName(), 100);
			if (!forceUpdate) {
				CardDescView.this.panel.setCard(card);
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						boolean nocard = (card == IMagicCard.DEFAULT);
						CardDescView.this.panel.setVisible(!nocard);
						CardDescView.this.message.setVisible(nocard);
						if (nocard)
							message.setText("Click on a card to populate the view");
						else
							message.setText("");
						message.getParent().layout(true);
						if (!isStillNeeded(card))
							return;
						CardDescView.this.panel.reload(card);
					}
				});
			}
			monitor.worked(10);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			if (card != IMagicCard.DEFAULT) {
				loadCardImage(new SubProgressMonitor(monitor, 45), card);
				loadCardExtraInfo(new SubProgressMonitor(monitor, 45), card);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		protected IStatus loadCardImage(IProgressMonitor monitor, final IMagicCard card) {
			monitor.beginTask("Loading image for " + card.getName(), 100);
			try {
				if (!isStillNeeded(card))
					return Status.CANCEL_STATUS;
				Image remoteImage1 = null;
				IOException e1 = null;
				try {
					remoteImage1 = ImageCreator.getInstance().getCardImage(card, CardCache.isLoadingEnabled(), false);
				} catch (CachedImageNotFoundException e) {
					// skip
				} catch (IOException e) {
					e1 = e;
				}
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (!isStillNeeded(card))
					return Status.CANCEL_STATUS;
				monitor.worked(90);
				final Image remoteImage = remoteImage1;
				final IOException e = e1;
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						if (remoteImage == null || remoteImage.getBounds().width < 20) {
							CardDescView.this.panel.setImageNotFound(card, e);
							message.setVisible(true);
							if (e != null)
								message.setText(e.getMessage());
							else
								message.setText("Image loading is disabled");
							message.getParent().layout(true);
						} else {
							if (!isStillNeeded(card))
								return;
							CardDescView.this.panel.setImage(card, remoteImage);
						}
					}
				});
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}

		protected IStatus loadCardExtraInfo(IProgressMonitor monitor, final IMagicCard card) {
			try {
				boolean updateRulings = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LOAD_RULINGS);
				boolean updateExtras = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LOAD_EXTRAS);
				boolean updateSets = MagicUIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LOAD_PRINTINGS);
				if (forceUpdate) {
					updateRulings = true;
					updateExtras = true;
				}
				if (updateExtras == false && updateRulings == false && updateSets == false)
					return Status.OK_STATUS;
				HashSet<ICardField> fieldMap = new HashSet<ICardField>();
				if (updateRulings)
					fieldMap.add(MagicCardField.RULINGS);
				if (updateSets)
					fieldMap.add(MagicCardField.SET);
				if (updateExtras)
					fieldMap.addAll(ParseGathererRulings.getAllExtraFields());
				return loadCardExtraInfo(monitor, card, fieldMap);
			} catch (IOException e) {
				return MagicUIActivator.getStatus(e);
			} finally {
				monitor.done();
			}
		}

		boolean isStillNeeded(final IMagicCard card) {
			return panel.getCard() == card;
		}

		IStatus loadCardExtraInfo(IProgressMonitor monitor, final IMagicCard card, HashSet<ICardField> fieldMap) throws IOException {
			monitor.beginTask("Loading info for " + card.getName(), 100);
			try {
				if (!isStillNeeded(card))
					return Status.CANCEL_STATUS;
				if (fieldMap.size() == 0)
					return Status.OK_STATUS;
				new ParseGathererRulings().updateCard(card, new SubProgressMonitor(monitor, 99), fieldMap);
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						if (!isStillNeeded(card))
							return;
						CardDescView.this.panel.setText(card);
					}
				});
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		this.message = new Label(parent, SWT.WRAP);
		this.message.setText("Click on a card to populate the view");
		this.message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.panel = new CardDescComposite(this, parent, SWT.BORDER);
		this.panel.setVisible(false);
		this.panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.loadCardJob = new LoadCardJob(IMagicCard.DEFAULT);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		revealCurrentSelection();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(getControl());
		getControl().setMenu(menu);
		// getSite().registerContextMenu(menuMgr, getViewer());
	}

	private Control getControl() {
		return panel;
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		// fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		// setGlobalHandlers();
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(sync);
	}

	private void fillContextMenu(IMenuManager manager) {
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	void makeActions() {
		this.sync = new Action("Update card info from web", SWT.NONE) {
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

	@Override
	public void setFocus() {
		this.panel.setFocus();
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

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part instanceof AbstractCardsView || part instanceof PrintingsView)
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

	private void runLoadJob(ISelection sel) {
		final IMagicCard card = getCard(sel);
		if (CardDescView.this.panel.getCard() == card)
			return;
		this.loadCardJob.cancel();
		this.loadCardJob = new LoadCardJob(card);
		this.loadCardJob.schedule();
	}

	public Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}
}
