package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.preferences.CollectorViewPreferencePage;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.LazyTreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class GroupListControl extends AbstractMagicCardsListControl {
	public class GroupTreeManager extends LazyTreeViewerManager implements IDisposable {
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
	}

	public class GroupTreeColumnCollection extends MagicColumnCollection {
		public GroupTreeColumnCollection() {
			super(CollectorViewPreferencePage.class.getName());
		}

		@Override
		protected void createColumns() {
			// super.createColumns();
			this.columns.add(createGroupColumn());
			this.columns.add(new CountColumn());
		}

		@Override
		protected GroupColumn createGroupColumn() {
			return new GroupColumn() {
				@Override
				public String getText(Object element) {
					if (element instanceof CardGroup) {
						return ((CardGroup) element).getName();
					}
					return super.getText(element);
				}
			};
		}
	}

	public GroupListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new GroupTreeManager(getPreferencePageId());
	}
}
