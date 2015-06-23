package com.reflexit.magiccards.ui.jobs;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;

public class LoadingPricesJob extends Job {
	private Iterable list;

	public LoadingPricesJob(Iterable list) {
		this("Loading prices (To view enable Online Price column)", list);
	}

	public LoadingPricesJob(String name, Iterable list) {
		super(name);
		this.list = list;
		setUser(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IPriceProvider parser = DataManager.getDBPriceStore().getProvider();
		try {
			parser.updatePricesAndSync(list, new CoreMonitorAdapter(monitor));
		} catch (IOException e) {
			return MagicUIActivator.getStatus(e);
		} catch (MagicException e) {
			return MagicUIActivator.getStatus(e);
		}
		return Status.OK_STATUS;
	}
}
