package com.reflexit.magiccards.ui.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.TextPrinter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.views.card.CardDescView;
import com.reflexit.magiccards.ui.views.printings.PrintingsView;

public class MagicDbView extends AbstractCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.MagicDbView";
	MenuManager addToDeck;
	protected Action showPrintings;
	protected Action exportDatabase;

	/**
	 * The constructor.
	 */
	public MagicDbView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MagicUIActivator.helpId("viewmagicdb"));
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicDBFilteredStore();
	}

	@Override
	protected AbstractMagicCardsListControl doGetViewControl() {
		return new AbstractMagicCardsListControl(this) {
			@Override
			public IMagicColumnViewer createViewerManager() {
				return new CompositeViewerManager(getId());
			}
		};
	}

	@Override
	protected void runDoubleClick() {
		try {
			getViewSite().getWorkbenchWindow().getActivePage().showView(CardDescView.ID);
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}

	protected IDeckAction copyToDeck;

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToDeck = new MenuManager("Add to");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, copyToDeck);
			}
		});
		copyToDeck = new IDeckAction() {
			public void run(String id) {
				IFilteredCardStore fstore = DataManager.getCardHandler().getCardCollectionFilteredStore(id);
				Location loc = fstore.getLocation();
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						DataManager.getCardHandler().copyCards(sel.toList(), loc);
					}
				}
			}
		};
		showPrintings = new Action("Show Other Sets") {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						try {
							PrintingsView view = (PrintingsView) page.showView(PrintingsView.ID);
							view.setDbMode(true);
						} catch (PartInitException e) {
							MagicUIActivator.log(e);
						}
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
	}

	protected void exportDatabase() {
		ICardStore<IMagicCard> store = getFilteredStore().getCardStore();
		MagicCardField[] fields = MagicCardField.values();
		String curset = null;
		TextPrinter exporter = null;
		File dir = new File("C:/tmp/madatabase");
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
					try {
						String abbr = editions.getEditionByName(set).getBaseFileName();
						FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, abbr + ".txt"));
						ps = new PrintStream(fileOutputStream);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					exporter.printHeader((MagicCard) card, ps);
					curset = set;
				}
				MagicCard mc = (MagicCard) card;
				exporter.print(mc, ps);
			}
		}
		if (ps != null) {
			ps.close();
		}
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(exportDatabase); // this is for internal use only
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.addToDeck);
		manager.add(this.actionCopy);
		manager.add(this.showPrintings);
	}

	@Override
	protected String getPreferencePageId() {
		return MagicDbViewPreferencePage.class.getName();
	}

	@Override
	public String getId() {
		return ID;
	}
}