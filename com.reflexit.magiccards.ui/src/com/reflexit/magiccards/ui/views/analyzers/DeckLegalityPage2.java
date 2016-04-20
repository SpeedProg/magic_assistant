/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tym The Enchanter - initial API and implementation
 *    Alena Laskavaia - ui re-design
 *******************************************************************************/
package com.reflexit.magiccards.ui.views.analyzers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
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
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils.CardStats;
import com.reflexit.magiccards.core.sync.ParseGathererLegality;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.actions.ImageAction;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.analyzers.GroupListControl.GroupTreeViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.CostColumn;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GenColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LegalityColumn;

public class DeckLegalityPage2 extends AbstractDeckListPage {
	private static final Format DEFAULT_FORMAT = Format.STANDARD;
	private Format format = DEFAULT_FORMAT;
	private ImageAction load;
	private LegalityMap deckLegalities = LegalityMap.EMPTY; // format->legality
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

	@Override
	public void createPageContents(Composite area) {
		area.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(area, SWT.HORIZONTAL);
		createMainControl(sashForm);
		createInfoPanel(sashForm);
		sashForm.setWeights(new int[] { 75, 25 });
		makeActions();
		setQuickFilterVisible(false);
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
		update.setText("Check Deck Legality Online...");
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
		maxRepeats = createTextLabel("Max Repeats: ",
				"How many time each card repeats, excluding basic land (for legality purposes)");
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

	@Override
	protected void makeActions() {
		super.makeActions();
		this.load = new ImageAction("Check Legality Online", "icons/clcl16/software_update.png", () -> performUpdate());
		refresh = new RefreshAction(this::refresh);
	}

	@Override
	public void refresh() {
		setFStore();
		deckLegalities = LegalityMap.calculateDeckLegality((ICardStore) fstore.getCardStore());
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
		refreshViewer();
	}

	@Override
	public void activate() {
		super.activate();
		refresh();
	}

	private void updateInfo() {
		stats = new CardStoreUtils.CardStats(getCardStore());
		totalSideboard.setText(String.valueOf(stats.sideboardCount));
		total.setText(String.valueOf(stats.mainCount));
		colors.setImage(SymbolConverter.buildCostImage(stats.mainColors));
		colorsSideboard.setImage(SymbolConverter.buildCostImage(stats.sideboardColors));
		maxRepeats.setText(String.valueOf(stats.maxRepeats));
		CardGroup types = CardStoreUtils.buildTypeGroups(getCardStore());
		CardGroup top = (CardGroup) types.getChildAtIndex(0);
		CardGroup ncre = (CardGroup) top.getChildAtIndex(1);
		CardGroup cre = (CardGroup) top.getChildAtIndex(2);
		String ncreRarity = ncre.getRarity();
		String creRarity = cre.getRarity();
		if (ncreRarity != null && ncreRarity.equals(creRarity)) {
			rarity.setText(ncreRarity);
		} else if (ncreRarity == null && creRarity != null) {
			rarity.setText(creRarity);
		} else {
			rarity.setText("*");
		}
		totalDeco.updateVisibility();
		maxRepeastDeco.updateVisibility();
	}

	public void setFStore() {
		if (getCardStore() == null)
			return;
		MemoryFilteredCardStore<ICard> mstore = new MemoryFilteredCardStore<>();
		Location loc = getCardStore().getLocation();
		MagicCardFilter filter = (MagicCardFilter) getDeckView().getFilter().clone();
		ICardStore mainStore = DataManager.getInstance().getCardStore(loc.toMainDeck());
		ICardStore sideStore = DataManager.getInstance().getCardStore(loc.toSideboard());
		if (mainStore != null)
			mstore.getCardStore().addAll(mainStore.getCards());
		if (sideStore != null)
			mstore.getCardStore().addAll(sideStore.getCards());
		mstore.setLocation(loc.toMainDeck());
		filter.getSortOrder().setSortField(MagicCardField.LEGALITY, true);
		filter.getSortOrder().setSortField(MagicCardField.SIDEBOARD, false);
		filter.setGroupFields(MagicCardField.SIDEBOARD);
		mstore.update(filter);
		this.fstore = mstore;
	}

	public void setFormat(final String f) {
		format = Format.valueOf(f);
		IStorageInfo storageInfo = getStorageInfo();
		if (storageInfo != null) {
			storageInfo.setProperty("format", f);
		}
		refresh();
	}

	protected ICardField[] getGroupFields() {
		return null;
	}

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		tree = new GroupTreeViewer(getPreferencePageId(), parent) {
			@Override
			protected void createCustomColumns(List<AbstractColumn> columns) {
				createPageCustomColumns(columns);
			}
		};
		tree.setAutoExpandLevel(2);
		return (IMagicColumnViewer) tree;
	}

	@Override
	protected String getPreferencePageId() {
		return null;
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
					default:
						break;
					}
				}
				return super.getBackground(element);
			}
		});
		columns.add(new GenColumn(MagicCardField.ERROR, "Error") {
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
		if (getCardStore() != null) {
			Job job = new Job("Calculating Legality") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Calculating Legality", 100);
					calculateCardLegalities(new SubProgressMonitor(monitor, 90));
					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refresh();
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
			return ParseGathererLegality.cardSetLegality((ICardStore) fstore.getCardStore(),
					new CoreMonitorAdapter(monitor));
		} catch (Exception e) {
			WaitUtils.syncExec(() -> MessageDialog.openError(getControl().getShell(), "Error", e.getMessage()));
			return null;
		}
	}

	protected void reloadLegalityCombo(Combo comboLegality) {
		comboLegality.removeAll();
		Map<Format, Legality> deckMap = this.deckLegalities.mapOfLegality();
		for (final Format f : deckMap.keySet()) {
			String label = getFormatLabel(f, deckMap.get(f));
			comboLegality.add(label);
			comboLegality.setData(label, f.name());
		}
		comboLegality.setText(getFormatLabel(format, deckMap.get(format)));
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
		String err = format.validateLegality((ICardStore) fstore.getCardStore(), stats);
		if (err == null)
			return "Format: " + format.name() + " is legal";
		else
			return "Format: " + format.name() + " - " + err;
	}
}
