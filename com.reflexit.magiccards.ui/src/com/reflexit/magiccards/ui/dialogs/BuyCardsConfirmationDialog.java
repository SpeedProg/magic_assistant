package com.reflexit.magiccards.ui.dialogs;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.jobs.LoadingPricesJob;
import com.reflexit.magiccards.ui.preferences.PriceProviderManager;

public class BuyCardsConfirmationDialog extends TitleAreaDialog {
	private GridDataFactory buttonGridData;
	private int totalSize;
	private int filSize;
	private int selSize;
	public static final int USE_SELECTION = 1;
	public static final int USE_FILTER = 2;
	public static final int USE_ALL = 3;
	private static final String ID = BuyCardsConfirmationDialog.class.getName();
	private Combo priceProviderCombo;
	private IFilteredCardStore filteredStore;
	private IStructuredSelection selection;
	private Label epri;

	public BuyCardsConfirmationDialog(Shell shell, IStructuredSelection selection,
			IFilteredCardStore filteredStore) {
		super(shell);
		this.selSize = selection.size();
		this.filSize = filteredStore.getSize();
		this.totalSize = filteredStore.getCardStore().size();
		this.selection = selection;
		this.filteredStore = filteredStore;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Buy Cards Confirmation");
		if (selSize > 0 && selSize != filSize)
			listChoice = USE_SELECTION;
		else if (filSize != totalSize)
			listChoice = USE_FILTER;
		else
			listChoice = USE_ALL;
		String cards = "";
		if (USE_SELECTION == listChoice)
			cards += "Selected " + selSize + " cards. ";
		if (USE_FILTER == listChoice)
			cards += "Visible " + filSize + " out of unique " + totalSize + ".";
		else
			cards += "Total of " + totalSize + " unique cards.";
		setTitle("Cards Choice...");
		setMessage("Choose which cards to buy. " + cards);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite panel = new Composite(area, SWT.NONE);
		panel.setLayout(new GridLayout(1, false));
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonGridData = GridDataFactory.fillDefaults();
		createListChoiceGroup(panel);
		createFieldsGroup(panel);
		restoreWidgetValues();
		return area;
	}

	private String[] getPriceProviders() {
		PriceProviderManager ppm = PriceProviderManager.getInstance();
		Collection<IPriceProvider> providers = ppm.getProviders();
		String[] res = new String[providers.size()];
		int i = 0;
		for (Iterator iterator = providers.iterator(); iterator.hasNext(); i++) {
			IPriceProviderStore prov = (IPriceProviderStore) iterator.next();
			res[i] = prov.getName();
		}
		return res;
	}

	protected void createFieldsGroup(Composite panel) {
		Composite checkArea = new Composite(panel, SWT.BORDER);
		checkArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout la = new GridLayout();
		checkArea.setLayout(la);
		la.marginHeight = 0;
		la.marginWidth = 0;
		createPriceFields(checkArea);
		epri = new Label(checkArea, SWT.NONE);
		epri.setLayoutData(GridDataFactory.fillDefaults().create());
		Button b = new Button(checkArea, SWT.PUSH);
		b.setText("Reload prices from Web");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LoadingPricesJob job = new LoadingPricesJob("Loading prices from "
						+ priceProviderCombo.getText(), getListAsIterable());
				job.setUser(true);
				job.setSystem(false);
				job.schedule();
				job.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						updateEstimatedPrice();
					}
				});
			}
		});
		updateEstimatedPrice();
		Label text = new Label(checkArea, SWT.NONE);
		text.setText("Disclaimer: After you press OK button you will be redirected to the web-site\n"
				+ "where you can complete your order using selected price provider.\n" //
				+ "That web-site will be responsible for your order not this software.\n" //
				+ "Please always double check your cart - names, sets, couns and condition of cards"); //
		text.setLayoutData(GridDataFactory.fillDefaults().create());
	}

	DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	private Button b1;
	private Button b2;
	private Button b3;

	protected void updateEstimatedPrice() {
		float cost = 0;
		int unknw = 0;
		Iterator listIterator = getListIterator();
		for (Iterator iterator = listIterator; iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			int count = 1;
			if (card instanceof ICardCountable)
				count = ((ICardCountable) card).getCount();
			float price = card.getDbPrice();
			cost += price * count;
			if (price == 0)
				unknw++;
		}
		final float fcost = cost;
		final int funknown = unknw;
		epri.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				epri.setText("Estimated price: $" + decimalFormat.format(fcost) + ". Not found cards: "
						+ funknown + ".");
			}
		});
	}

	private Iterator getListIterator() {
		switch (listChoice) {
			case USE_SELECTION:
				return selection.iterator();
			case USE_FILTER:
				return filteredStore.iterator();
			case USE_ALL:
				return filteredStore.getCardStore().iterator();
			default:
				return null;
		}
	}

	public Iterable getListAsIterable() {
		switch (listChoice) {
			case USE_SELECTION:
				return selection.toList();
			case USE_FILTER:
				return filteredStore;
			case USE_ALL:
				return filteredStore.getCardStore();
			default:
				return null;
		}
	}

	protected void createPriceFields(Composite buttons) {
		priceProviderCombo = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		priceProviderCombo.setItems(getPriceProviders());
		priceProviderCombo.setText(PriceProviderManager.getInstance().getProviderName());
		priceProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = priceProviderCombo.getText();
				if (text.length() > 0)
					PriceProviderManager.getInstance().setProviderName(text);
				updateEstimatedPrice();
			}
		});
		priceProviderCombo.setLayoutData(GridDataFactory.fillDefaults().create());
	}

	protected void createListChoiceGroup(Composite panel) {
		Composite group = panel;
		// Group group = new Group(panel, SWT.NONE);
		// group.setLayout(new GridLayout());
		// group.setLayoutData(new GridData(GridData.FILL_BOTH));
		b1 = createRadioButton(group, USE_SELECTION, "Selected cards", selSize);
		b2 = createRadioButton(group, USE_FILTER, "Only cards in the filtered list", filSize);
		b3 = createRadioButton(group, USE_ALL, "All cards in the collection", totalSize);
		if (filSize == selSize) {
			b1.setEnabled(false);
		}
		if (filSize == totalSize) {
			b2.setEnabled(false);
		}
	}

	private int listChoice;

	public int getListChoice() {
		return listChoice;
	}

	private Button createRadioButton(Composite group, int i, String text, int size) {
		final Button button = new Button(group, SWT.RADIO);
		button.setText(text + (size > 0 ? (" (" + size + " cards)") : ""));
		button.setData(i);
		button.setSelection(i == listChoice);
		if (size == 0) {
			button.setEnabled(false);
		}
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					listChoice = (Integer) button.getData();
					updateEstimatedPrice();
				}
			}
		});
		buttonGridData.applyTo(button);
		return button;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return MagicUIActivator.getDefault().getDialogSettings(ID);
	}

	@Override
	protected void okPressed() {
		saveWidgetValues();
		super.okPressed();
	}

	protected void saveWidgetValues() {
		try {
			IDialogSettings dialogSettings = getDialogBoundsSettings();
			//
			dialogSettings.put("listChoice", listChoice);
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void restoreWidgetValues() {
		IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
		// restore file
		try {
			listChoice = dialogSettings.getInt("listChoice");
			if (listChoice != 0) {
				b1.setSelection(false);
				b2.setSelection(false);
				b3.setSelection(false);
				switch (listChoice) {
					case USE_SELECTION:
						b1.setSelection(true);
						break;
					case USE_FILTER:
						b2.setSelection(true);
						break;
					case USE_ALL:
						b3.setSelection(true);
						break;
					default:
						break;
				}
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}
}
