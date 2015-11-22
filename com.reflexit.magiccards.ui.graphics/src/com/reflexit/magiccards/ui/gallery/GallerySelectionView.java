package com.reflexit.magiccards.ui.gallery;

import java.util.Collection;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.IMagicViewer;

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
	protected AbstractMagicCardsListControl doGetViewControl() {
		return new GalleryListControl(this) {
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
				GalleryViewerManager m = new GalleryViewerManager(getPreferencePageId()) {
					@Override
					public Control createContents(Composite parent) {
						Control x = super.createContents(parent);
						((LazyGalleryTreeViewer) getViewer()).setGroupsVisible(false);
						return x;
					}
				};
				m.createContents(parent);
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
		if (!(selection instanceof IStructuredSelection))
			return;
		IStructuredSelection ss = (IStructuredSelection) selection;
		gsstore.getCardStore().removeAll();
		for (Object item : ss.toList()) {
			if (item instanceof ICard) {
				if (item instanceof CardGroup) {
					Collection children = ((CardGroup) item).expand();
					gsstore.addAll(children);
				} else
					gsstore.add((ICard) item);
			}
		}
		gsstore.update();
		control.updateViewer();
	}
}
