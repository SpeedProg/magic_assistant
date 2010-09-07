package com.reflexit.magiccards.ui.views.card;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.io.IOException;
import java.net.URL;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.sync.ParseGathererRulings;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.MagicDbView;

public class CardDescView extends ViewPart implements ISelectionListener {
	public static final String ID = CardDescView.class.getName();
	private CardDescComposite panel;
	private Label message;
	private LoadCardJob loadCardJob;
	public class LoadCardJob extends Job {
		private IMagicCard card;

		public LoadCardJob(IMagicCard card) {
			super("Loading card image");
			this.card = card;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			setName("Loading card info: " + card.getName());
			monitor.beginTask("Loading info for " + card.getName(), 100);
			CardDescView.this.panel.setCard(card);
			getViewSite().getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					boolean nocard = (card == IMagicCard.DEFAULT);
					CardDescView.this.panel.setVisible(!nocard);
					CardDescView.this.message.setVisible(nocard);
					if (nocard)
						message.setText("Click on a card to populate the view");
					if (!isStillNeeded(card))
						return;
					CardDescView.this.panel.reload(card);
				}
			});
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

		protected boolean isStillNeeded(final IMagicCard card) {
			return CardDescView.this.panel.getCard() == card;
		}

		protected IStatus loadCardImage(IProgressMonitor monitor, final IMagicCard card) {
			monitor.beginTask("Loading image for " + card.getName(), 100);
			try {
				if (!isStillNeeded(card))
					return Status.CANCEL_STATUS;
				Image remoteImage1 = null;
				IOException e1 = null;
				try {
					remoteImage1 = createCardImage(card);
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
			monitor.beginTask("Loading info for " + card.getName(), 100);
			try {
				boolean update = MagicUIActivator.getDefault().getPreferenceStore()
				        .getBoolean(PreferenceConstants.LOAD_RULINGS);
				if (update == false)
					return Status.OK_STATUS;
				if (!isStillNeeded(card))
					return Status.CANCEL_STATUS;
				new ParseGathererRulings().updateCard(card, monitor, null);
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						if (!isStillNeeded(card))
							return;
						CardDescView.this.panel.setText(card);
					}
				});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		revealCurrentSelection();
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

	private Image createCardImage(IMagicCard card) throws IOException {
		URL url = CardCache.createCardURL(card);
		if (url == null)
			return null;
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		Image remoteImage = imageDesc.createImage(false, getDisplay());
		MagicUIActivator.trace("Loading URL: " + url + (remoteImage == null ? " failed" : " success"));
		return remoteImage;
	}
}
