package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.TreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
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
		public void hookDragAndDrop() {
			this.getViewer().getControl().setDragDetect(true);
			int ops = DND.DROP_COPY | DND.DROP_MOVE;
			Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
			getViewer().addDragSupport(ops, transfers, new MagicCardDragListener(getViewer()));
		}

		@Override
		protected ColumnCollection doGetColumnCollection(String prefPageId) {
			// return super.doGetColumnCollection(prefPageId);
			return new GroupTreeColumnCollection();
		}

		@Override
		public void updateColumns(String value) {
			// no update
		}
	}

	public class GroupTreeColumnCollection extends MagicColumnCollection {
		public GroupTreeColumnCollection() {
			super(null);
		}

		@Override
		protected void createColumns() {
			super.createColumns();
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
