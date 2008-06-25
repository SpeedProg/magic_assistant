package com.reflexit.magiccards.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IDisposable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class ViewerManager extends ColumnCollection implements IDisposable {
	protected MagicCardFilter filter;
	private IFilteredCardStore mhandler;

	protected ViewerManager(MagicCardFilter filter, IFilteredCardStore handler) {
		this.filter = filter;
		this.mhandler = handler;
	}

	public IPreferenceStore getPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}

	void asyncUpdateViewer(Display display) {
		display.syncExec(new Runnable() {
			public void run() {
				updateViewer();
			}
		});
	}

	protected void updateViewer() {
	}

	void checkInit() {
		try {
			DataManager.getCardHandler().loadInitialIfNot(new NullProgressMonitor());
			//DataManager.getCardHandler().getMagicCardHandler().getTotal();
		} catch (MagicException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void updateFilter() {
		IPreferenceStore store = getPreferenceStore();
		HashMap map = new HashMap();
		Collection col = FilterHelper.getAllIds();
		for (Iterator iterator = col.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			String value = store.getString(id);
			if (value != null && value.length() > 0) {
				map.put(id, value);
				System.err.println(id + "=" + value);
			}
		}
		this.filter.update(map);
	}

	public abstract Control createContents(Composite parent);

	public Shell getShell() {
		return getViewer().getControl().getShell();
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void loadData() {
		updateFilter();
		final Display display = getShell().getDisplay();
		Job job = new Job("Loading cards") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					setName("Initialising database");
					checkInit();
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					setName("Loading cards");
					monitor.subTask("Loading cards...");
					getFilteredStore().update(ViewerManager.this.filter);
				} catch (final Exception e) {
					display.syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(getViewer().getControl().getShell(), "Error", e.getMessage());
						}
					});
					MagicUIActivator.log(e);
					return Status.OK_STATUS;
				}
				//asyncUpdateViewer();
				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				asyncUpdateViewer(display);
				super.done(event);
			}
		});
		job.schedule();
	}

	public abstract ColumnViewer getViewer();

	public IFilteredCardStore getFilteredStore() {
		return this.mhandler;
	}

	public void updateColumns(String newValue) {
		// TODO Auto-generated method stub
	}

	protected void sort(int index) {
		updateSortColumn(index);
		loadData();
	}

	protected abstract void updateSortColumn(int index);
}
