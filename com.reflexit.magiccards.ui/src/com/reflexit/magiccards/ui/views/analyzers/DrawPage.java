package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.storage.PlayingDeck;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.LazyTableViewerManager;

public class DrawPage extends AbstractDeckListPage {
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
		public String getStatusMessage() {
			int cards = deckstore.getSize();
			int total = ((ICardCountable) store).getCount();
			int hand = total < 7 ? total : 7;
			return "Drawn " + cards + " of " + total + ". Hand " + (cards - hand + 1);
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
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.draw);
		manager.add(this.shuffle);
		super.fillLocalToolBar(manager);
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		manager.add(draw);
		manager.add(shuffle);
		manager.add(new Separator());
		super.fillContextMenu(manager);
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
				getListControl().reloadData();
			}
		};
	}

	public void runShuffle() {
		deckstore.shuffle();
		deckstore.draw(7);
		getListControl().unsort();
		getListControl().reloadData();
	}

	@Override
	public void createCardsTree(Composite parent) {
		super.createCardsTree(parent);
	}

	@Override
	public DrawListControl doGetMagicCardListControl() {
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
		getListControl().reloadData();
	}
}
