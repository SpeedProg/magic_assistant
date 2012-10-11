package com.reflexit.magiccards.ui.graphics;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckPage;

public class GraphicsDeckPage extends AbstractDeckPage {
	private DesktopCanvas panel;
	private Label status;
	private IFilteredCardStore fstore;
	private Action refresh;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		status = createStatusLine(getArea());
		panel = new DesktopCanvas(getArea());
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.refresh = new Action("Refresh", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
			}

			@Override
			public void run() {
				activate();
			}
		};
		panel.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				view.getSite().getSelectionProvider().setSelection(sel);
			}
		});
		hookDragAndDrop();
		return getArea();
	}

	@Override
	public void dispose() {
		panel.dispose();
		super.dispose();
	}

	public void hookDragAndDrop() {
		// panel.setDragDetect(true);
		StructuredViewer viewer = getViewer();
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MagicCardTransfer.getInstance() };
		viewer.addDragSupport(operations, transfers, new MagicCardDragListener(viewer));
		viewer.addDropSupport(operations, transfers, new MagicCardDropAdapter(viewer) {
			@Override
			public boolean performDrop(Object data) {
				boolean did = super.performDrop(data);
				if (did) {
					IMagicCard[] toDropArray = (IMagicCard[]) data;
					if (toDropArray.length == 1) {
						IMagicCard single = toDropArray[0];
						DesktopFigure desktop = panel.getDesktop();
						fstore.update();
						single = (IMagicCard) fstore.getCardStore().getCard(single.getCardId());
						CardFigure figure = desktop.addNewFigureIfNotFound(single);
						Point control = panel.toControl(curEvent.x, curEvent.y);
						figure.setLocation(control.x, control.y);
					}
				}
				return did;
			}
		});
	}

	private StructuredViewer getViewer() {
		return panel.getViewer();
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		this.fstore = store;
	}

	@Override
	public String getStatusMessage() {
		return "This page is under contruction...";
	}

	@Override
	public void activate() {
		super.activate();
		panel.setInput(fstore);
		panel.forceFocus();
		status.setText(getStatusMessage());
	}

	@Override
	protected void fillLocalPullDown(IMenuManager viewMenuManager) {
		super.fillLocalPullDown(viewMenuManager);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager toolBarManager) {
		super.fillLocalToolBar(toolBarManager);
		toolBarManager.add(view.getGroupAction());
		toolBarManager.add(refresh);
	}
}
