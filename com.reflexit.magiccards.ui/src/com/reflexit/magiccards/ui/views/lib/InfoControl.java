package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.text.DecimalFormat;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

public class InfoControl extends AbstractDeckPage implements IDeckPage {
	private Text text;
	private Label total;
	private Label dbprice;
	String prefix = "Deck";
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	private Label colors;

	@Override
	public Composite createContents(Composite parent) {
		area = super.createContents(parent);
		area.setLayout(new GridLayout(2, false));
		createStatsArea();
		createTextArea();
		return area;
	}

	private void createStatsArea() {
		total = createTextLabel("Total Cards: ");
		dbprice = createTextLabel(prefix + " cost: ");
		dbprice.setToolTipText("Cost of a deck using Seller's Price column,"
		        + " in brackets cost of a deck using User's Price column");
		colors = createTextLabel("Colors: ");
	}

	private Label createTextLabel(String string) {
		Label label = new Label(area, SWT.NONE);
		label.setText(string);
		Label text = new Label(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		return text;
	}

	private void createTextArea() {
		Label label = new Label(area, SWT.NONE);
		label.setText(prefix + " description:");
		text = new Text(area, SWT.WRAP | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		text.setLayoutData(gd);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setComment(text.getText());
			}
		});
	}

	protected void setComment(String text2) {
		IStorageInfo si = getInfo();
		if (si == null)
			return;
		si.setComment(text2);
	}

	private IStorageInfo getInfo() {
		if (store instanceof IStorageContainer) {
			IStorage storage = ((IStorageContainer) store).getStorage();
			if (storage instanceof IStorageInfo) {
				IStorageInfo si = ((IStorageInfo) storage);
				return si;
			}
		}
		return null;
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		super.setFilteredStore(store);
		IStorageInfo si = getInfo();
		if (si == null)
			return;
		prefix = (getInfo().getType().equals(IStorageInfo.DECK_TYPE)) ? "Deck" : "Collection";
	}

	@Override
	public void updateFromStore() {
		IStorageInfo si = getInfo();
		if (si != null) {
			String comment = si.getComment();
			if (comment != null)
				text.setText(comment);
		}
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
		colors.setText("" + CardStoreUtils.buildColors(store));
	}
}
