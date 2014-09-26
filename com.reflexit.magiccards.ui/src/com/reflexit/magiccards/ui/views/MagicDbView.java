package com.reflexit.magiccards.ui.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.TextPrinter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.EditMagicCardDialog;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.views.card.CardDescView;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.printings.PrintingsView;

public class MagicDbView extends AbstractCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.MagicDbView";
	protected MenuManager addToDeck;
	protected Action showPrintings;
	protected IDeckAction copyToDeck;
	protected Action exportDatabase;
	protected Action edit;

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
	protected AbstractMagicCardsListControl doGetViewControl() {
		return new AbstractMagicCardsListControl(this) {
			@Override
			public IMagicColumnViewer createViewerManager() {
				return new CompositeViewerManager(getPreferencePageId()) {
					@Override
					protected ColumnCollection doGetColumnCollection(String prefPageId) {
						return new MagicColumnCollection(prefPageId) {
							@Override
							protected GroupColumn createGroupColumn() {
								return new GroupColumn() {
									@Override
									protected int getCount(Object element) {
										return ((IMagicCard) element).getUniqueCount();
									}
								};
							}
						};
					}
				};
			}

			@Override
			protected void addStoreChangeListener() {
				// do not add library listener
			}

			@Override
			protected void removeStoreChangeListener() {
				// ignore
			}

			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return DataManager.getCardHandler().getMagicDBFilteredStore();
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
				Location loc = fstore.getLocation();
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						DataManager.getInstance().copyCards(sel.toList(), loc);
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
							page.showView(PrintingsView.ID);
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
					TextPrinter.printHeader((MagicCard) card, ps);
					curset = set;
				}
				MagicCard mc = (MagicCard) card;
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
		manager.add(this.showPrintings);
		manager.add(this.showInstances);
		manager.add(this.edit);
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