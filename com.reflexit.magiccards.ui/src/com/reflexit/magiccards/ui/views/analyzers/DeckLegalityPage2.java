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
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.CardGroup;
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
import com.reflexit.magiccards.core.model.utils.CardStoreUtils.CardStats;
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
	private static final Format DEFAULT_FORMAT = Format.STANDARD;
	private IFilteredCardStore fstore;
	private Format format = DEFAULT_FORMAT;
	private ImageAction load;
	private LegalityMap deckLegalities = new LegalityMap(); // map format->legality
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
	private CheckControlDecoration totalDeco;
	private CardStats stats;
	private CheckControlDecoration maxRepeastDeco;

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
		sashForm.setWeights(new int[] { 25, 75 });
		makeActions();
		return area;
	}

	abstract class CheckControlDecoration extends ControlDecoration {
		public CheckControlDecoration(Control control, int position) {
			super(control, position);
			FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
			Image newImage = registry.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
			setImage(newImage);
			hide();
		}

		public void updateVisibility() {
			String error = validate();
			if (error == null) {
				hide();
			} else {
				setDescriptionText(error);
				show();
			}
		}

		protected abstract String validate();
	}

	private void createInfoPanel(Composite parent) {
		info = new Composite(parent, SWT.BORDER);
		info.setLayout(new GridLayout(2, false));
		Button update = new Button(info, SWT.PUSH);
		update.setText("Load Deck Legality...");
		update.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performUpdate();
			}
		});
		update.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		// createBlueLabel("Format");
		comboLegality = createLegalityCombo(info);
		comboLegality.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		total = createTextLabel("Cards: ");
		totalSideboard = createTextLabel("Cards (Sideboard): ");
		maxRepeats = createTextLabel("Max Repeats: ", "How many time each card repeats, excluding basic land (for legality purposes)");
		colors = createTextLabel("Colors: ");
		colorsSideboard = createTextLabel("Colors (Sideboard): ");
		rarity = createTextLabel("Rarity: ");
		totalDeco = new CheckControlDecoration(total, SWT.LEAD | SWT.CENTER) {
			@Override
			protected String validate() {
				if (stats == null)
					return null;
				String err = format.validateDeckCount(stats.mainCount);
				return err;
			}
		};
		maxRepeastDeco = new CheckControlDecoration(maxRepeats, SWT.LEAD | SWT.CENTER) {
			@Override
			protected String validate() {
				if (stats == null)
					return null;
				String err = format.validateCardCount(stats.maxRepeats);
				return err;
			}
		};
	}

	protected Combo createLegalityCombo(Composite parent) {
		final Combo comboLegality = new Combo(parent, SWT.READ_ONLY);
		comboLegality.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setFormat(getFormat(comboLegality));
			}
		});
		reloadLegalityCombo(comboLegality);
		return comboLegality;
	}

	protected String getFormat(Combo combo) {
		return (String) combo.getData(combo.getText());
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
		deckLegalities = LegalityMap.calculateDeckLegality(fstore.getCardStore());
		IStorageInfo storageInfo = getStorageInfo();
		if (storageInfo != null) {
			String f = storageInfo.getProperty("format");
			if (f != null && f.trim().length() > 0) {
				format = Format.valueOf(f);
			} else {
				format = DEFAULT_FORMAT;
			}
			reloadLegalityCombo(comboLegality);
		}
		updateInfo();
		ICardGroup root = fstore.getCardGroupRoot();
		tree.setInput(root);
		tree.refresh(true);
		getListControl().updateViewer();
	}

	private void updateInfo() {
		stats = new CardStoreUtils.CardStats(store);
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
		totalDeco.updateVisibility();
		maxRepeastDeco.updateVisibility();
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
		format = Format.valueOf(f);
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

			@Override
			public String getStatusMessage() {
				return DeckLegalityPage2.this.getStatusMessage();
			}
		};
	}

	protected void createPageCustomColumns(List<AbstractColumn> columns) {
		columns.add(new GroupColumn(false, true, false));
		columns.add(new CountColumn() {
			@Override
			public Color getBackground(Object element) {
				if (element instanceof IMagicCard) {
					String err = format.validateCardOrGroup((IMagicCard) element);
					if (err != null)
						return MagicUIActivator.COLOR_PINKINSH;
				}
				return super.getBackground(element);
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IMagicCard) {
					String err = format.validateCardOrGroup((IMagicCard) element);
					return err;
				}
				return super.getToolTipText(element);
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
		columns.add(new GenColumn(MagicCardFieldPhysical.ERROR, "Error") {
			@Override
			public int getColumnWidth() {
				return 250;
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IMagicCard) {
					String err = format.validateCardOrGroup((IMagicCard) element);
					return err;
				}
				return super.getToolTipText(element);
			}
		});
	}

	protected void performUpdate() {
		if (store != null) {
			Job job = new Job("Calculating Legality") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Calculating Legality", 100);
					calculateCardLegalities(new SubProgressMonitor(monitor, 90));
					getControl().getDisplay().asyncExec(new Runnable() {
						public void run() {
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
			String label = getFormatLabel(f, deckLegalities.get(f));
			comboLegality.add(label);
			comboLegality.setData(label, f.name());
		}
		comboLegality.setText(getFormatLabel(format, deckLegalities.get(format)));
	}

	private String getFormatLabel(Format f, Legality legality) {
		if (legality == Legality.UNKNOWN)
			return f.name();
		return f.name() + " - " + legality.getLabel();
	}

	@Override
	public String getStatusMessage() {
		if (fstore == null || format == null || stats == null)
			return "";
		String err = format.validateLegality(fstore.getCardStore(), stats);
		if (err == null)
			return "Format: " + format.name() + " is legal";
		else
			return "Format: " + format.name() + " - " + err;
	}
}
