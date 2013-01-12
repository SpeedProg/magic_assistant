package com.reflexit.magiccards.ui.views.analyzers;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.TreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class GroupListControl extends AbstractMagicCardsListControl {
	public class GroupTreeManager extends TreeViewerManager implements IDisposable {
		protected GroupTreeManager(String id) {
			super(id);
		}

		@Override
		public Control createContents(Composite parent) {
			Control control = super.createContents(parent);
			hookDragAndDrop();
			// getViewer().setComparator(new CollectorViewerComparator());
			return control;
		}

		@Override
		protected ColumnCollection doGetColumnCollection(String prefPageId) {
			return new MagicColumnCollection(prefPageId) {
				@Override
				protected void createColumns() {
					createCustomColumns(columns);
				}
			};
		}

		protected void createCustomColumns(ArrayList<AbstractColumn> columns) {
			columns.add(new GroupColumn(false, false, false));
			columns.add(new CountColumn());
		}

		@Override
		public void updateColumns(String value) {
			// no update
		}
	}

	/**
	 * Update view in UI thread after data load is finished
	 */
	@Override
	public void updateViewer() {
		if (manager.getControl().isDisposed())
			return;
		ISelection selection = getSelection();
		manager.getViewer().refresh(true);
		if (revealSelection != null) {
			// set desired selection
			getSelectionProvider().setSelection(revealSelection);
			revealSelection = null;
		} else {
			// restore selection
			getSelectionProvider().setSelection(selection);
		}
		updateStatus();
	}

	@Override
	protected void populateStore(IProgressMonitor monitor) {
		// ignore
	}

	public GroupListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	protected IFilteredCardStore<ICard> doGetFilteredStore() {
		return null;// new MemoryFilteredCardStore<ICard>();
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new GroupTreeManager(getPreferencePageId());
	}

	@Override
	protected void updateSortColumn(final int index) {
		if (index >= 0) {
			manager.setSortColumn(index, 0);
		} else {
			manager.setSortColumn(-1, 0);
		}
	}
}
