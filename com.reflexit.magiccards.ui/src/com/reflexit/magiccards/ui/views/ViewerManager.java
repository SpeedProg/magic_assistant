package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.model.SortOrderViewerComparator;
import com.reflexit.magiccards.ui.widgets.ContextFocusListener;

public class ViewerManager implements IDisposable {
	private SortOrderViewerComparator vcomp = new SortOrderViewerComparator();
	private ColumnCollection collumns;
	private IColumnSortAction sortAction;
	protected MenuManager menuManager;
	protected Viewer viewer;

	public ViewerManager(Viewer viewer, ColumnCollection columns) {
		this.collumns = columns;
		this.viewer = viewer;
	}

	public void setCollumns(ColumnCollection collumns) {
		this.collumns = collumns;
	}

	@Override
	public void dispose() {
		// override to dispose resources
		if (menuManager != null)
			menuManager.dispose();
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId);
	}

	public AbstractColumn getColumn(int i) {
		return collumns.getColumn(i);
	}

	public ColumnCollection getColumnsCollection() {
		return collumns;
	}

	protected int getColumnsNumber() {
		return collumns.getColumnsNumber();
	}

	public void hookContext(String id) {
		getViewer().getControl().addFocusListener(new ContextFocusListener(id));
	}

	public Viewer getViewer() {
		return viewer;
	};

	public Control getControl() {
		return getViewer().getControl();
	}

	public Shell getShell() {
		return getControl().getShell();
	}

	public interface IContextMenuFiller {
		public void fillContextMenu(IMenuManager manager);
	}

	protected MenuManager hookContextMenu(final IContextMenuFiller filler) {
		if (filler == null)
			return null;
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				filler.fillContextMenu(manager);
			}
		});
		hookContextMenu(menuMgr);
		return menuMgr;
	}

	public boolean hookContextMenu(MenuManager menuMgr) {
		this.menuManager = menuMgr;
		createContentMenu();
		return true;
	}

	protected void createContentMenu() {
		if (getControl().isDisposed())
			return;
		Menu menu = getMenuManager().createContextMenu(getControl());
		getControl().setMenu(menu);
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void hookSortAction(IColumnSortAction sortAction) {
		this.sortAction = sortAction;
	}

	protected final void callSortAction(final int coln, int direction) {
		if (sortAction != null)
			sortAction.sort(coln, direction);
	}

	public void hookDragAndDrop() {
		if (getViewer() instanceof StructuredViewer)
			hookDragAndDrop((StructuredViewer) getViewer());
	}

	public static final void hookDragAndDrop(StructuredViewer viewer) {
		viewer.getControl().setDragDetect(true);
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(ops,
				new Transfer[] { MagicCardTransfer.getInstance(), //
						TextTransfer.getInstance(), //
						PluginTransfer.getInstance() //
		}, new MagicCardDragListener(viewer));
		viewer.addDropSupport(ops,
				new Transfer[] { //
						MagicCardTransfer.getInstance(), //
						PluginTransfer.getInstance() //
		}, new MagicCardDropAdapter(viewer));
	}

	public static Font getFont() {
		return MagicUIActivator.getDefault().getFont();
	}

	protected void openColumnPreferences(String id) {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null);
		dialog.open();
	}

	protected String getPreferencesId() {
		return getColumnsCollection().getId();
	}

	public SortOrderViewerComparator getViewerComparator() {
		return vcomp;
	}
}
