package com.reflexit.magiccards.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
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
import org.eclipse.ui.services.IDisposable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public abstract class ViewerManager extends ColumnCollection implements IDisposable {
	protected MagicCardFilter filter;
	private IFilteredCardStore mhandler;
	private IPreferenceStore store;
	protected AbstractCardsView view;
	private String statusMessage;

	protected ViewerManager(IFilteredCardStore handler, IPreferenceStore store, String viewId) {
		super(viewId);
		this.filter = new MagicCardFilter();
		this.mhandler = handler;
		this.store = store;
	}

	public IPreferenceStore getPreferenceStore() {
		return this.store;
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	void asyncUpdateViewer(Display display) {
		display.syncExec(new Runnable() {
			public void run() {
				updateViewer();
			}
		});
	}

	protected abstract void updateViewer();

	void checkInit() {
		try {
			DataManager.getCardHandler().loadInitialIfNot(new NullProgressMonitor());
			//DataManager.getCardHandler().getMagicCardHandler().getTotal();
		} catch (MagicException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void updateFilter() {
		HashMap map = storeToMap();
		this.filter.update(map);
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
				//System.err.println(id + "=" + value);
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

	public void loadData(final Runnable postLoad) {
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
				if (postLoad != null)
					display.syncExec(postLoad);
				else
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
		loadData(null);
	}

	public abstract void updateSortColumn(int index);

	/**
	 * @param indexCmc
	 */
	public void updateGroupBy(ICardField fieldIndex) {
		this.filter.setGroupField(fieldIndex);
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
	}

	public void updateStatus() {
		statusMessage = getStatusMessage();
	}

	public String getStatusMessage() {
		ICardStore cardStore = getFilteredStore().getCardStore();
		String cardCountTotal = "";
		int filSize = getFilteredStore().getSize();
		int totalSize = cardStore.size();
		if (totalSize == 0)
			return "";
		int diff = totalSize - filSize;
		if (cardStore instanceof ICardCountable) {
			int count = ((ICardCountable) cardStore).getCount();
			if (count == totalSize) {
				cardCountTotal = "Cards: " + count;
			} else {
				cardCountTotal = "Cards: " + count + ", unique cards " + totalSize;
			}
		} else {
			cardCountTotal = "Shown Cards: " + filSize;
		}
		String diffStr = "";
		if (diff > 0) {
			diffStr = " (filtered " + diff + ")";
		}
		String res = cardCountTotal + diffStr;
		return res;
	}

	/**
	 * 
	 */
	public void shuffle() {
		sort(-2);
	}

	public void addDargAndDrop() {
		this.getViewer().getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer(), this.view));
		getViewer().addDropSupport(ops, transfers, new MagicCardDropAdapter(getViewer(), this.view));
	}

	public Control getControl() {
		return getViewer().getControl();
	}
}
