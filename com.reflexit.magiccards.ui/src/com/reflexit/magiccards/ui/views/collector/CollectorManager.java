package com.reflexit.magiccards.ui.views.collector;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.LazyTreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class CollectorManager extends LazyTreeViewerManager implements IDisposable {
	protected CollectorManager(String id) {
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
		return new CollectorColumnCollection();
	}
}
