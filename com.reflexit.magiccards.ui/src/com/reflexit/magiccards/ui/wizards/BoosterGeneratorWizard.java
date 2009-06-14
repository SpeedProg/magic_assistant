package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.util.HashMap;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.Rarity;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;
import com.reflexit.magiccards.ui.widgets.EditionsComposite;

public class BoosterGeneratorWizard extends NewDeckWizard implements INewWizard {
	public static final String ID = "com.reflexit.magiccards.ui.wizards.BoosterGeneratorWizard";
	static class BoosterGeneratorWizardPage extends WizardPage {
		Spinner sp;
		EditionsComposite edi;

		/**
		 * @param pageName
		 * @param title
		 * @param titleImage
		 */
		protected BoosterGeneratorWizardPage() {
			super("page2", "Booster Pack", null);
			setDescription("Specify booster pack(s) properties");
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			Composite a = new Composite(parent, SWT.NONE);
			a.setLayout(new GridLayout(2, false));
			createAmountControl(a);
			this.edi = new EditionsComposite(a, SWT.BORDER, false);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			this.edi.setLayoutData(gd);
			setControl(a);
		}

		private void createAmountControl(Composite a) {
			Label lb = new Label(a, SWT.NONE);
			lb.setText("Number of booster packs: ");
			this.sp = new Spinner(a, SWT.NONE);
			this.sp.setMinimum(1);
			this.sp.setMaximum(50);
			this.sp.setSelection(3);
		}
	}
	private BoosterGeneratorWizardPage page2;
	private String editionName;
	private int packs;

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard#addPages()
	 */
	@Override
	public void addPages() {
		this.page2 = new BoosterGeneratorWizardPage();
		addPage(this.page2);
		this.page = new NewDeckWizardPage(this.selection) {
			@Override
			public String getResourceNameHint() {
				return "booster pack";
			}
		};
		addPage(this.page);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewCardElementWizard#beforeFinish()
	 */
	@Override
	protected void beforeFinish() {
		IStructuredSelection sel = this.page2.edi.getSelection();
		this.editionName = (String) sel.getFirstElement();
		this.packs = this.page2.sp.getSelection();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard#doFinish(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doFinish(final String containerName, final String name, final IProgressMonitor monitor)
	        throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + name, 10);
		ModelRoot root = DataManager.getModelRoot();
		final CardElement resource = root.findElement(new Path(containerName));
		if (!(resource instanceof CardOrganizer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		monitor.worked(1);
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					CardOrganizer parent = (CardOrganizer) resource;
					final CardCollection col = new CardCollection(name + ".xml", parent, true);
					populateLibrary(BoosterGeneratorWizard.this.editionName, BoosterGeneratorWizard.this.packs, col,
					        new SubProgressMonitor(monitor, 7));
					IViewPart view = page.showView(CardsNavigatorView.ID);
					view.getViewSite().getSelectionProvider().setSelection(new StructuredSelection(col));
					monitor.worked(1);
					page.showView(DeckView.ID, col.getFileName(), IWorkbenchPage.VIEW_ACTIVATE);
					monitor.worked(1);
				} catch (PartInitException e) {
					//  ignore
				}
			}
		});
		monitor.done();
	}

	/**
	 * @param firstElement
	 * @param selection
	 * @param col 
	 * @param subProgressMonitor 
	 */
	private void populateLibrary(String editionName, int packs, CardCollection col, IProgressMonitor monitor) {
		monitor.beginTask("Generating", 10);
		if (col.isOpen() == false) {
			IFilteredCardStore fstore = DataManager.getCardHandler().getCardCollectionHandler(col.getFileName());
		}
		monitor.worked(1);
		ICardStore store = col.getStore();
		MagicCardFilter filter = new MagicCardFilter();
		HashMap filterset = new HashMap();
		IFilteredCardStore dbcards = DataManager.getCardHandler().getDatabaseHandler();
		MagicCardFilter oldFilter = dbcards.getFilter();
		try {
			String editionId = Editions.getInstance().getPrefConstantByName(editionName);
			filterset.put(editionId, "true");
			// 1*packs rare cards
			String rarity = Rarity.getInstance().getPrefConstant("Rare");
			filterset.put(rarity, "true");
			filter.update(filterset);
			dbcards.update(filter);
			generateRandom(1 * packs, dbcards, store, col);
			monitor.worked(3);
			// 3*packs uncommon
			filterset.remove(rarity);
			rarity = Rarity.getInstance().getPrefConstant("Uncommon");
			filterset.put(rarity, "true");
			filter.update(filterset);
			dbcards.update(filter);
			generateRandom(3 * packs, dbcards, store, col);
			monitor.worked(3);
			// 11*packs common
			filterset.remove(rarity);
			rarity = Rarity.getInstance().getPrefConstant("Common");
			filterset.put(rarity, "true");
			filter.update(filterset);
			dbcards.update(filter);
			generateRandom(11 * packs, dbcards, store, col);
		} finally {
			monitor.done();
			dbcards.update(oldFilter);
		}
	}

	/**
	 * @param packs
	 * @param dbcards
	 * @param store
	 * @param col
	 */
	private void generateRandom(int packs, IFilteredCardStore dbcards, ICardStore store, CardElement col) {
		int rcards = dbcards.getSize();
		for (int i = 0; i < packs; i++) {
			int index = (int) (Math.random() * rcards);
			IMagicCard card = (IMagicCard) dbcards.getElement(index);
			MagicCardPhisical pcard = new MagicCardPhisical(card);
			pcard.setLocation(col.getLocation());
			store.add(pcard);
		}
	}
}