package com.reflexit.magiccards.ui.views.card;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.MagicDbView;

public class CardDescView extends ViewPart implements ISelectionListener {
	public static final String ID = CardDescView.class.getName();
	private CardDescComposite panel;
	private Label message;
	private LoadCardJob loadCardJob;
	private Action sync;
	private Action actionAsScanned;
	private boolean asScanned;

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
			if (forceUpdate) {
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						setMessage("Loading...");
						CardDescView.this.panel.reload(card);
					}
				});
			} else {
				CardDescView.this.panel.setCard(card);
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						boolean nocard = (card == IMagicCard.DEFAULT);
						CardDescView.this.panel.setVisible(!nocard);
						if (nocard)
							setMessage("Click on a card to populate the view");
						else
							setMessage("");
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
				loadCardImage(new SubProgressMonitor(monitor, 45), card, forceUpdate);
				loadCardExtraInfo(new SubProgressMonitor(monitor, 45), card);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			}
			monitor.done();
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
					fieldMap.addAll(getAllExtraFields());
				return loadCardExtraInfo(monitor, card, fieldMap);
			} catch (IOException e) {
				return MagicUIActivator.getStatus(e);
			} finally {
				monitor.done();
			}
		}

		public Set<ICardField> getAllExtraFields() {
			HashSet<ICardField> res = new HashSet<ICardField>();
			res.add(MagicCardField.RATING);
			res.add(MagicCardField.ARTIST);
			res.add(MagicCardField.COLLNUM);
			res.add(MagicCardField.ORACLE);
			res.add(MagicCardField.TEXT);
			res.add(MagicCardField.TYPE);
			res.add(MagicCardField.NAME);
			return res;
		}

		IStatus loadCardExtraInfo(IProgressMonitor monitor, final IMagicCard card, HashSet<ICardField> fieldMap) throws IOException {
			monitor.beginTask("Loading info for " + card.getName(), 100);
			try {
				if (!isStillNeeded(card))
					return Status.CANCEL_STATUS;
				if (fieldMap.size() == 0)
					return Status.OK_STATUS;
				if (card.getCardId() == 0)
					return Status.OK_STATUS;
				ICardStore store = DataManager.getCardHandler().getMagicDBStore();
				new UpdateCardsFromWeb().updateStore(card, fieldMap, null, store, new CoreMonitorAdapter(
						new SubProgressMonitor(monitor, 99)));
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

	boolean isStillNeeded(final IMagicCard card) {
		return panel.getCard() == card;
	}

	protected IStatus loadCardImage(IProgressMonitor monitor, final IMagicCard card, boolean forceUpdate) {
		monitor.beginTask("Loading image for " + card.getName(), 100);
		try {
			if (!isStillNeeded(card))
				return Status.CANCEL_STATUS;
			Image remoteImage1 = null;
			IOException e1 = null;
			try {
				if (card.getCardId() != 0) {
					String path = ImageCreator.getInstance().createCardPath(card, CardCache.isLoadingEnabled(), forceUpdate);
					boolean resize = asScanned == false;
					remoteImage1 = ImageCreator.getInstance().createCardImage(path, resize);
				}
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
					setMessage("");
					if (e != null)
						setMessage(e.getMessage());
					else if (!CardCache.isLoadingEnabled())
						setMessage("Image loading is disabled");
					else if (card.getGathererId() == 0)
						setMessage("Card does not exist in dababase");
					if (!isStillNeeded(card))
						return;
					Image image = remoteImage;
					if (image == null || image.getBounds().width < 20) {
						image = ImageCreator.getInstance().createCardNotFoundImage(card);
					}
					// rotate image if needed
					String options = (String) card.get(MagicCardField.PART);
					if (options != null && options.length() > 0 && image != null) {
						int rotate = 0;
						if (options.contains("rotate180")) {
							rotate = 180;
						} else if (options.contains("rotate90")) {
							rotate = 90;
						}
						if (rotate != 0) {
							Image rimage = ImageCreator.getInstance().getRotated(image, rotate);
							image.dispose();
							image = rimage;
						}
					}
					if (!isStillNeeded(card))
						return;
					CardDescView.this.panel.setImage(card, image);
				}
			});
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	public void setMessage(String text) {
		if (text == null || text.length() == 0) {
			message.setText("");
			message.setVisible(false);
			((GridData) message.getLayoutData()).heightHint = 0;
		} else {
			message.setText(text);
			message.setVisible(true);
			((GridData) message.getLayoutData()).heightHint = SWT.DEFAULT;
		}
		message.getParent().layout(true);
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MagicUIActivator.helpId("viewcard"));
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(panel,
		// MagicUIActivator.helpId("viewcard"));
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
		manager.add(actionAsScanned);
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
		this.actionAsScanned = new Action("When depressed - scanned image is not scaled", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/zoom_original.png"));
			}

			@Override
			public void run() {
				asScanned = actionAsScanned.isChecked();
				loadCardImage(new NullProgressMonitor(), panel.getCard(), false);
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
		this.panel.getParent().setFocus();
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
			if (firstElement instanceof ICardGroup) {
				if (((ICardGroup) firstElement).size() == 0)
					return IMagicCard.DEFAULT;
				return ((CardGroup) firstElement).getFirstCard();
			}
			if (firstElement instanceof IMagicCard) {
				IMagicCard card = (IMagicCard) firstElement;
				return card;
			}
		}
		return IMagicCard.DEFAULT;
	}

	private void runLoadJob(ISelection sel) {
		final IMagicCard card = getCard(sel);
		if (CardDescView.this.panel.getCard() == card || sel.isEmpty())
			return;
		this.loadCardJob.cancel();
		this.loadCardJob = new LoadCardJob(card);
		this.loadCardJob.schedule();
	}

	public Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}

	public void setSelection(ISelection sel) {
		runLoadJob(sel);
	}
}
