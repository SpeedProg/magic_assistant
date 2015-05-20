package com.reflexit.magiccards.ui.jobs;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.LoadExtrasDialog;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

public class LoadingExtraJob extends Job {
	private AbstractCardsView view;
	private Set<ICardField> fields;
	private IStructuredSelection selection;
	private int listChoice;
	private String lang;

	public LoadingExtraJob(AbstractCardsView view) {
		this("Loading extra fields", view);
	}

	public LoadingExtraJob(String name, AbstractCardsView view) {
		super(name);
		this.view = view;
		setUser(true);
	}

	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

	public void setListChoice(int listChoice) {
		this.listChoice = listChoice;
	}

	public void setFields(Set<ICardField> set) {
		this.fields = set;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			int size = 0;
			Iterator list = null;
			switch (listChoice) {
				case LoadExtrasDialog.USE_SELECTION:
					size = selection.size();
					list = selection.iterator();
					break;
				case LoadExtrasDialog.USE_FILTER:
					size = view.getFilteredStore().getSize();
					list = view.getFilteredStore().iterator();
					break;
				case LoadExtrasDialog.USE_ALL:
					size = view.getFilteredStore().getCardStore().size();
					list = view.getFilteredStore().getCardStore().iterator();
					break;
				default:
					return Status.CANCEL_STATUS;
			}
			IDbCardStore<IMagicCard> db = DataManager.getInstance().getMagicDBStore();
			UpdateCardsFromWeb parser = new UpdateCardsFromWeb();
			// parser.set
			parser.updateStore(list, size, fields, lang, db, new CoreMonitorAdapter(monitor));
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					view.reloadData();
				}
			});
		} catch (IOException e) {
			MagicUIActivator.log(e);
		} catch (MagicException e) {
			return new Status(IStatus.ERROR, MagicUIActivator.PLUGIN_ID, e.getMessage());
		}
		return Status.OK_STATUS;
	}

	public void setLanguage(String language) {
		this.lang = language;
	}
}
