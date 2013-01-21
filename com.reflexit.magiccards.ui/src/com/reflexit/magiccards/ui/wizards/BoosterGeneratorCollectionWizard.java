package com.reflexit.magiccards.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
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
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class BoosterGeneratorCollectionWizard extends NewCardCollectionWizard implements INewWizard {
	public static final String ID = "com.reflexit.magiccards.ui.wizards.BoosterGeneratorCollectionWizard";
	private BoosterGeneratorCollectionWizardPage page2;
	private List sets;
	private int packs;
	private int countRare;
	private int countCommon;
	private int countUncommon;

	static class BoosterGeneratorCollectionWizardPage extends WizardPage implements ICheckStateListener {
		Spinner sp;
		private LocationFilterPreferencePage locPage;
		private CheckboxTreeViewer listViewer;
		private PreferenceStore store;
		private Spinner rc;
		private Spinner ru;
		private Spinner rr;

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
			listViewer = (CheckboxTreeViewer) locPage.getViewer();
			GridData data = new GridData(GridData.FILL_BOTH);
			listViewer.getControl().setLayoutData(data);
			listViewer.addCheckStateListener(this);
			return locPage.getControl();
		}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard#addPages()
	 */
	@Override
	public void addPages() {
		this.page2 = new BoosterGeneratorCollectionWizardPage();
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard#doFinish(java .lang.String,
	 * java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doFinish(final String containerName, final String name, boolean virtual, final IProgressMonitor monitor)
			throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + name, 10);
		ModelRoot root = DataManager.getModelRoot();
		final CardElement resource = root.findElement(new LocationPath(containerName));
		if (!(resource instanceof CardOrganizer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		monitor.worked(1);
		CardOrganizer parent = (CardOrganizer) resource;
		final CardCollection col = new CardCollection(name + ".xml", parent, false);
		populateLibrary(BoosterGeneratorCollectionWizard.this.sets, BoosterGeneratorCollectionWizard.this.packs, col,
				new SubProgressMonitor(monitor, 7));
		monitor.worked(1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// ignore
		}
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IViewPart view = page.showView(CardsNavigatorView.ID);
					view.getViewSite().getSelectionProvider().setSelection(new StructuredSelection(col));
					monitor.worked(1);
					page.showView(DeckView.ID, col.getFileName(), IWorkbenchPage.VIEW_ACTIVATE);
					monitor.worked(1);
				} catch (PartInitException e) {
					// ignore
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
	private void populateLibrary(List collections, int packs, CardCollection col, IProgressMonitor monitor) {
		monitor.beginTask("Generating", 9 * packs + 1);
		if (col.isOpen() == false) {
			col.open();
		}
		monitor.worked(1);
		ICardStore<IMagicCard> store = col.getStore();
		HashMap<String, String> filterset = new HashMap<String, String>();
		IFilteredCardStore<IMagicCard> dbcards = DataManager.getCardHandler().getLibraryFilteredStoreWorkingCopy();
		MagicCardFilter filter = dbcards.getFilter();
		try {
			for (Object o : collections) {
				if (o instanceof String) {
					filterset.put((String) o, "true");
				}
			}
			for (int i = 0; i < packs; i++) {
				// 1*packs rare cards
				generateRarity(countRare, filterset, filter, dbcards, col, Rarity.MYTHIC_RARE, Rarity.RARE);
				monitor.worked(3);
				// 3*packs uncommon
				generateRarity(countUncommon, filterset, filter, dbcards, col, Rarity.UNCOMMON, Rarity.OTHER);
				monitor.worked(3);
				// 11*packs common
				generateRarity(countCommon, filterset, filter, dbcards, col, Rarity.COMMON);
				monitor.worked(3);
			}
		} finally {
			monitor.done();
		}
	}

	public void generateRarity(int cards, HashMap<String, String> filterset, MagicCardFilter filter,
			IFilteredCardStore<IMagicCard> dbcards, CardCollection col, String... rarity) {
		for (int i = 0; i < rarity.length; i++) {
			String r = rarity[i];
			String id = Rarity.getInstance().getPrefConstant(r);
			filterset.put(id, "true");
		}
		filter.update(filterset);
		dbcards.update();
		if (dbcards.getSize() == 0) {
			throw new MagicException("No cards of rarity " + rarity[0] + " found in the selected collections");
		}
		generateRandom(cards, dbcards, col.getStore(), col);
		for (int i = 0; i < rarity.length; i++) {
			String r = rarity[i];
			String id = Rarity.getInstance().getPrefConstant(r);
			filterset.remove(id);
		}
	}

	/**
	 * @param packs
	 * @param dbcards
	 * @param store
	 * @param col
	 */
	private void generateRandom(int packs, IFilteredCardStore<IMagicCard> dbcards, ICardStore<IMagicCard> store, CardElement col) {
		int rcards = dbcards.getSize();
		if (rcards == 0)
			return;
		for (int i = 0; i < packs; i++) {
			int index = (int) (Math.random() * rcards);
			IMagicCard card = (IMagicCard) dbcards.getElement(index);
			MagicCardPhysical pcard = new MagicCardPhysical(card, col.getLocation());
			store.add(pcard);
		}
	}
}