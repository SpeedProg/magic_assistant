package com.reflexit.magiccards.ui.commands;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;
import com.reflexit.magiccards.core.sync.CurrencyConvertor;
import com.reflexit.magiccards.core.sync.ParseGathererSets;
import com.reflexit.magiccards.core.sync.ParseSetLegality;
import com.reflexit.magiccards.core.sync.ParseWikiSets;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;

public class CheckForUpdateDbHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands. ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (WebUtils.isWorkOffline()) {
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					MessageDialog.openInformation(MagicUIActivator.getShell(), "Disabled",
							"Online updates are disabled");
				}
			});
			return null;
		}
		doCheckForCardUpdates();
		return null;
	}

	public static void doCheckForCardUpdates() {
		new Job("Checking for cards updates...") {
			@Override
			public IStatus run(IProgressMonitor imonitor) {
				final CoreMonitorAdapter monitor = new CoreMonitorAdapter(imonitor);
				monitor.beginTask("Checking for cards updates...", 110);
				try {
					final ICardHandler handler = DataManager.getCardHandler();
					ParseGathererSets setsLoader = new ParseGathererSets();
					setsLoader.load(new SubCoreProgressMonitor(monitor, 10));
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					new ParseWikiSets().load(new SubCoreProgressMonitor(monitor, 10));
					ParseSetLegality.loadAllFormats(new SubCoreProgressMonitor(monitor, 10));
					final Collection<Edition> newSets = setsLoader.getNew();
					if (newSets.size() > 0) {
						Editions.getInstance().save();
						final boolean result[] = new boolean[1];
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								if (MessageDialog.openQuestion(null, "New Cards", "New sets are available: "
										+ newSets
										+ ". Would you like to download them now?")) {
									result[0] = true;
								}
							}
						});
						if (result[0]) {
							int k = newSets.size();
							for (Iterator iterator = newSets.iterator(); iterator.hasNext();) {
								Edition edition = (Edition) iterator.next();
								try {
									handler.downloadUpdates(edition.getName(), new Properties(),
											new SubCoreProgressMonitor(
													monitor, 60 / k));
								} catch (MagicException e) {
									MagicUIActivator.log(e);
								} catch (InterruptedException e) {
									monitor.setCanceled(true);
								}
								if (monitor.isCanceled())
									break;
							}
						}
					}
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					CurrencyConvertor.update();
				} catch (Exception e) {
					MagicUIActivator.log(e); // move on if exception via set
												// loading
				}
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.done();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		setBaseEnabled(true);
	}
}
