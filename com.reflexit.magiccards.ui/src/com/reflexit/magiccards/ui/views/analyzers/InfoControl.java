package com.reflexit.magiccards.ui.views.analyzers;

import java.text.DecimalFormat;
import java.util.Iterator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.dialogs.EditDeckPropertiesDialog;
import com.reflexit.magiccards.ui.utils.SymbolConverter;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class InfoControl extends AbstractDeckPage implements IDeckPage {
	private Text text;
	private Label total;
	private Label dbprice;
	String prefix = "Deck";
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	private Label colors;
	private Label ownership;
	private Link editButton;
	private Label decktype;
	private Label averagecost;
	private Composite stats;

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
					if (new EditDeckPropertiesDialog(editButton.getShell(), getInfo()).open() == Window.OK) {
						activate();
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		});
		return editButton;
	}

	private Composite createStatsArea() {
		stats = new Composite(getArea(), SWT.NONE);
		stats.setLayout(new GridLayout(2, false));
		decktype = createTextLabel("Type: ");
		total = createTextLabel("Total Cards: ");
		dbprice = createTextLabel("Money Cost: ");
		dbprice.setToolTipText("Cost of a deck using Seller's Price column," + " in brackets cost of a deck using User's Price column");
		colors = createTextLabel("Colors: ");
		ownership = createTextLabel("Ownership: ");
		averagecost = createTextLabel("Average Mana Cost: ");
		// stats.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		return stats;
	}

	private Label createTextLabel(String string) {
		Label label = new Label(stats, SWT.NONE);
		label.setText(string);
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
		Label text = new Label(stats, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
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
			public void modifyText(ModifyEvent e) {
				setComment(text.getText());
			}
		});
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		return group;
	}

	protected void setComment(String text2) {
		IStorageInfo si = getInfo();
		if (si == null)
			return;
		si.setComment(text2);
	}

	private IStorageInfo getInfo() {
		getCardStore();
		IStorage storage = store.getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}

	@Override
	public void activate() {
		super.activate();
		IStorageInfo si = getInfo();
		if (si != null) {
			String comment = si.getComment();
			if (comment != null)
				text.setText(comment);
		}
		String type = getInfo().getType();
		prefix = (type != null && type.equals(IStorageInfo.DECK_TYPE)) ? "Deck" : "Collection";
		if (store instanceof ICardCountable) {
			total.setText(((ICardCountable) store).getCount() + "");
		}
		float cost = 0;
		float ucost = 0;
		for (Iterator iterator = store.iterator(); iterator.hasNext();) {
			MagicCardPhisical elem = (MagicCardPhisical) iterator.next();
			cost += elem.getDbPrice() * elem.getCount();
			ucost += elem.getPrice() * elem.getCount();
			if (elem.getDbPrice() == 0)
				cost += elem.getPrice() * elem.getCount();
		}
		dbprice.setText("$" + decimalFormat.format(cost) + " ($" + decimalFormat.format(ucost) + ")");
		String costs = CardStoreUtils.buildColorsCost(store);
		Image buildCostImage = SymbolConverter.buildCostImage(costs);
		colors.setImage(buildCostImage);
		ownership.setText(store.isVirtual() ? "Virtual" : "Own");
		decktype.setText(prefix);
		float acost = CardStoreUtils.getInstance().getAverageManaCost(store);
		averagecost.setText(String.valueOf(acost));
		getArea().layout(true);
	}
}
