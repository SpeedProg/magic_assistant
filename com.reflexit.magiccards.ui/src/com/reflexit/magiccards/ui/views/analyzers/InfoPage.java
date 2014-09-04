package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.dialogs.EditDeckPropertiesDialog;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class InfoPage extends AbstractDeckPage implements IDeckPage {
	private Text text;
	private Label total;
	private Label totalSideboard;
	private Label dbprice;
	String prefix = "Deck";
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	private Label colors;
	private Label ownership;
	private Link editButton;
	private Label decktype;
	private Label averagecost;
	private Composite stats;
	private Label maxRepeats;
	private Label loclabel;
	private Label colorsSideboard;
	private Label rarity;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		getArea().setLayout(new GridLayout(3, false));
		createStatsArea().setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.FILL).grab(false, true).create());
		createTextArea().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		createEditButton(stats).setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.BEGINNING, SWT.END).create());
		return getArea();
	}

	protected Control createEditButton(Composite parent) {
		editButton = new Link(parent, SWT.PUSH);
		editButton.setText("<a>Edit...</a>");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (new EditDeckPropertiesDialog(editButton.getShell(), getStorageInfo()).open() == Window.OK) {
						activate();
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		});
		return editButton;
	}

	@Override
	public String getStatusMessage() {
		return "";
	}

	private Composite createStatsArea() {
		stats = new Composite(getArea(), SWT.NONE);
		stats.setLayout(new GridLayout(2, false));
		decktype = createTextLabel("Type: ");
		loclabel = createTextLabel("Location: ");
		ownership = createTextLabel("Ownership: ");
		total = createTextLabel("Cards: ");
		totalSideboard = createTextLabel("Cards (Sideboard): ");
		colors = createTextLabel("Colors: ");
		colorsSideboard = createTextLabel("Colors (Sideboard): ");
		averagecost = createTextLabel("Average Mana Cost: ");
		maxRepeats = createTextLabel("Max Repeats: ", "How many time each card repeats, excluding basic land (for legality purposes)");
		rarity = createTextLabel("Rarity: ");
		// tree.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		dbprice = createTextLabel("Price: ", "Cost of a deck using Seller's Price column,"
				+ " in brackets cost of a deck using User's Price column");
		return stats;
	}

	private Label createTextLabel(String string) {
		return createTextLabel(string, null);
	}

	private Label createTextLabel(String string, String tip) {
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

	private Group createTextArea() {
		Group group = new Group(getArea(), SWT.NONE);
		group.setText("Description");
		GridData gd = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd);
		group.setLayout(new GridLayout());
		text = new Text(group, SWT.WRAP | SWT.READ_ONLY);
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setComment(text.getText());
			}
		});
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		return group;
	}

	protected void setComment(String text2) {
		IStorageInfo si = getStorageInfo();
		if (si == null)
			return;
		si.setComment(text2);
	}

	@Override
	public void activate() {
		super.activate();
		IStorageInfo si = getStorageInfo();
		String type = null;
		if (si != null) {
			String comment = si.getComment();
			if (comment != null)
				text.setText(comment);
			type = si.getType();
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
		float cost = group.getDbPrice();
		float ucost = group.getPrice();
		loclabel.setText(location.toString());
		dbprice.setText("$" + decimalFormat.format(cost) + " ($" + decimalFormat.format(ucost) + ")");
		colors.setImage(SymbolConverter.buildCostImage(CardStoreUtils.buildColors(mainStore)));
		colorsSideboard.setImage(SymbolConverter.buildCostImage(CardStoreUtils.buildColors(sideboardStore)));
		ownership.setText(store.isVirtual() ? "Virtual" : "Own");
		decktype.setText(prefix);
		maxRepeats.setText(String.valueOf(CardStoreUtils.getMaxRepeats(group.getChildrenList())));
		CardGroup types = CardStoreUtils.buildTypeGroups(group.getChildrenList());
		CardGroup top = (CardGroup) types.getChildAtIndex(0);
		CardGroup land = (CardGroup) top.getChildAtIndex(0);
		CardGroup spell = (CardGroup) top.getChildAtIndex(1);
		int spellCount = spell.getCount();
		if (spellCount > 0) {
			rarity.setText(spell.getRarity());
			averagecost.setText(String.valueOf(CardStoreUtils.getManaCost(spell.expand()) / (float) spellCount) + " (" + spellCount
					+ " spells)");
		}
		getArea().layout(true);
	}
}
