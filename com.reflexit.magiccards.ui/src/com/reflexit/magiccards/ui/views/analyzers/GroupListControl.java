package com.reflexit.magiccards.ui.views.analyzers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.ExtendedTreeViewer;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public abstract class GroupListControl extends AbstractMagicCardsListControl {
	public static class GroupTreeViewer extends ExtendedTreeViewer {
		protected GroupTreeViewer(String id, Composite parent) {
			super(parent, id);
			// hookDragAndDrop();
		}

		@Override
		protected ColumnCollection doGetColumnCollection(String prefPageId) {
			return new MagicColumnCollection(null) {
				@Override
				protected void createColumns(List<AbstractColumn> columns) {
					createCustomColumns(columns);
				}
			};
		}

		protected void createCustomColumns(List<AbstractColumn> columns) {
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
	public void refreshViewer() {
		if (viewer.getControl().isDisposed())
			return;
		ISelection selection = getSelection();
		viewer.getViewer().refresh();
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

	public GroupListControl() {
		super(Presentation.TREE);
	}

	@Override
	protected IFilteredCardStore<ICard> doGetFilteredStore() {
		return new MemoryFilteredCardStore<>();
	}

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		return new GroupTreeViewer(getPreferencePageId(), parent);
	}

	@Override
	public void saveColumnLayout() {
	}

	@Override
	protected void updateSortColumn(final int index) {
		if (viewer instanceof IMagicColumnViewer) {
			IMagicColumnViewer cviewer = (IMagicColumnViewer) viewer;
			if (index >= 0) {
				cviewer.setSortColumn(index, 0);
			} else {
				cviewer.setSortColumn(-1, 0);
			}
		}
	}
}
