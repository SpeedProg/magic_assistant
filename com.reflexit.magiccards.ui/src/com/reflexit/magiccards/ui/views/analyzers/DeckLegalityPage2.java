package com.reflexit.magiccards.ui.views.analyzers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.core.sync.ParseGathererLegality;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.CostColumn;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LegalityColumn;

public class DeckLegalityPage2 extends AbstractDeckListPage {
	private IFilteredCardStore fstore;
	private static final String DEFAULT_FORMAT = "Standard";
	private String format = DEFAULT_FORMAT;
	private ImageAction load;
	private LegalityMap deckLegalities = new LegalityMap(true); // map format->legality
	private Combo comboLegality;
	protected TreeViewer tree;
	private ImageAction refresh;
	private Composite info;
	private Label total;
	private Label totalSideboard;
	private Label colors;
	private Label colorsSideboard;
	private Label maxRepeats;
	private Label rarity;

	class ImageAction extends Action {
		public ImageAction(String name, String iconKey, int style) {
			super(name, style);
			setImageDescriptor(MagicUIActivator.getImageDescriptor(iconKey));
			setToolTipText(name);
		}
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		area.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(area, SWT.HORIZONTAL);
		createInfoPanel(sashForm);
		createCardsTree(sashForm);
		sashForm.setWeights(new int[] { 30, 70 });
		makeActions();
		return area;
	}

	private void createInfoPanel(Composite parent) {
		info = new Composite(parent, SWT.BORDER);
		info.setLayout(new GridLayout(2, false));
		Button update = new Button(info, SWT.PUSH);
		update.setText("Load Legality...");
		update.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performUpdate();
			}
		});
		update.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		createBlueLabel("Format");
		comboLegality = createLegalityCombo(info);
		comboLegality.setLayoutData(GridDataFactory.fillDefaults().create());
		total = createTextLabel("Cards: ");
		totalSideboard = createTextLabel("Cards (Sideboard): ");
		colors = createTextLabel("Colors: ");
		colorsSideboard = createTextLabel("Colors (Sideboard): ");
		maxRepeats = createTextLabel("Max Repeats: ", "How many time each card repeats, excluding basic land (for legality purposes)");
		rarity = createTextLabel("Rarity: ");
	}

	protected Combo createLegalityCombo(Composite parent) {
		final Combo comboLegality = new Combo(parent, SWT.READ_ONLY);
		comboLegality.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setFormat(comboLegality.getText());
			}
		});
		reloadLegalityCombo(comboLegality);
		return comboLegality;
	}

	private Label createTextLabel(String string) {
		return createTextLabel(string, null);
	}

	private Label createTextLabel(String string, String tip) {
		Label label = createBlueLabel(string);
		Label text = new Label(info, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		if (tip != null) {
			label.setToolTipText(tip);
			text.setToolTipText(tip);
		}
		return text;
	}

	protected Label createBlueLabel(String string) {
		Label label = new Label(info, SWT.NONE);
		label.setText(string);
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
		return label;
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.load);
		manager.add(this.refresh);
		// super.fillLocalToolBar(manager);
	}

	protected void makeActions() {
		this.load = new ImageAction("Load Legality", "icons/clcl16/web_sync.gif", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				performUpdate();
			}
		};
		this.refresh = new ImageAction("Refresh", "icons/clcl16/refresh.gif", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				activate();
			}
		};
	}

	@Override
	public void activate() {
		super.activate();
		setFStore();
		IStorageInfo storageInfo = getStorageInfo();
		if (storageInfo != null) {
			String f = storageInfo.getProperty("format");
			if (f != null) {
				format = f;
			} else {
				format = DEFAULT_FORMAT;
			}
			reloadLegalityCombo(comboLegality);
		}
		Format.valueOf(format).checkLegality(fstore.getCardStore(), null);
		ICardGroup root = fstore.getCardGroupRoot();
		tree.setInput(root);
		tree.refresh(true);
		updateInfo();
	}

	private void updateInfo() {
		CardStoreUtils.CardStats stats = new CardStoreUtils.CardStats(store);
		totalSideboard.setText(String.valueOf(stats.sideboardCount));
		total.setText(String.valueOf(stats.mainCount));
		colors.setImage(SymbolConverter.buildCostImage(stats.mainColors));
		colorsSideboard.setImage(SymbolConverter.buildCostImage(stats.sideboardColors));
		maxRepeats.setText(String.valueOf(stats.maxRepeats));
		CardGroup types = CardStoreUtils.buildTypeGroups(store);
		CardGroup top = (CardGroup) types.getChildAtIndex(0);
		CardGroup spell = (CardGroup) top.getChildAtIndex(1);
		int spellCount = spell.getCount();
		if (spellCount > 0) {
			rarity.setText(spell.getRarity());
		}
	}

	@Override
	public void setFilteredStore(IFilteredCardStore fstore) {
		super.setFilteredStore(fstore);
		setFStore();
	}

	public void setFStore() {
		if (getCardStore() == null)
			return;
		MemoryFilteredCardStore<IMagicCard> mstore = new MemoryFilteredCardStore<IMagicCard>();
		Location loc = store.getLocation();
		MagicCardFilter filter = (MagicCardFilter) view.getFilter().clone();
		ICardStore mainStore = DataManager.getCardStore(loc.toMainDeck());
		ICardStore sideStore = DataManager.getCardStore(loc.toSideboard());
		mstore.addAll(mainStore);
		if (sideStore != null)
			mstore.addAll(sideStore);
		mstore.setLocation(loc.toMainDeck());
		filter.getSortOrder().setSortField(MagicCardField.LEGALITY, true);
		filter.getSortOrder().setSortField(MagicCardFieldPhysical.SIDEBOARD, false);
		filter.setGroupField(MagicCardFieldPhysical.SIDEBOARD);
		mstore.update(filter);
		this.fstore = mstore;
	}

	public void setFormat(final String f) {
		format = f;
		IStorageInfo storageInfo = getStorageInfo();
		if (storageInfo != null) {
			storageInfo.setProperty("format", f);
		}
		activate();
	}

	protected ICardField[] getGroupFields() {
		return null;
	}

	@Override
	public void createCardsTree(Composite parent) {
		super.createCardsTree(parent);
		tree = (TreeViewer) getListControl().getManager().getViewer();
		tree.setAutoExpandLevel(2);
	}

	@Override
	public GroupListControl doGetMagicCardListControl() {
		return new GroupListControl(view) {
			@Override
			public IMagicColumnViewer createViewerManager() {
				return new GroupTreeManager(getPreferencePageId()) {
					@Override
					protected void createCustomColumns(ArrayList<AbstractColumn> columns) {
						createPageCustomColumns(columns);
					}
				};
			}
		};
	}

	protected void createPageCustomColumns(List<AbstractColumn> columns) {
		columns.add(new GroupColumn(false, true, false));
		columns.add(new CountColumn() {
			@Override
			public Color getBackground(Object element) {
				if (element instanceof ICardCountable) {
					int count = ((ICardCountable) element).getCount();
					LegalityMap legalityMap = ((IMagicCard) element).getLegalityMap();
					if (legalityMap == null)
						return null;
					Legality legality = legalityMap.get(format);
					boolean legal = Format.valueOf(format).isCountLegal(count, legality);
					if (!legal)
						return MagicUIActivator.COLOR_PINKINSH;
				}
				return super.getBackground(element);
			}
		});
		columns.add(new CostColumn());
		// columns.add(new SetColumn());
		columns.add(new LegalityColumn() {
			@Override
			public Color getBackground(Object element) {
				if (element instanceof IMagicCard) {
					LegalityMap legalityMap = ((IMagicCard) element).getLegalityMap();
					if (legalityMap == null)
						return null;
					Legality legality = legalityMap.get(format);
					switch (legality) {
						case UNKNOWN:
						case NOT_LEGAL:
						case BANNED:
							return MagicUIActivator.COLOR_PINKINSH;
						case LEGAL:
							return MagicUIActivator.COLOR_GREENISH;
						case RESTRICTED:
							return Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW);
					}
				}
				return super.getBackground(element);
			}
		});
		columns.add(new GenColumn(MagicCardFieldPhysical.ERROR, "Error"));
	}

	protected void performUpdate() {
		if (store != null) {
			Job job = new Job("Calculating Legality") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Calculating Legality", 100);
					Map<Integer, LegalityMap> cardLegalities = calculateCardLegalities(new SubProgressMonitor(monitor, 90));
					deckLegalities = LegalityMap.calculateDeckLegality(cardLegalities.values());
					getControl().getDisplay().asyncExec(new Runnable() {
						public void run() {
							// tree.setInput(deckInput);
							reloadLegalityCombo(comboLegality);
							activate();
						}
					});
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	private Map<Integer, LegalityMap> calculateCardLegalities(IProgressMonitor monitor) {
		try {
			return ParseGathererLegality.cardSetLegality(fstore.getCardStore(), new CoreMonitorAdapter(monitor));
		} catch (IOException e) {
			MessageDialog.openError(getControl().getShell(), "Error", e.getMessage());
			return null;
		}
	}

	protected void reloadLegalityCombo(Combo comboLegality) {
		comboLegality.removeAll();
		for (final Format f : deckLegalities.keySet()) {
			comboLegality.add(f.name());
		}
		if (format.equals(DEFAULT_FORMAT))
			comboLegality.add(DEFAULT_FORMAT);
		comboLegality.setText(format);
	}
}
