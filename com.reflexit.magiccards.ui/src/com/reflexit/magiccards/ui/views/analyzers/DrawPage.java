package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.storage.PlayingDeck;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.LazyTableViewerManager;

public class DrawPage extends AbstractDeckPage {
	private DrawListControl listControl;
	private PlayingDeck deckstore = new PlayingDeck(new MemoryCardStore());
	private IAction shuffle;
	private Action draw;
	public static final String ID = DrawPage.class.getName();

	private final class DrawListControl extends AbstractMagicCardsListControl {
		private DrawListControl(AbstractCardsView abstractCardsView) {
			super(abstractCardsView);
		}

		@Override
		public IMagicColumnViewer createViewerManager() {
			return new LazyTableViewerManager(getId());
		}

		@Override
		public void unsort() {
			super.unsort();
		}

		@Override
		public String getStatusMessage() {
			int cards = deckstore.getSize();
			return "Drawn " + cards + " of " + ((ICardCountable) store).getCount() + ". Hand " + (cards - 6);
		}

		@Override
		public IFilteredCardStore doGetFilteredStore() {
			return deckstore;
		}

		@Override
		public void fillContextMenu(IMenuManager ma) {
			ma.add(actionShowPrefs);
		}

		@Override
		public void fillLocalToolBar(IToolBarManager mm) {
			mm.add(actionShowPrefs);
			mm.add(actionShowFind);
		}
	}

	public String getId() {
		return ID;
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		// activateActionHandler(view.actionCopy, "org.eclipse.ui.edit.copy");
		// activateActionHandler(view.actionPaste, "org.eclipse.ui.edit.paste");
		// listControl.setGlobalHandlers(bars);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(this.draw);
		manager.add(this.shuffle);
		listControl.fillLocalToolBar(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(draw);
		manager.add(shuffle);
		manager.add(new Separator());
		listControl.fillContextMenu(manager);
	}

	@Override
	protected MenuManager hookContextMenu() {
		MenuManager menuMgr = super.hookContextMenu();
		Control control = listControl.getManager().getControl();
		Menu menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);
		return menuMgr;
	}

	protected void makeActions() {
		this.shuffle = new Action("Shuffle") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/hand16.png"));
				setToolTipText("Shuffle and Draw 7");
			}

			@Override
			public void run() {
				runShuffle();
			}
		};
		this.draw = new Action("Draw") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/one_card16.png"));
				setToolTipText("Draw One");
			}

			@Override
			public void run() {
				deckstore.draw(1);
				listControl.reloadData();
			}
		};
	}

	public void runShuffle() {
		deckstore.shuffle();
		deckstore.draw(7);
		listControl.unsort();
		listControl.reloadData();
	}

	public void createCardsTree(Composite parent) {
		listControl = doGetMagicCardListControl();
		listControl.createPartControl(parent);
		// listControl.getFilter().setGroupFields(getGroupFields());
		ColumnViewer stats = listControl.getManager().getViewer();
		// stats.setContentProvider(new GroupContentProvider());
		stats.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					view.getSite().getSelectionProvider().setSelection(sel);
				}
			}
		});
	}

	private DrawListControl doGetMagicCardListControl() {
		return new DrawListControl(view);
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite p = super.createContents(parent);
		createCardsTree(p);
		makeActions();
		return p;
	}

	@Override
	public void setFilteredStore(IFilteredCardStore fstore) {
		super.setFilteredStore(fstore);
		deckstore.setStore(store);
	}

	@Override
	public void activate() {
		super.activate();
		listControl.reloadData();
	}
}
