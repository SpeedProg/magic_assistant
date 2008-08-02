package com.reflexit.magiccards.ui.views.card;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.MagicDbView;

public class CardDescView extends ViewPart implements ISelectionListener {
	public static final String ID = CardDescView.class.getName();
	private CardDescComposite panel;
	private Label message;
	private LoadCardJob loadCardJob;
	public class LoadCardJob extends Job {
		ISelection sel;

		public LoadCardJob(ISelection sel) {
			super("Loading card image");
			this.sel = sel;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final IMagicCard card = getCard(this.sel);
			setName("Loading image: " + card.getName());
			monitor.beginTask("Loading image for " + card.getName(), 100);
			if (CardDescView.this.panel.getCard() == card)
				return Status.OK_STATUS;
			getViewSite().getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					if (card == IMagicCard.DEFAULT || card == null) {
						CardDescView.this.panel.setVisible(false);
						CardDescView.this.message.setVisible(true);
						return;
					}
					CardDescView.this.message.setVisible(false);
					CardDescView.this.panel.setVisible(true);
					CardDescView.this.panel.reload(card);
				}
			});
			monitor.worked(10);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			if (card != IMagicCard.DEFAULT) {
				final Image remoteImage = createRemoteImage(card);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						if (remoteImage.getBounds().width < 20) {
							CardDescView.this.panel.setImageNotFound(card);
						} else {
							CardDescView.this.panel.setImage(card, remoteImage);
						}
					}
				});
				monitor.worked(10);
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		this.message = new Label(parent, SWT.WRAP);
		this.message.setText("Click on card to populate the view");
		this.panel = new CardDescComposite(this, parent, SWT.BORDER);
		this.panel.setVisible(false);
		this.panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.loadCardJob = new LoadCardJob(new StructuredSelection());
		revealCurrentSelection();
	}

	private void revealCurrentSelection() {
		try {
			IViewPart dbview = getViewSite().getWorkbenchWindow().getActivePage().findView(MagicDbView.ID);
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
		//getSite().getPage().addSelectionListener(MagicDbView.ID, this);
		//getSite().getPage().addSelectionListener(LibView.ID, this);
		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		//getSite().getPage().removeSelectionListener(MagicDbView.ID, this);
		//getSite().getPage().removeSelectionListener(LibView.ID, this);
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
		this.loadCardJob.cancel();
		this.loadCardJob = new LoadCardJob(sel);
		this.loadCardJob.schedule();
	}

	private Image createRemoteImage(IMagicCard card) {
		try {
			URL url;
			String edition = card.getEdition();
			String editionAbbr = Editions.getInstance().getAbbrByName(edition);
			if (editionAbbr == null)
				editionAbbr = "";
			int cardId = card.getCardId();
			String locale = guessLocale(cardId, edition, editionAbbr);
			url = createImageURL(cardId, editionAbbr, locale);
			ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
			Image remoteImage = imageDesc.createImage(getDisplay());
			return remoteImage;
		} catch (MalformedURLException e) {
			MagicUIActivator.log(e);
			return null;
		}
	}

	public Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}

	private String guessLocale(int cardId, String edition, String editionAbbr) throws MalformedURLException {
		String locale = Editions.getInstance().getLocale(edition);
		if (locale == null) {
			URL url = null;
			locale = "en-us";
			url = createImageURL(cardId, editionAbbr, locale);
			try {
				url.openStream();
				Editions.getInstance().addLocale(editionAbbr, locale);
			} catch (IOException e) {
				locale = "EN";
				url = createImageURL(cardId, editionAbbr, locale);
				try {
					url.openStream();
					Editions.getInstance().addLocale(editionAbbr, locale);
				} catch (IOException e2) {
					locale = "?";
				}
			}
			try {
				Editions.getInstance().save();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return locale;
	}

	private URL createImageURL(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		return new URL("http://resources.wizards.com/Magic/Cards/" + editionAbbr + "/" + locale + "/Card" + cardId
		        + ".jpg");
	}
}
