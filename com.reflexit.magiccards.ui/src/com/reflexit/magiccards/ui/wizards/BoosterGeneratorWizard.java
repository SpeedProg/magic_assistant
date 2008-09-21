package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.Rarity;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.views.lib.LibView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;
import com.reflexit.magiccards.ui.widgets.EditionsComposite;

public class BoosterGeneratorWizard extends NewCardCollectionWizard implements INewWizard {
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
			this.edi = new EditionsComposite(a, SWT.BORDER);
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
		this.page = new NewCardCollectionWizardPage(this.selection) {
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
	protected void doFinish(String containerName, String name, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + name, 2);
		ModelRoot root = DataManager.getModelRoot();
		final CardElement resource = root.findElement(new Path(containerName));
		if (!(resource instanceof CollectionsContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		CollectionsContainer parent = (CollectionsContainer) resource;
		final CardCollection col = new CardCollection(name + ".xml", parent);
		populateLibrary(this.editionName, this.packs, col);
		monitor.worked(1);
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IViewPart view = page.showView(CardsNavigatorView.ID);
					view.getViewSite().getSelectionProvider().setSelection(new StructuredSelection(col));
					LibView lib = (LibView) page.showView(LibView.ID);
					lib.setLocationFilter(col.getLocation());
				} catch (PartInitException e) {
					//  ignore
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * @param firstElement
	 * @param selection
	 * @param col 
	 */
	private void populateLibrary(String editionName, int packs, CardCollection col) {
		IFilteredCardStore library = DataManager.getCardHandler().getMagicLibraryHandler();
		MagicCardFilter filter = new MagicCardFilter();
		HashMap filterset = new HashMap();
		IFilteredCardStore dbcards = DataManager.getCardHandler().getMagicCardHandler();
		String editionId = Editions.getInstance().getPrefConstantByName(editionName);
		filterset.put(editionId, "true");
		// 1*packs rare cards
		String rarity = Rarity.getInstance().getPrefConstant("Rare");
		filterset.put(rarity, "true");
		filter.update(filterset);
		dbcards.update(filter);
		generateRandom(1 * packs, dbcards, library, col);
		// 3*packs uncommon
		filterset.remove(rarity);
		rarity = Rarity.getInstance().getPrefConstant("Uncommon");
		filterset.put(rarity, "true");
		filter.update(filterset);
		dbcards.update(filter);
		generateRandom(3 * packs, dbcards, library, col);
		// 11*packs common
		filterset.remove(rarity);
		rarity = Rarity.getInstance().getPrefConstant("Common");
		filterset.put(rarity, "true");
		filter.update(filterset);
		dbcards.update(filter);
		generateRandom(11 * packs, dbcards, library, col);
	}

	/**
	 * @param packs
	 * @param dbcards
	 * @param library
	 * @param col
	 */
	private void generateRandom(int packs, IFilteredCardStore dbcards, IFilteredCardStore library, CardCollection col) {
		int rcards = dbcards.getSize();
		for (int i = 0; i < packs; i++) {
			int index = (int) (Math.random() * rcards);
			IMagicCard card = (IMagicCard) dbcards.getElement(index);
			MagicCardPhisical pcard = new MagicCardPhisical(card);
			pcard.setLocation(col.getLocation());
			library.getCardStore().addCard(pcard);
		}
	}
}