package com.reflexit.magiccards.ui.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;
import com.reflexit.magiccards.core.sync.Currency;
import com.reflexit.magiccards.core.sync.ParseGathererSets;
import com.reflexit.magiccards.core.sync.ParseSetLegality;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;

public class CheckForUpdateDbHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doCheckForCardUpdates();
		return null;
	}

	public static void doCheckForCardUpdates() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor imonitor) throws InvocationTargetException, InterruptedException {
				CoreMonitorAdapter monitor = new CoreMonitorAdapter(imonitor);
				monitor.beginTask("Checking for cards updates...", 110);
				try {
					ICardHandler handler = DataManager.getCardHandler();
					ParseGathererSets setsLoader = new ParseGathererSets();
					setsLoader.load(new SubCoreProgressMonitor(monitor, 10));
					ParseSetLegality.loadAllFormats(new SubCoreProgressMonitor(monitor, 10));
					Collection<Edition> newSets = setsLoader.getNew();
					if (newSets.size() > 0) {
						Editions.getInstance().save();
						if (MessageDialog.openQuestion(null, "New Cards", "New sets are available: " + newSets
								+ ". Would you like to download them now?")) {
							int k = newSets.size();
							for (Iterator iterator = newSets.iterator(); iterator.hasNext();) {
								Edition edition = (Edition) iterator.next();
								handler.downloadUpdates(edition.getName(), new Properties(), new SubCoreProgressMonitor(monitor, 60 / k));
							}
						}
					}
					Currency.update();
				} catch (Exception e) {
					MagicUIActivator.log(e); // move on if exception via set loading
				}
				monitor.done();
			}
		};
		try {
			new ProgressMonitorDialog(null).run(false, true, runnable);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		setBaseEnabled(true);
	}
}
