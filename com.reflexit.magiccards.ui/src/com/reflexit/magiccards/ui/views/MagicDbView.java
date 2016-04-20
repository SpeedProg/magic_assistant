package com.reflexit.magiccards.ui.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ShowInContext;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.TextPrinter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.EditMagicCardDialog;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.card.CardDescView;

public class MagicDbView extends AbstractSingleControlCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.MagicDbView";
	protected MenuManager addToDeck;
	protected IDeckAction copyToDeck;
	protected Action exportDatabase;
	protected Action edit;

	public MagicDbView() {
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewmagicdb");
	}

	class MagicDbListControl extends AbstractMagicCardsListControl {
		public MagicDbListControl() {
			super(Presentation.SPLITTREE);
		}

		@Override
		protected String getPreferencePageId() {
			return MagicDbView.this.getPreferencePageId();
		}

		@Override
		public void handleEvent(CardEvent event) {
			mcEventHandler(event);
		}

		@Override
		public IFilteredCardStore doGetFilteredStore() {
			return DataManager.getCardHandler().getMagicDBFilteredStore();
		}
	}

	@Override
	protected AbstractMagicCardsListControl createViewControl() {
		return new MagicDbListControl();
	}

	@Override
	protected void runDoubleClick() {
		try {
			getViewSite().getWorkbenchWindow().getActivePage().showView(CardDescView.ID);
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToDeck = new MenuManager("Add to");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, copyToDeck);
			}
		});
		copyToDeck = new IDeckAction() {
			@Override
			public void run(String id) {
				IFilteredCardStore fstore = DataManager.getCardHandler().getCardCollectionFilteredStore(id);
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection iss = (IStructuredSelection) selection;
					if (!iss.isEmpty()) {
						DataManager dm = DataManager.getInstance();
						dm.copyCards(dm.expandGroups(iss.toList()), fstore.getCardStore());
					}
				}
			}
		};
		exportDatabase = new Action("Export Database") {
			@Override
			public void run() {
				exportDatabase();
			}
		};
		edit = new Action("Edit...") {
			@Override
			public void run() {
				editCard();
			}
		};
	}

	protected void editCard() {
		IStructuredSelection selection = (IStructuredSelection) getSelectionProvider().getSelection();
		Object el = selection.getFirstElement();
		if (el instanceof MagicCard) {
			MagicCard card = (MagicCard) el;
			new EditMagicCardDialog(getShell(), card).open();
		}
	}

	protected void exportDatabase() {
		ICardStore<IMagicCard> store = getFilteredStore().getCardStore();
		String curset = null;
		File dir = new File("/tmp/madatabase");
		FileUtils.deleteTree(dir);
		dir.mkdirs();
		Editions editions = Editions.getInstance();
		PrintStream ps = null;
		for (IMagicCard magicCard : store) {
			IMagicCard card = magicCard;
			if (card instanceof MagicCard) {
				String set = card.getSet();
				if (set != curset) {
					if (ps != null) {
						ps.close();
					}
					boolean header = false;
					try {
						String abbr = editions.getEditionByName(set).getBaseFileName();
						File file = new File(dir, abbr + ".txt");
						if (!file.exists())
							header = true;
						FileOutputStream fileOutputStream = new FileOutputStream(file, true);
						ps = new PrintStream(fileOutputStream);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (header)
						TextPrinter.printHeader(ps);
					curset = set;
				}
				MagicCard mc = ((MagicCard) card).cloneCard();
				mc.setLegalityMap(null);
				mc.setDbPrice(0);
				TextPrinter.print(mc, ps);
			}
		}
		if (ps != null) {
			ps.close();
		}
		try {
			Editions.getInstance().save(new File(dir, "editions.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDialog.openInformation(getShell(), "Location", "Database is stored in " + dir);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		if (MagicUIActivator.TRACE_EXPORT)
			manager.add(exportDatabase); // this is for internal use only
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.addToDeck);
		manager.add(this.actionCopy);
		manager.add(this.edit);
	}

	@Override
	protected String getPreferencePageId() {
		return MagicDbViewPreferencePage.PPID;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean show(ShowInContext context) {
		ArrayList<Object> l = new ArrayList<>();
		for (Object o : ((IStructuredSelection) context.getSelection()).toList()) {
			if (o instanceof IMagicCard) {
				l.add(((IMagicCard) o).getBase());
			}
		}
		if (l.size() > 0) {
			getSelectionProvider().setSelection(new StructuredSelection(l));
			if (getSelection().isEmpty()) {
				if (MessageDialog.openQuestion(getShell(), "Error", "Cards are not visible, reset filter?")) {
					PreferenceInitializer.setToDefault(getFilterPreferenceStore());
					AbstractMagicCardsListControl lcon = (AbstractMagicCardsListControl) getMagicControl();
					lcon.syncQuickFilter();
					lcon.setNextSelection(new StructuredSelection(l));
					lcon.syncFilter();
					lcon.loadData(null);
				}
			}
		}
		return true;
	}
}