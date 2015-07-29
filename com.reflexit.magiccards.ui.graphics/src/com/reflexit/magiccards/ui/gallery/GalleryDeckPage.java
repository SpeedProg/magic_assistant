package com.reflexit.magiccards.ui.gallery;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.views.TreeViewContentProvider;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckPage;

public class GalleryDeckPage extends AbstractDeckPage {
	private LazyGalleryTreeViewer panel;
	private Label status;
	private IFilteredCardStore fstore = new MemoryFilteredCardStore<>();
	private Action refresh;
	private LabelProvider lp = new LabelProvider() {
		HashMap<IMagicCard, Image> map = new HashMap<>();

		public void dispose() {
			for (Image im : map.values()) {
				im.dispose();
			}
			map.clear();
		}

		public String getText(Object element) {
			if (element instanceof ICardGroup)
				return ((ICardGroup) element).getName();
			if (element instanceof IMagicCard) {
				return ((IMagicCard) element).getName();
			}
			return "";
		};

		public Image getImage(Object element) {
			// System.err.println("getting image for " + element + " " +
			// element.getClass());
			if (element instanceof CardGroup && ((CardGroup) element).getFieldIndex() != MagicCardField.NAME)
				return null;
			if (map.containsKey(element))
				return map.get(element);
			// System.err.println("loaidng");
			new Job("loading card") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						final IMagicCard card = (IMagicCard) element;
						if (card == null) {
							return Status.OK_STATUS;
						}
						String path = ImageCreator.getInstance().createCardPath(card, true, false);
						final Image image = ImageCreator.getInstance().createCardImage(path, false);
						if (image == null) {
							Image it = ImageCreator.getInstance().getCardNotFoundImageTemplate();
							Image itCopy = new Image(Display.getDefault(), it, SWT.IMAGE_COPY);
							map.put(card, itCopy);
						} else {
							map.put(card, image);
						}
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								panel.getViewer().refresh(element, true);
								// System.err.println("setting real image for "
								// + card);
								// item.setImage(image);
								// item.getParent().redraw();
							}
						});
					} catch (CannotDetermineSetAbbriviation e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
			return ImageCreator.getInstance().getCardNotFoundImageTemplate();
		};
	};

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		status = createStatusLine(getArea());
		final LazyGalleryTreeViewer galleryViewer = new LazyGalleryTreeViewer(getArea());
		// getArea().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));

		panel = galleryViewer;
		panel.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		makeActions();
		hookDragAndDrop();
		fstore.getFilter().setNameGroupping(false);
		fstore.getFilter().setGroupFields(MagicCardField.CMC);
		getViewer().setContentProvider(new TreeViewContentProvider<>());
		getViewer().setLabelProvider(lp);
		return getArea();
	}

	protected void makeActions() {
		this.refresh = new Action("Refresh", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
			}

			@Override
			public void run() {
				activate();
			}
		};
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return panel.getSelectionProvider();
	}

	@Override
	public void dispose() {
		lp.dispose();
		panel.getControl().dispose();
		super.dispose();
	}

	public void hookDragAndDrop() {
		//// panel.setDragDetect(true);
		StructuredViewer viewer = getViewer();
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		viewer.addDragSupport(operations, transfers, new MagicCardDragListener(viewer));
		viewer.addDropSupport(operations, transfers, new MagicCardDropAdapter(viewer) {
			@Override
			public boolean performDrop(Object data) {
				boolean did = super.performDrop(data);
				if (did) {
					StructuredSelection sel = new StructuredSelection(Arrays.asList(data));
					getViewer().setSelection(sel, true);
				}
				return did;
			}
		});
	}

	private StructuredViewer getViewer() {
		return panel.getViewer();
	}

	@Override
	public void setFilteredStore(IFilteredCardStore nfstore) {
		super.setFilteredStore(nfstore);
	}

	@Override
	public String getStatusMessage() {
		if (fstore.getSize() > 100) {
			return "Cannot show graphics for " + fstore.getSize() + " cards";
		}
		return "This page is under contruction...";
	}

	@Override
	public void activate() {
		super.activate();
		fstore.setLocation(getCardStore().getLocation());
		fstore.clear();
		fstore.getCardStore().addAll(getCardStore().getCards());
		fstore.update();
		if (fstore.getSize() <= 100) {
			getViewer().setInput(fstore);
			panel.getControl().forceFocus();
		}
		status.setText(getStatusMessage());
	}

	@Override
	public void fillLocalPullDown(IMenuManager viewMenuManager) {
		super.fillLocalPullDown(viewMenuManager);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager toolBarManager) {
		super.fillLocalToolBar(toolBarManager);
		// toolBarManager.add(view.getGroupAction());
		toolBarManager.add(refresh);
	}
}
