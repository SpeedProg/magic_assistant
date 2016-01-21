package com.reflexit.magiccards.ui.graphics;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.LocationFilteredCardStore;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.dnd.MagicCardDragListener;
import com.reflexit.magiccards.ui.dnd.MagicCardDropAdapter;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckPage;

public class GraphicsDeckPage extends AbstractDeckPage {
	private DesktopCanvas panel;
	private Label status;
	private IFilteredCardStore fstore = new LocationFilteredCardStore();
	private Action refresh;

	@Override
	protected void createPageContents(Composite area) {
		status = createStatusLine(getArea());
		panel = new DesktopCanvas(getArea());
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.refresh = new RefreshAction(this::activate);
		hookDragAndDrop();
		fstore.getFilter().setGroupFields(MagicCardField.CMC);
	}

	protected Label createStatusLine(Composite composite) {
		Label statusLine = new Label(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 3;
		statusLine.setLayoutData(gd);
		statusLine.setText("Status");
		return statusLine;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return panel.getSelectionProvider();
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

	public String getStatusMessage() {
		if (fstore.getSize() > 100) {
			return "Cannot show graphics for " + fstore.getSize() + " cards";
		}
		return "This page is under contruction...";
	}

	@Override
	public void refresh() {
		fstore.setLocation(getCardStore().getLocation());
		fstore.clear();
		fstore.getCardStore().addAll(getCardStore().getCards());
		fstore.update();
		WaitUtils.asyncExec(() -> {
			panel.setInput(fstore);
			panel.forceFocus();
			status.setText(getStatusMessage());
		});
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
