package com.reflexit.magiccards.ui.jobs;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.xml.PricesXmlStreamWriter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.LoadExtrasDialog;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

public class LoadingPricesJob extends Job {
	private AbstractCardsView view;
	private IStructuredSelection selection;
	private int listChoice;

	public LoadingPricesJob(AbstractCardsView view) {
		this("Loading prices (To view enable Price columns)", view);
	}

	public LoadingPricesJob(String name, AbstractCardsView view) {
		super(name);
		this.view = view;
		setUser(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		int size = 0;
		Iterable<IMagicCard> list = null;
		switch (listChoice) {
			case LoadExtrasDialog.USE_SELECTION:
				size = selection.size();
				list = selection.toList();
				break;
			case LoadExtrasDialog.USE_FILTER:
				size = view.getFilteredStore().getSize();
				list = view.getFilteredStore();
				break;
			case LoadExtrasDialog.USE_ALL:
				size = view.getFilteredStore().getCardStore().size();
				list = view.getFilteredStore().getCardStore();
				break;
			default:
				return Status.CANCEL_STATUS;
		}
		IPriceProvider parser = DataManager.getDBPriceStore().getProvider();
		try {
			IDbCardStore db = DataManager.getCardHandler().getMagicDBStore();
			parser.updateStore(db, list, size, new CoreMonitorAdapter(monitor));
			new PricesXmlStreamWriter().save(db, parser);
			if (view != null)
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						view.refresh();
					}
				});
		} catch (IOException e) {
			return MagicUIActivator.getStatus(e);
		}
		return Status.OK_STATUS;
	}

	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

	public void setListChoice(int listChoice) {
		this.listChoice = listChoice;
	}
}
