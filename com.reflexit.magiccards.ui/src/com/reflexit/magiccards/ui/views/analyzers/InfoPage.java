package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.dialogs.EditDeckPropertiesDialog;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.columns.PriceColumn;
import com.reflexit.magiccards.ui.views.columns.SellerPriceColumn;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;
import com.reflexit.magiccards.ui.widgets.DynamicCombo;

public class InfoPage extends AbstractDeckPage implements IDeckPage {
	private Text text;
	private Label total;
	private Label totalSideboard;
	private Label dbprice;
	String prefix = "Deck";
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	private Label colors;
	private DynamicCombo ownership;
	private Link editButton;
	private Label decktype;
	private Label averagecost;
	private Composite stats;
	private Label maxRepeats;
	private Label loclabel;
	private Label colorsSideboard;
	private Label rarity;
	private DynamicCombo protection;
	private IStorageInfo storageInfo;

	@Override
	public void createPageContents(Composite parent) {
		createTextArea().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).minSize(-1, 40).create());
		createEditButton(getArea())
				.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.END).create());
		createStatsArea(getArea())
				.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.FILL).grab(false, true).create());
	}

	protected Control createEditButton(Composite parent) {
		editButton = new Link(parent, SWT.PUSH);
		editButton.setText("<a>Edit...</a>");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openEdit();
			}
		});
		return editButton;
	}

	private Composite createStatsArea(Composite parent) {
		stats = new Composite(parent, SWT.NONE);
		stats.setLayout(new GridLayout(4, false));
		decktype = createTextLabel("Type: ");
		loclabel = createTextLabel("Location: ");
		ownership = createDynCombo("Ownership: ", null, "Own", "Virtual");
		ownership.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = ownership.getCombo().getText();
				boolean virtual = value.equals("Virtual");
				if (storageInfo.isVirtual() != virtual) {
					storageInfo.setVirtual(virtual);
				}
			}
		});
		protection = createDynCombo("Protection: ",
				"If collection is read only it cannot be modfied, except for unsetting read only flag", "Read Only",
				"Writable");
		protection.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = protection.getCombo().getText();
				boolean b = value.equals("Read Only");
				if (storageInfo.isReadOnly() != b) {
					storageInfo.setReadOnly(b);
				}
			}
		});
		total = createTextLabel("Cards: ");
		totalSideboard = createTextLabel("Cards (Sideboard): ");
		colors = createTextLabel("Colors: ");
		colorsSideboard = createTextLabel("Colors (Sideboard): ");
		averagecost = createTextLabel("Average Mana Cost: ");
		maxRepeats = createTextLabel("Max Repeats: ",
				"How many time each card repeats, excluding basic land (for legality purposes)");
		rarity = createTextLabel("Rarity: ");
		// tree.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		dbprice = createTextLabel("Price: ",
				"Cost of a deck using Online Price column," + " in brackets cost of a deck using User Price column");
		return stats;
	}

	private Label createTextLabel(String string) {
		return createTextLabel(string, null);
	}

	private Label createTextLabel(final String string, String tip) {
		Label label = new Label(stats, SWT.NONE);
		label.setText(string);
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
		Label text = new Label(stats, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		if (tip != null) {
			label.setToolTipText(tip);
			text.setToolTipText(tip);
		}
		return text;
	}

	private DynamicCombo createDynCombo(final String string, String tip, String... values) {
		Label label = new Label(stats, SWT.NONE);
		label.setText(string);
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
		final DynamicCombo text = new DynamicCombo(stats, SWT.READ_ONLY, values);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				text.activateCombo();
			}
		});
		if (tip != null) {
			label.setToolTipText(tip);
			text.setToolTipText(tip);
		}
		return text;
	}

	private Group createTextArea() {
		Group group = new Group(getArea(), SWT.NONE);
		group.setText("Description");
		group.setLayout(new GridLayout());
		text = new Text(group, SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		text.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				openEdit();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// ignore
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openEdit();
			}
		});
		text.setLayoutData(GridDataFactory.fillDefaults().hint(600, 80).grab(true, false).create());
		return group;
	}

	protected void setComment(String text2) {
		IStorageInfo si = getStorageInfo();
		if (si == null)
			return;
		if (!text2.equals(si.getComment()))
			si.setComment(text2);
	}

	@Override
	public void activate() {
		super.activate();
		storageInfo = getStorageInfo();
		String type = null;
		if (storageInfo != null) {
			String comment = storageInfo.getComment();
			if (comment != null)
				text.setText(comment);
			type = storageInfo.getType();
			protection.setText(storageInfo.isReadOnly() ? "Read Only" : "Writable");
		}
		Location location = store.getLocation();
		Location sideboard = location.toSideboard();
		ICardStore<IMagicCard> sideboardStore = DataManager.getInstance().getCardStore(sideboard);
		ICardStore<IMagicCard> mainStore = DataManager.getInstance().getCardStore(location.toMainDeck());
		if (mainStore == null)
			mainStore = store;
		totalSideboard.setText(String.valueOf(getCount(sideboardStore)));
		total.setText(String.valueOf(getCount(mainStore)));
		prefix = (type != null && type.equals(IStorageInfo.DECK_TYPE)) ? "Deck" : "Collection";
		if (location.isSideboard()) {
			prefix = "Sideboard";
		}
		CardGroup group = CardStoreUtils.buildGroup(mainStore, sideboardStore);
		loclabel.setText(location.toString());
		String sp = new SellerPriceColumn().getText(group);
		String up = new PriceColumn().getText(group);
		dbprice.setText(sp + " (" + up + ")");
		colors.setImage(SymbolConverter.buildCostImage(CardStoreUtils.buildColors(mainStore)));
		colorsSideboard.setImage(SymbolConverter.buildCostImage(CardStoreUtils.buildColors(sideboardStore)));
		ownership.setText(store.isVirtual() ? "Virtual" : "Own");
		decktype.setText(prefix);
		List<? extends ICard> childrenList = group.getChildrenList();
		maxRepeats.setText(String.valueOf(CardStoreUtils.getMaxRepeats(childrenList)));
		CardGroup types = CardStoreUtils.buildTypeGroups(childrenList);
		CardGroup top = (CardGroup) types.getChildAtIndex(0);
		CardGroup ncre = (CardGroup) top.getChildAtIndex(1);
		CardGroup cre = (CardGroup) top.getChildAtIndex(2);
		if (ncre.getRarity().equals(cre.getRarity())) {
			rarity.setText(ncre.getRarity());
		} else {
			rarity.setText("*");
		}
		int spellCount = ncre.getCount() + cre.getCount();
		if (spellCount > 0) {
			int creCost = CardStoreUtils.getManaCost(cre.expand());
			int ncreCost = CardStoreUtils.getManaCost(ncre.expand());
			averagecost.setText(
					String.valueOf((creCost + ncreCost) / (float) spellCount) + " (" + spellCount + " spells)");
		}
		getArea().layout(true);
	}

	private void openEdit() {
		try {
			if (new EditDeckPropertiesDialog(editButton.getShell(), getStorageInfo()).open() == Window.OK) {
				activate();
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}
}
