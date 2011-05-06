package com.reflexit.magiccards.ui.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.EditionsFilterPreferencePage;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public abstract class ViewerManager extends MagicColumnCollection implements IDisposable {
	protected MagicCardFilter filter;
	private IFilteredCardStore mhandler;
	private IPreferenceStore store;
	protected AbstractCardsView view;
	private String statusMessage;
	private Job loadingJob;

	protected ViewerManager(IPreferenceStore store, String viewId) {
		super(viewId);
		this.filter = new MagicCardFilter();
		this.store = store;
	}

	public IPreferenceStore getPreferenceStore() {
		return this.store;
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	public void setFilteredCardStore(IFilteredCardStore store) {
		this.mhandler = store;
	}

	void asyncUpdateViewer(Display display) {
		display.syncExec(new Runnable() {
			public void run() {
				updateViewer();
			}
		});
	}

	public abstract void updateViewer();

	void checkInit() {
		try {
			DataManager.getCardHandler().loadInitialIfNot(new NullProgressMonitor());
			// DataManager.getCardHandler().getMagicCardHandler().getTotal();
		} catch (MagicException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void updateFilter() {
		HashMap map = storeToMap();
		this.filter.update(map);
		this.filter.setOnlyLastSet(getPreferenceStore().getBoolean(EditionsFilterPreferencePage.LAST_SET));
	}

	private HashMap storeToMap() {
		IPreferenceStore store = getPreferenceStore();
		HashMap map = new HashMap();
		Collection col = FilterHelper.getAllIds();
		for (Iterator iterator = col.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			String value = store.getString(id);
			if (value != null && value.length() > 0) {
				map.put(id, value);
				// System.err.println(id + "=" + value);
			}
		}
		return map;
	}

	public abstract Control createContents(Composite parent);

	public Shell getShell() {
		return getViewer().getControl().getShell();
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	private Object jobFamility = new Object();
	private ISchedulingRule jobRule = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	public void loadData(final Runnable postLoad) {
		Job[] jobs = Job.getJobManager().find(jobFamility);
		if (jobs.length >= 2) {
			// System.err.println(jobs.length +
			// " already running skipping refresh");
			return;
		}
		final Display display = PlatformUI.getWorkbench().getDisplay();
		loadingJob = new Job("Loading cards") {
			@Override
			public boolean belongsTo(Object family) {
				return family == jobFamility;
			}

			@Override
			public boolean shouldSchedule() {
				Job[] jobs = Job.getJobManager().find(jobFamility);
				if (jobs.length >= 2)
					return false;
				return super.shouldSchedule();
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (jobFamility) {
					try {
						setName("Loading cards");
						checkInit();
						if (getFilteredStore() == null) {
							setFilteredCardStore(view.doGetFilteredStore());
						}
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						monitor.subTask("Loading cards...");
						updateStore(monitor);
						updateFilter();
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						getFilteredStore().update(ViewerManager.this.filter);
					} catch (final Exception e) {
						display.syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(display.getActiveShell(), "Error", e.getMessage());
							}
						});
						MagicUIActivator.log(e);
						return Status.OK_STATUS;
					}
					// asyncUpdateViewer();
					return Status.OK_STATUS;
				}
			}
		};
		// loadingJob.setRule(jobRule);
		loadingJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (postLoad != null)
					display.syncExec(postLoad);
				else
					asyncUpdateViewer(display);
				super.done(event);
			}
		});
		loadingJob.schedule(100);
	}

	protected void updateStore(IProgressMonitor monitor) {
		// do nothing here
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
		loadData(null);
	}

	public abstract void updateSortColumn(int index);

	/**
	 * @param indexCmc
	 */
	public void updateGroupBy(ICardField field) {
		ICardField oldIndex = this.filter.getGroupField();
		if (oldIndex == field)
			return;
		if (field != null)
			filter.setSortField(field, true);
		this.filter.setGroupField(field);
	}

	/**
	 * @param doubleClickListener
	 */
	public void addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getViewer().addDoubleClickListener(doubleClickListener);
	}

	/**
	 * @param menuMgr
	 */
	public void hookContextMenu(MenuManager menuMgr) {
		Menu menu = menuMgr.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
	}

	/**
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return getViewer();
	}

	/**
	 * @return
	 * @return
	 */
	public MagicCardFilter getFilter() {
		return this.filter;
	}

	protected void setStatus(String string) {
		this.view.setStatus(string);
	}

	protected void updateTableHeader() {
		// to be overriden
	}

	public void updateStatus() {
		statusMessage = getStatusMessage();
	}

	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		String cardCountTotal = "";
		int filSize = filteredStore.getSize();
		int totalSize = cardStore.size();
		if (totalSize == 0)
			return "";
		int diff = totalSize - filSize;
		int count = totalSize;
		if (cardStore instanceof ICardCountable) {
			count = ((ICardCountable) cardStore).getCount();
		}
		cardCountTotal = "Total " + count + " cards, shown unique " + filSize + " of " + totalSize;
		String res = cardCountTotal;
		return res;
	}

	/**
	 * 
	 */
	public void shuffle() {
		sort(-2);
	}

	public void addDragAndDrop() {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
		getViewer().addDropSupport(ops, transfers, new MagicCardDropAdapter(getViewer(), this.view));
	}

	public Control getControl() {
		return getViewer().getControl();
	}
}
