package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.text.DecimalFormat;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.dialogs.EditDeckPropertiesDialog;
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
	private Button editButton;
	private Label decktype;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		getArea().setLayout(new GridLayout(4, false));
		createStatsArea();
		createTextArea();
		editButton = new Button(getArea(), SWT.PUSH);
		editButton.setText("Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (new EditDeckPropertiesDialog(editButton.getShell(), getInfo()).open() == Window.OK) {
						updateFromStore();
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		});
		return getArea();
	}

	private void createStatsArea() {
		decktype = createTextLabel("Type: ");
		total = createTextLabel("Total Cards: ");
		dbprice = createTextLabel("Cost: ");
		dbprice.setToolTipText("Cost of a deck using Seller's Price column,"
		        + " in brackets cost of a deck using User's Price column");
		colors = createTextLabel("Colors: ");
		ownership = createTextLabel("Ownership: ");
	}

	private Label createTextLabel(String string) {
		Label label = new Label(getArea(), SWT.NONE);
		label.setText(string);
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
		Label text = new Label(getArea(), SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		return text;
	}

	private void createTextArea() {
		Group group = new Group(getArea(), SWT.NONE);
		group.setText("Description");
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = ((GridLayout) getArea().getLayout()).numColumns;
		group.setLayoutData(gd);
		group.setLayout(new GridLayout());
		text = new Text(group, SWT.WRAP | SWT.READ_ONLY);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setComment(text.getText());
			}
		});
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	protected void setComment(String text2) {
		IStorageInfo si = getInfo();
		if (si == null)
			return;
		si.setComment(text2);
	}

	private IStorageInfo getInfo() {
		getCardStore();
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
	public void updateFromStore() {
		getCardStore();
		if (store == null)
			return;
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
		colors.setText("" + CardStoreUtils.buildColors(store));
		ownership.setText(store.isVirtual() ? "Virtual" : "Own");
		decktype.setText(prefix);
	}
}
