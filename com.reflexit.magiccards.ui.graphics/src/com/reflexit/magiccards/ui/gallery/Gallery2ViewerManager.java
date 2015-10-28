package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.RootTreeViewContentProvider;
import com.reflexit.magiccards.ui.views.TreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class Gallery2ViewerManager extends TreeViewerManager {
	protected LazyGalleryTreeViewer galleryviewer;

	public Gallery2ViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		// Composite comp = new Composite(parent, SWT.NONE);
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		Composite comp = form;
		super.createContents(comp);
		comp.setLayout(new FillLayout());
		// this.viewer.setContentProvider(new LazyTreeViewContentProvider());
		this.viewer.setContentProvider(new RootTreeViewContentProvider());
		this.viewer.setAutoExpandLevel(2);
		this.viewer.getTree().setHeaderVisible(false);
		this.viewer.getTree().setLayoutData(null);
		this.galleryviewer = new LazyGalleryTreeViewer(comp);
		this.galleryviewer.getControl().setFont(getFont());
		this.galleryviewer.setContentProvider(new GroupExpandContentProvider());
		this.galleryviewer.setLabelProvider(new MagicCardImageLabelProvider(galleryviewer));
		this.galleryviewer.setGroupsVisible(false);
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (!ssel.isEmpty())
					galleryviewer.setInput(ssel.toList());
			}
		});
		form.setWeights(new int[] { 22, 78 });
		return comp;
	}

	@Override
	public void updateColumns(String value) {
		for (AbstractColumn col : getColumnsCollection().getColumns()) {
			col.setVisible(false);
		}
		syncColumns();
	}

	@Override
	protected void createFillerColumn() {
		// ignore
		// super.createFillerColumn();
	}

	@Override
	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId) {
			@Override
			protected GroupColumn createGroupColumn() {
				return new GroupColumn(true, true, false);
			}
		};
	}

	@Override
	public void dispose() {
		super.dispose();
		this.galleryviewer.getLabelProvider().dispose();
		this.galleryviewer.getControl().dispose();
		this.galleryviewer = null;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return galleryviewer;
	}

	@Override
	public ColumnViewer getViewer() {
		return super.getViewer();
	}

	public ColumnViewer getColumnViewer() {
		return super.getViewer();
	}

	public StructuredViewer getStructuredViewer() {
		return galleryviewer;
	}

	@Override
	public void hookDoubleClickListener(IDoubleClickListener doubleClickListener) {
		getStructuredViewer().addDoubleClickListener(doubleClickListener);
	}

	@Override
	protected void createContentMenu() {
		Control control = getStructuredViewer().getControl();
		control.setMenu(getMenuManager().createContextMenu(control));
	}

	@Override
	protected void hookMenuDetect(Tree tcontrol) {
		// ignore
	}

	@Override
	public void hookDragAndDrop() {
		super.hookDragAndDrop(getStructuredViewer());
	}

	@Override
	public void setSortColumn(int index, int direction) {
		super.setSortColumn(index, direction);
		getStructuredViewer().setComparator(getViewer().getComparator());
	}

	@Override
	public void updateViewer(Object input) {
		if (viewer == null || this.viewer.getControl().isDisposed())
			return;
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		viewer.setSelection(new StructuredSelection());
		super.updateViewer(input);
		if (galleryviewer == null || this.galleryviewer.getControl().isDisposed())
			return;
		if (input instanceof IFilteredCardStore) {
			if (selection.isEmpty()) {
				IFilteredCardStore fstore = (IFilteredCardStore) input;
				ICardGroup group = fstore.getCardGroupRoot();
				selection = new StructuredSelection(group);
				viewer.setSelection(selection, true);
			} else {
				viewer.setSelection(selection, true);
			}
			galleryviewer.refresh(true);
		}
	}
}
