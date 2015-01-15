package com.reflexit.magiccards.ui.views.analyzers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardGame.MagicCardGameField;
import com.reflexit.magiccards.core.model.MagicCardGame.Zone;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.storage.PlayingDeck;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.LazyTableViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CostColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.PowerColumn;
import com.reflexit.magiccards.ui.views.columns.StringEditingSupport;
import com.reflexit.magiccards.ui.views.columns.TypeColumn;

public class DrawPage extends AbstractDeckListPage {
	private PlayingDeck playdeck = new PlayingDeck(new MemoryCardStore());
	private Action shuffle;
	private Action reset;
	private Action draw;
	protected ColumnCollection columns;
	private Action play;
	private Action returnh;
	private ZoneAction libtop;
	private ZoneAction exile;
	private ZoneAction kill;
	private Action scry;
	private ZoneAction libbottom;
	private ImageAction showlib;
	private ImageAction unsort;
	private ImageAction showgrave;
	private ImageAction showexile;
	private ImageAction newturn;
	private ImageAction tap;
	private ImageAction mulligan;
	public static final String ID = DrawPage.class.getName();

	public DrawPage() {
		this.columns = new ColumnCollection() {
			@Override
			protected void createColumns(List<AbstractColumn> columns) {
				columns.add(new GroupColumn(true, true, false));
				columns.add(new GenColumn(MagicCardGameField.DRAWID, "DrawId") {
					@Override
					public int getColumnWidth() {
						return 20;
					}
				});
				columns.add(new CostColumn());
				columns.add(new TypeColumn());
				columns.add(new PowerColumn(MagicCardField.POWER, "P", "Power"));
				columns.add(new PowerColumn(MagicCardField.TOUGHNESS, "T", "Toughness"));
				columns.add(new GenColumn(MagicCardGameField.ZONE, "Zone"));
				columns.add(new GenColumn(MagicCardGameField.TAPPED, "Tapped"));
				columns.add(new GenColumn(MagicCardGameField.NOTE, "Notes") {
					@Override
					public EditingSupport getEditingSupport(final ColumnViewer viewer) {
						return new StringEditingSupport(viewer, this);
					}
				});
			}
		};
	}

	private final class DrawListControl extends AbstractMagicCardsListControl {
		private DrawListControl(AbstractCardsView abstractCardsView) {
			super(abstractCardsView);
		}

		@Override
		public IMagicColumnViewer createViewerManager() {
			return new LazyTableViewerManager(getId()) {
				@Override
				public ColumnCollection getColumnsCollection() {
					return DrawPage.this.columns;
				}
			};
		}

		@Override
		protected final void updateStatus() {
			setStatus(getStatusMessage());
			setWarning(false);
		}

		@Override
		protected String getPreferencePageId() {
			return getClass().getName();
		}

		@Override
		public String getStatusMessage() {
			if (store == null)
				return "";
			int cards = playdeck.countDrawn();
			int total = ((ICardCountable) store).getCount();
			String res = "Drawn " + cards + " of " + total + ". Turn " + playdeck.getTurn() + ".";
			for (Zone zone : Zone.values()) {
				res += zoneStatus(zone);
			}
			return res;
		}

		private String zoneStatus(Zone zone) {
			int g = playdeck.countInZone(zone);
			String zoneStr = "";
			if (g != 0)
				zoneStr = " " + zone.getLabel() + " " + g + ".";
			return zoneStr;
		}

		@Override
		public IFilteredCardStore doGetFilteredStore() {
			return playdeck;
		}

		@Override
		public void fillContextMenu(IMenuManager ma) {
			// ma.add(actionShowPrefs);
		}

		@Override
		public void fillLocalToolBar(IToolBarManager mm) {
			// mm.add(actionShowPrefs);
			mm.add(actionShowFind);
		}

		@Override
		public void fillLocalPullDown(IMenuManager mm) {
			mm.add(actionShowFind);
		}

		@Override
		protected void loadInitial() {
			setQuickFilterVisible(false);
		}
	}

	public String getId() {
		return ID;
	}

	@Override
	public void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.unsort);
		manager.add(this.shuffle);
		manager.add(this.mulligan);
		manager.add(reset);
		manager.add(new Separator());
		manager.add(newturn);
		manager.add(draw);
		manager.add(scry);
		manager.add(new Separator());
		manager.add(showlib);
		manager.add(showgrave);
		manager.add(showexile);
		manager.add(new Separator());
		super.fillLocalPullDown(manager);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(showlib);
		manager.add(showgrave);
		manager.add(showexile);
		manager.add(new Separator());
		manager.add(newturn);
		manager.add(reset);
		super.fillLocalToolBar(manager);
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		manager.add(play);
		manager.add(returnh);
		manager.add(libtop);
		manager.add(libbottom);
		manager.add(exile);
		manager.add(kill);
		manager.add(tap);
		manager.add(new Separator());
		super.fillContextMenu(manager);
	}

	class ImageAction extends Action {
		public ImageAction(String name, String iconPath, String tooltip) {
			this(name, iconPath, tooltip, IAction.AS_PUSH_BUTTON);
		}

		public ImageAction(String name, String iconPath, String tooltip, int style) {
			super(name, style);
			if (tooltip != null)
				setToolTipText(tooltip);
			if (iconPath != null)
				setImageDescriptor(MagicUIActivator.getImageDescriptor(iconPath));
		}
	}

	class SelectionImageAction extends ImageAction implements ISelectionChangedListener {
		public SelectionImageAction(String name, String iconPath, String tooltip) {
			super(name, iconPath, tooltip);
			getSelectionProvider().addSelectionChangedListener(this);
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			setEnabled(!selection.isEmpty());
		}
	}

	class ZoneAction extends SelectionImageAction {
		protected Zone zone;

		public ZoneAction(Zone zone, String name, String iconPath, String tooltip) {
			super(name, iconPath, tooltip);
			this.zone = zone;
		}

		@Override
		public void run() {
			playdeck.toZone(getCardSelection(), zone);
			getListControl().reloadData();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(playdeck.canZone(getCardSelection(), zone));
		}
	}

	protected void makeActions() {
		this.reset = new ImageAction("New Game", "icons/obj16/hand16.png", "New Game. Shuffle and Draw 7") {
			@Override
			public void run() {
				playdeck.restart();
				playdeck.draw(7);
				getListControl().unsort();
				getListControl().reloadData();
			}
		};
		this.unsort = new ImageAction("Unsort", null, "Remove sort by column") {
			@Override
			public void run() {
				getListControl().unsort();
				getListControl().reloadData();
			}
		};
		this.draw = new ImageAction("Draw", "icons/obj16/one_card16.png", "Draw One") {
			@Override
			public void run() {
				playdeck.draw(1);
				getListControl().reloadData();
			}
		};
		this.newturn = new ImageAction("New Turn", "icons/obj16/one_card16.png", "New Turn (Untap and Draw)") {
			@Override
			public void run() {
				playdeck.newturn();
				getListControl().reloadData();
			}
		};
		this.scry = new ImageAction("Scry", "icons/obj16/hand16.png", "Look at the top card of the library (Scry)") {
			@Override
			public void run() {
				playdeck.scry(1);
				getListControl().reloadData();
			}
		};
		this.showlib = new ImageAction("Show Library", "icons/obj16/lib16.png", null, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				playdeck.showZone(Zone.LIBRARY, isChecked());
				getListControl().reloadData();
			}
		};
		this.showgrave = new ImageAction("Show Graveyard", "icons/clcl16/graveyard.png", null, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				playdeck.showZone(Zone.GRAVEYARD, isChecked());
				getListControl().reloadData();
			}
		};
		this.showexile = new ImageAction("Show Exile", "icons/clcl16/palm16.png", null, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				playdeck.showZone(Zone.EXILE, isChecked());
				getListControl().reloadData();
			}
		};
		this.shuffle = new ImageAction("Suffle Library", "icons/clcl16/shuffle16.png", null) {
			@Override
			public void run() {
				playdeck.shuffle();
				getListControl().reloadData();
			}
		};
		this.mulligan = new ImageAction("Mulligan", null, null) {
			@Override
			public void run() {
				int i = playdeck.countInZone(Zone.HAND) - 1;
				playdeck.restart();
				playdeck.draw(i);
				getListControl().reloadData();
			}
		};
		this.play = new ZoneAction(Zone.BATTLEFIELD, "Play", "icons/clcl16/arrow_right.png", "Put in the battlefield");
		this.returnh = new ZoneAction(Zone.HAND, "Return", "icons/clcl16/arrow_left.png", "Return to hand");
		this.libtop = new ZoneAction(Zone.LIBRARY, "Library Top", "icons/clcl16/arrow_up.png", "Put on top of the library");
		this.libbottom = new ZoneAction(Zone.LIBRARY, "Library Bottom", "icons/clcl16/arrow_down.png", "Put at the bottom of the library") {
			@Override
			public void run() {
				playdeck.toZone(getCardSelection(), zone);
				playdeck.pushback(getCardSelection());
				getListControl().reloadData();
			}
		};
		this.exile = new ZoneAction(Zone.EXILE, "Exile", "icons/clcl16/palm16.png", "Remove from the game (Exile)");
		this.kill = new ZoneAction(Zone.GRAVEYARD, "Kill", "icons/clcl16/graveyard.png", "Put to graveyard");
		this.tap = new ImageAction("Tap", "icons/tap.gif", null) {
			@Override
			public void run() {
				playdeck.tap(getCardSelection(), true);
				getListControl().reloadData();
			}
		};
	}

	private List<IMagicCard> getCardSelection() {
		ISelection selection = getSelectionProvider().getSelection();
		if (!(selection instanceof IStructuredSelection))
			return Collections.EMPTY_LIST;
		return ((IStructuredSelection) selection).toList();
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
		playdeck.setStore(store);
	}

	@Override
	public void activate() {
		super.activate();
		getListControl().reloadData();
	}
}
