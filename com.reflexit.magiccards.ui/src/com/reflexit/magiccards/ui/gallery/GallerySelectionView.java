package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicViewer;
import com.reflexit.magiccards.ui.views.model.GroupExpandContentProvider;

public class GallerySelectionView extends GalleryView {
	public static final String ID = "com.reflexit.magiccards.ui.gallery.GallerySelectionView";
	protected MemoryFilteredCardStore gsstore;
	private ISelectionListener selectionListener;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewselection");
	}

	public synchronized MemoryFilteredCardStore getGsstore() {
		if (gsstore == null)
			gsstore = new MemoryFilteredCardStore<>();
		return gsstore;
	}

	@Override
	protected AbstractMagicCardsListControl createViewControl() {
		return new GalleryListControl() {
			@Override
			protected String getPreferencePageId() {
				return getViewPreferencePageId();
			}

			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return getGsstore();
			}

			@Override
			public void fillLocalToolBar(IToolBarManager manager) {
				// no action on toolbar
			}

			@Override
			public void fillLocalPullDown(IMenuManager manager) {
				// no actions on toolbar
			}

			@Override
			public IMagicViewer createViewer(Composite parent) {
				GallerySimpleViewer m = new GallerySimpleViewer(parent, getPreferencePageId());
				return m;
			}
		};
	}

	@Override
	public String getPreferencePageId() {
		return null;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		ISelectionService s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		selectionListener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (part.getSite().getId().equals(ID))
					return;
				setDetails(selection);
			}
		};
		s.addPostSelectionListener(selectionListener);
	}

	@Override
	public void dispose() {
		ISelectionService s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		s.removePostSelectionListener(selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ISelectionService s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = s.getSelection();
		setDetails(selection);
	}

	public void setDetails(ISelection selection) {
		if (!(selection instanceof IStructuredSelection) || selection.isEmpty())
			return;
		IStructuredSelection ss = (IStructuredSelection) selection;
		((AbstractMagicCardsListControl) getMagicControl()).refreshViewer();
		gsstore.getCardStore().removeAll();
		Object[] children = new GroupExpandContentProvider().calculateChildren(ss.toList());
		for (Object item : children) {
			if (item instanceof ICard) {
				gsstore.add((ICard) item);
			}
		}
		gsstore.update();
		((AbstractMagicCardsListControl) getMagicControl()).refreshViewer();
	}
}
