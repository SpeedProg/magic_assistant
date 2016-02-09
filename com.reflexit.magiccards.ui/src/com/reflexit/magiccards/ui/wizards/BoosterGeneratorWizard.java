package com.reflexit.magiccards.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.INewWizard;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.Rarity;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.LocationPath;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;
import com.reflexit.magiccards.ui.views.lib.DeckView;

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

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
		 * .widgets.Composite)
		 */
		@Override
		public void createControl(Composite parent) {
			Composite a = new Composite(parent, SWT.NONE);
			a.setLayout(new GridLayout(2, false));
			createAmountControl(a);
			this.edi = new EditionsComposite(a, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION, true);
			this.edi.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					pageChanged();
				}
			});
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			this.edi.setLayoutData(gd);
			setControl(a);
			pageChanged();
		}

		private void createAmountControl(Composite a) {
			Label lb = new Label(a, SWT.NONE);
			lb.setText("Number of booster packs: ");
			this.sp = new Spinner(a, SWT.NONE);
			this.sp.setMinimum(1);
			this.sp.setMaximum(50);
			this.sp.setSelection(3);
			this.sp.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					pageChanged();
				}
			});
		}

		protected void pageChanged() {
			IStructuredSelection sel = edi.getSelection();
			if (sel.isEmpty())
				updateStatus("Select one or more sets");
			else
				updateStatus(null);
		}

		protected void updateStatus(String message) {
			setErrorMessage(message);
			setPageComplete(message == null);
		}
	}

	private BoosterGeneratorWizardPage page2;
	private ArrayList<Edition> sets;
	private int packs;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard#addPages()
	 */
	@Override
	public void addPages() {
		this.page2 = new BoosterGeneratorWizardPage();
		addPage(this.page2);
		this.page = new NewCardCollectionWizardPage(this.selection) {
			@Override
			public String getResourceNameHint() {
				return "";
			}
		};
		addPage(this.page);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.wizards.NewCardElementWizard#beforeFinish()
	 */
	@Override
	protected void beforeFinish() {
		IStructuredSelection sel = this.page2.edi.getSelection();
		this.sets = new ArrayList<Edition>();
		sets.addAll(sel.toList());
		this.packs = this.page2.sp.getSelection();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard#doFinish(java .lang.String,
	 * java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doFinish(final String containerName, final String name, boolean virtual,
			final IProgressMonitor monitor)
			throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + name, 10);
		ModelRoot root = getModelRoot();
		final CardElement resource = root.findElement(new LocationPath(containerName));
		if (!(resource instanceof CardOrganizer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		monitor.worked(1);
		CardOrganizer parent = (CardOrganizer) resource;
		final CardCollection col = new CardCollection(name + ".xml", parent, false, virtual);
		populateLibrary(BoosterGeneratorWizard.this.sets, BoosterGeneratorWizard.this.packs, col,
				new SubProgressMonitor(monitor, 7));
		monitor.worked(1);
		DeckView.openCollection(col, null);
		monitor.done();
	}

	/**
	 * @param firstElement
	 * @param selection
	 * @param col
	 * @param subProgressMonitor
	 */
	private void populateLibrary(ArrayList<Edition> sets, int packs, CardCollection col,
			IProgressMonitor monitor) {
		monitor.beginTask("Generating", 10);
		monitor.worked(1);
		ICardStore<IMagicCard> store = col.getStore();
		HashMap<String, String> filterset = new HashMap<String, String>();
		IFilteredCardStore<IMagicCard> dbcards = DataManager.getCardHandler()
				.getMagicDBFilteredStoreWorkingCopy();
		MagicCardFilter filter = dbcards.getFilter();
		try {
			for (Edition ed : sets) {
				String editionId = Editions.getInstance().getPrefConstantByName(ed.getName());
				filterset.put(editionId, "true");
			}
			// 1*packs rare cards
			String rarity = Rarity.getInstance().getPrefConstant(Rarity.RARE);
			filterset.put(rarity, "true");
			String rarity1 = Rarity.getInstance().getPrefConstant(Rarity.MYTHIC_RARE);
			filterset.put(rarity1, "true");
			filter.update(filterset);
			dbcards.update();
			if (dbcards.getSize() == 0) {
				throw new MagicException("No cards found in the selected sets");
			}
			generateRandom(1 * packs, dbcards, store, col);
			monitor.worked(3);
			// 3*packs uncommon
			filterset.remove(rarity);
			filterset.remove(rarity1);
			rarity = Rarity.getInstance().getPrefConstant(Rarity.UNCOMMON);
			filterset.put(rarity, "true");
			filter.update(filterset);
			dbcards.update();
			generateRandom(3 * packs, dbcards, store, col);
			monitor.worked(3);
			// 11*packs common
			filterset.remove(rarity);
			rarity = Rarity.getInstance().getPrefConstant(Rarity.COMMON);
			filterset.put(rarity, "true");
			filter.update(filterset);
			dbcards.update();
			generateRandom(11 * packs, dbcards, store, col);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param packs
	 * @param dbcards
	 * @param store
	 * @param col
	 */
	private void generateRandom(int packs, IFilteredCardStore<IMagicCard> dbcards,
			ICardStore<IMagicCard> store, CardElement col) {
		int rcards = dbcards.getSize();
		for (int i = 0; i < packs; i++) {
			int index = (int) (Math.random() * rcards);
			IMagicCard card = (IMagicCard) dbcards.getElement(index);
			MagicCardPhysical pcard = new MagicCardPhysical(card, col.getLocation());
			store.add(pcard);
		}
	}
}