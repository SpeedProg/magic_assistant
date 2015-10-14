package com.reflexit.magiccards.ui.wizards;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.INewWizard;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.Rarity;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.LocationPath;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public class BoosterGeneratorCollectionWizard extends NewCardCollectionWizard implements INewWizard {
	public static final String ID = "com.reflexit.magiccards.ui.wizards.BoosterGeneratorCollectionWizard";
	private BoosterGeneratorCollectionWizardPage page2;
	private List sets;
	private int packs;
	private int countRare;
	private int countCommon;
	private int countUncommon;
	private boolean separateCollections;

	static class BoosterGeneratorCollectionWizardPage extends WizardPage implements ICheckStateListener {
		Spinner sp;
		private LocationFilterPreferencePage locPage;
		private CheckboxTreeViewer listViewer;
		private PreferenceStore store;
		private Spinner rc;
		private Spinner ru;
		private Spinner rr;
		private Button sep;

		/**
		 * @param pageName
		 * @param title
		 * @param titleImage
		 */
		protected BoosterGeneratorCollectionWizardPage() {
			super("page2", "Booster Pack", null);
			setDescription("Specify booster pack(s) properties");
			store = new PreferenceStore();
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
			Composite am = createAmountControl(a);
			GridData data = new GridData(GridData.FILL_VERTICAL);
			// data.horizontalSpan = 2;
			am.setLayoutData(data);
			Control lp = createLocationPicker(a);
			data = new GridData(GridData.FILL_VERTICAL);
			// data.horizontalSpan = 2;
			lp.setLayoutData(data);
			setControl(a);
			pageChanged();
		}

		private Composite createAmountControl(Composite p) {
			Composite a = new Composite(p, SWT.NONE);
			a.setLayout(new GridLayout(2, false));
			sp = labelAndSpinner(a, 3, "Number of booster packs: ");
			rc = labelAndSpinner(a, 11, "Common: ");
			ru = labelAndSpinner(a, 3, "Uncommon: ");
			rr = labelAndSpinner(a, 1, "Rare+: ");
			sep = new Button(a, SWT.CHECK);
			sep.setText("Generate each booster pack as separate collection");
			sep.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					pageChanged();
				}
			});
			return a;
		}

		public Spinner labelAndSpinner(Composite a, int current, String name) {
			Label lb = new Label(a, SWT.NONE);
			lb.setText(name);
			Spinner sp2 = new Spinner(a, SWT.NONE);
			sp2.setMinimum(0);
			sp2.setMaximum(50);
			sp2.setSelection(current);
			sp2.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					pageChanged();
				}
			});
			return sp2;
		}

		protected Control createLocationPicker(final Composite parent) {
			locPage = new LocationFilterPreferencePage(SWT.MULTI);
			locPage.noDefaultAndApplyButton();
			locPage.setPreferenceStore(store);
			locPage.createControl(parent);
			locPage.setChecked(DataManager.getInstance().getModelRoot().getCollectionsContainer(), true);
			listViewer = (CheckboxTreeViewer) locPage.getViewer();
			GridData data = new GridData(GridData.FILL_BOTH);
			listViewer.getControl().setLayoutData(data);
			listViewer.addCheckStateListener(this);
			return locPage.getControl();
		}

		@Override
		public void checkStateChanged(final CheckStateChangedEvent event) {
			pageChanged();
		}

		protected void pageChanged() {
			Object[] checkedElements = listViewer.getCheckedElements();
			if (checkedElements.length == 0)
				updateStatus("Select one or more collections");
			else
				updateStatus(null);
		}

		protected void updateStatus(String message) {
			setErrorMessage(message);
			setPageComplete(message == null);
		}
	}

	@Override
	public void addPages() {
		this.page2 = new BoosterGeneratorCollectionWizardPage();
		addPage(this.page2);
		this.page = new NewCardCollectionWizardPage(this.selection) {
			@Override
			public String getResourceNameHint() {
				String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				return "booster_" + time;
			}

			@Override
			protected void createOptionsGroup(Composite container) {
				super.createOptionsGroup(container);
				virtual.setSelection(true);
				virtual.setEnabled(false);
			}
		};
		addPage(this.page);
	}

	@Override
	protected void beforeFinish() {
		page2.locPage.performOk();
		this.sets = new ArrayList<CardElement>();
		String[] names = page2.store.preferenceNames();
		for (int i = 0; i < names.length; i++) {
			String string = names[i];
			if (page2.store.getBoolean(string)) {
				sets.add(string);
			}
		}
		this.packs = this.page2.sp.getSelection();
		countRare = page2.rr.getSelection();
		countCommon = page2.rc.getSelection();
		countUncommon = page2.ru.getSelection();
		separateCollections = page2.sep.getSelection();
	}

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
		CardCollection col = populateLibrary(BoosterGeneratorCollectionWizard.this.sets,
				BoosterGeneratorCollectionWizard.this.packs, name,
				parent, virtual, new SubProgressMonitor(monitor, 7));
		monitor.worked(1);
		if (col != null)
			DeckView.openCollection(col, null);
		monitor.done();
	}

	/**
	 * @param firstElement
	 * @param selection
	 * @param col
	 * @param subProgressMonitor
	 * @return
	 */
	private CardCollection populateLibrary(List collections, int packs, String name, CardOrganizer parent,
			boolean virtual,
			IProgressMonitor monitor) {
		monitor.beginTask("Generating", 9 * packs + 1);
		CardCollection col = null;
		monitor.worked(1);
		// ICardStore<IMagicCard> store = col.getStore();
		HashMap<String, String> filterset = new HashMap<String, String>();
		IFilteredCardStore<IMagicCard> dbcards = DataManager.getCardHandler()
				.getLibraryFilteredStoreWorkingCopy();
		MagicCardFilter filter = dbcards.getFilter();
		boolean succ = false;
		for (Object o : collections) {
			if (o instanceof String) {
				filterset.put((String) o, "true");
			}
		}
		if (!separateCollections) {
			col = new CardCollection(name + ".xml", parent, false, virtual);
			try {
				// 1*packs rare cards
				generateRarity(countRare * packs, filterset, filter, dbcards, col, Rarity.MYTHIC_RARE,
						Rarity.RARE);
				monitor.worked(3 * packs);
				// 3*packs uncommon
				generateRarity(countUncommon * packs, filterset, filter, dbcards, col, Rarity.UNCOMMON,
						Rarity.OTHER);
				monitor.worked(3 * packs);
				// 11*packs common
				generateRarity(countCommon * packs, filterset, filter, dbcards, col, Rarity.COMMON);
				monitor.worked(3 * packs);
				succ = true;
			} finally {
				if (!succ) {
					col.remove();
					col = null;
				}
			}
		} else {
			for (int pack = 0; pack < packs; pack++) {
				col = new CardCollection(name + "_" + (pack + 1) + ".xml", parent, false, virtual);
				try {
					// 1*packs rare cards
					generateRarity(countRare, filterset, filter, dbcards, col, Rarity.MYTHIC_RARE,
							Rarity.RARE);
					monitor.worked(3);
					// 3*packs uncommon
					generateRarity(countUncommon, filterset, filter, dbcards, col, Rarity.UNCOMMON,
							Rarity.OTHER);
					monitor.worked(3);
					// 11*packs common
					generateRarity(countCommon, filterset, filter, dbcards, col, Rarity.COMMON);
					monitor.worked(3);
					succ = true;
				} finally {
					if (!succ) {
						col.remove();
					}
				}
			}
		}
		monitor.done();
		return col;
	}

	public void generateRarity(int cards, HashMap<String, String> filterset, MagicCardFilter filter,
			IFilteredCardStore<IMagicCard> dbcards, CardCollection col, String... rarity) {
		if (cards == 0)
			return;
		for (int i = 0; i < rarity.length; i++) {
			String r = rarity[i];
			String id = Rarity.getInstance().getPrefConstant(r);
			filterset.put(id, "true");
		}
		try {
			filter.update(filterset);
			dbcards.update();
			if (dbcards.getSize() == 0) {
				prompt("No cards found of rarity " + Arrays.asList(rarity));
				return;
			}
			try {
				generateRandom(cards, dbcards, col.getStore(), col);
			} catch (MagicException e) {
				prompt(e.getMessage() + " of rarity " + Arrays.asList(rarity));
			}
		} finally {
			for (int i = 0; i < rarity.length; i++) {
				String r = rarity[i];
				String id = Rarity.getInstance().getPrefConstant(r);
				filterset.remove(id);
			}
		}
	}

	private boolean prompt(final String str) {
		final Boolean result[] = new Boolean[] { false };
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				boolean ok = MessageDialog
						.openConfirm(
								getShell(),
								"Confirm",
								str
										+ ". Press OK to continue generation and Cancel to abort and change selection and options");
				result[0] = ok;
			}
		});
		if (!result[0]) {
			throw new MagicException("Booster generation aborted. Modify parameters and try again.");
		}
		return result[0];
	}

	/**
	 * @param num
	 * @param dbcards
	 * @param store
	 * @param col
	 */
	private void generateRandom(int num, IFilteredCardStore<IMagicCard> dbcards,
			ICardStore<IMagicCard> store, CardElement col) {
		for (int i = 0; i < num; i++) {
			try {
				generateRandom(dbcards, store);
			} catch (MagicException e) {
				throw new MagicException("Only can add " + i + " of " + num + " cards");
			}
		}
	}

	private void generateRandom(IFilteredCardStore<IMagicCard> sourceCards, ICardStore<IMagicCard> store) {
		boolean succ = false;
		while (!succ) {
			int rcards = sourceCards.getSize();
			if (rcards == 0)
				throw new MagicException("No more cards found");
			int index = (int) (Math.random() * rcards);
			IMagicCard card = (IMagicCard) sourceCards.getElement(index);
			MagicCardPhysical pcard = new MagicCardPhysical(card, store.getLocation());
			pcard.setOwn(!store.isVirtual());
			pcard.setCount(1);
			IMagicCard old = store.getCard(pcard.getCardId());
			if (old != null) {
				int ncount = ((MagicCardPhysical) old).getCount();
				if (ncount >= ((ICardCountable) card).getCount()) {
					((AbstractFilteredCardStore) sourceCards).getCardStore().remove(card);
					continue;
				}
			}
			store.add(pcard);
			succ = true;
		}
	}
}