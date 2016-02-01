package com.reflexit.magiccards.ui.views.analyzers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardGame.MagicCardGameField;
import com.reflexit.magiccards.core.model.MagicCardGame.Zone;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.storage.PlayingDeck;
import com.reflexit.magiccards.ui.actions.ImageAction;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.LazyTableViewer;
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
	private ImageAction refresh;
	private SpinnerContributionItem spinner;
	public static final String ID = DrawPage.class.getName();

	class SpinnerContributionItem extends ControlContribution {
		private Spinner control;
		private int value = 20;

		public void setValue(int x) {
			value = x;
			if (control != null)
				control.setSelection(x);
		}

		public int getValue() {
			return value;
		}

		protected SpinnerContributionItem(String id) {
			super(id);
		}

		@Override
		protected Control createControl(Composite parent) {
			control = new Spinner(parent, SWT.BORDER);
			control.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					value = control.getSelection();
					control.setToolTipText("Life Counter " + value);
				}
			});
			control.setValues(value, 0, 99, 0, 1, 10);
			control.setToolTipText("Life Counter " + value);
			return control;
		}

		public Spinner getControl() {
			return control;
		}
	}

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

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		return new LazyTableViewer(parent, columns);
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
		if (getCardStore() == null)
			return "";
		int cards = playdeck.countDrawn();
		int total = ((ICardCountable) getCardStore()).getCount();
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
	public ICardStore<IMagicCard> getCardStore() {
		return super.getCardStore();
	}

	@Override
	protected void loadInitial() {
		setQuickFilterVisible(false);
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
		manager.add(refresh);
		// super.fillLocalPullDown(manager);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(spinner);
		manager.add(new Separator());
		manager.add(showlib);
		manager.add(showgrave);
		manager.add(showexile);
		manager.add(new Separator());
		manager.add(newturn);
		manager.add(reset);
		manager.add(new Separator());
		manager.add(actionShowFind);
		manager.add(refresh);
		// super.fillLocalToolBar(manager);
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
		manager.add(actionShowFind);
		// super.fillContextMenu(manager);
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
			fullReload();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(playdeck.canZone(getCardSelection(), zone));
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.reset = new ImageAction("New Game", "icons/obj16/hand16.png", "New Game. Shuffle and Draw 7", () -> {
			playdeck.newGame();
			unsort();
			spinner.setValue(20);
			fullReload();
		});
		this.unsort = new ImageAction("Unsort", null, "Remove sort by column", () -> {
			unsort();
			fullReload();
		});
		this.draw = new ImageAction("Draw", "icons/obj16/one_card16.png", "Draw One", () -> {
			playdeck.draw(1);
			fullReload();
		});
		this.newturn = new ImageAction("New Turn", "icons/obj16/one_card16.png", "New Turn (Untap and Draw)", () -> {
			playdeck.newTurn();
			fullReload();
		});
		this.scry = new ImageAction("Scry", "icons/obj16/hand16.png", "Look at the top card of the library (Scry)",
				() -> {
					playdeck.scry(1);
					fullReload();
				});
		this.showlib = new ImageAction("Show Library", "icons/obj16/lib16.png", null, IAction.AS_CHECK_BOX, () -> {
			playdeck.showZone(Zone.LIBRARY, showlib.isChecked());
			fullReload();
		});
		this.showgrave = new ImageAction("Show Graveyard", "icons/clcl16/graveyard.png", null, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				playdeck.showZone(Zone.GRAVEYARD, isChecked());
				fullReload();
			}
		};
		this.showexile = new ImageAction("Show Exile", "icons/clcl16/palm16.png", null, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				playdeck.showZone(Zone.EXILE, isChecked());
				fullReload();
			}
		};
		this.shuffle = new ImageAction("Suffle Library", "icons/clcl16/shuffle16.png", () -> {
			playdeck.shuffleLibrary();
			fullReload();
		});
		this.mulligan = new ImageAction("Mulligan", null, () -> {
			if (playdeck.getTurn() > 1)
				throw new MagicException("Only can do this on first turn");
			int i = playdeck.countInZone(Zone.HAND) - 1;
			playdeck.restart();
			playdeck.draw(i);
			fullReload();
		});
		this.play = new ZoneAction(Zone.BATTLEFIELD, "Battlefield", "icons/clcl16/arrow_right.png",
				"Put in the battlefield");
		this.returnh = new ZoneAction(Zone.HAND, "Hand", "icons/clcl16/arrow_left.png", "Return to hand");
		this.libtop = new ZoneAction(Zone.LIBRARY, "Library Top", "icons/clcl16/arrow_up.png",
				"Put on top of the library");
		this.libbottom = new ZoneAction(Zone.LIBRARY, "Library Bottom", "icons/clcl16/arrow_down.png",
				"Put at the bottom of the library") {
			@Override
			public void run() {
				playdeck.toZone(getCardSelection(), zone);
				playdeck.pushback(getCardSelection());
				fullReload();
			}
		};
		this.exile = new ZoneAction(Zone.EXILE, "Exile", "icons/clcl16/palm16.png", "Remove from the game (Exile)");
		this.kill = new ZoneAction(Zone.GRAVEYARD, "Graveyard", "icons/clcl16/graveyard.png", "Put to graveyard");
		this.tap = new ImageAction("Tap/Untap", "icons/tap.gif", () -> {
			playdeck.tap(getCardSelection());
			fullReload();
		});
		this.refresh = new RefreshAction(this::fullReload);
		this.spinner = new SpinnerContributionItem("spinner");
	}

	private List<IMagicCard> getCardSelection() {
		ISelection selection = getSelectionProvider().getSelection();
		if (!(selection instanceof IStructuredSelection))
			return Collections.EMPTY_LIST;
		return ((IStructuredSelection) selection).toList();
	}

	@Override
	public void activate() {
		playdeck.setStore(getCardStore());
		contributeToActionBars();
		addListeners();
		fullReload();
	}

	private void fullReload() {
		playdeck.setRefreshRequired(true);
		refresh();
	}
}
