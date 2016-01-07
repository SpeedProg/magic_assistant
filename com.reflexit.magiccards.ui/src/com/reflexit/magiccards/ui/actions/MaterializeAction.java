package com.reflexit.magiccards.ui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardReconcileDialog;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;

public class MaterializeAction extends org.eclipse.jface.action.Action {
	private ICardStore store;

	public MaterializeAction() {
		super("Materialize...");
		setToolTipText("Attempt to materialize a deck/collection by replacing"
				+ " all virtual cards with own cards from inventory");
	}

	public MaterializeAction(ICardStore store) {
		this();
		this.store = store;
	}

	@Override
	public void run() {
		runMaterialize();
	}

	protected void runMaterialize() {
		Collection<IMagicCard> orig = getCardStore().getCards();
		LocationPickerDialog locationPickerDialog = new LocationPickerDialog(getShell(), SWT.SINGLE | SWT.READ_ONLY) {
			@Override
			protected Control createDialogArea(Composite parent) {
				Control area = super.createDialogArea(parent);
				setMessage("Pick collection(s) from which you want to pull cards to materialize this deck");
				return area;
			}
		};
		if (locationPickerDialog.open() == Window.OK) {
			List<CardCollection> collections = locationPickerDialog.getSelectedCardCollections();
			ArrayList<ICardStore<IMagicCard>> stores = new ArrayList<>();
			for (CardCollection collection : collections) {
				if (collection.getStore().equals(getCardStore()))
					continue;
				stores.add(collection.getStore());
			}
			Collection<MagicCardPhysical> res = DataManager.getInstance().materialize(orig, stores);
			CardReconcileDialog cardReconcileDialog = new CardReconcileDialog(getShell()) {
				@Override
				protected void okPressed() {
					ICardStore<IMagicCard> cardStore = getCardStore();
					getStorageInfo().setVirtual(false);
					DataManager.getInstance().remove(orig, cardStore);
					DataManager.getInstance().moveCards((Collection<IMagicCard>) elements, cardStore);
					super.okPressed();
				}
			};
			cardReconcileDialog.setBlockOnOpen(false);
			cardReconcileDialog.setInput(res);
			cardReconcileDialog.open();
		}
	}

	private Shell getShell() {
		return MagicUIActivator.getShell();
	}

	private IStorageInfo getStorageInfo() {
		IStorage<IMagicCard> storage = getCardStore().getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}

	public ICardStore getCardStore() {
		return store;
	}
}
