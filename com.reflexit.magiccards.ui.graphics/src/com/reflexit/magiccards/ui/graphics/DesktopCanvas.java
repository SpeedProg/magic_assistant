package com.reflexit.magiccards.ui.graphics;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class DesktopCanvas extends ScrollableCanvas {
	private DesktopFigure desktop;
	private StructuredViewer viewer;

	public DesktopCanvas(Composite parent) {
		super(parent, null);
		desktop = new DesktopFigure(this);
		viewer = new StructuredViewer() {
			@Override
			public Control getControl() {
				return DesktopCanvas.this;
			}

			@Override
			protected void setSelectionToWidget(List l, boolean reveal) {
				getSelectionProvider().setSelection(new StructuredSelection(l));
			}

			@Override
			public void reveal(Object element) {
				// TODO Auto-generated method stub
			}

			@Override
			protected void internalRefresh(Object element) {
				DesktopCanvas.this.redraw();
			}

			@Override
			protected List getSelectionFromWidget() {
				return ((IStructuredSelection) getSelectionProvider().getSelection()).toList();
			}

			@Override
			protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
				// TODO Auto-generated method stub
			}

			@Override
			protected Widget doFindItem(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected Widget doFindInputItem(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getInput() {
				return desktop.getInput();
			}
		};
		setDragDetect(false);
	}

	/**
	 * Translate mouse event coordinates to relative of receiver
	 * 
	 * @param e
	 * @return
	 */
	protected Point translate(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		return p;
	}

	@Override
	public void mouseMove(MouseEvent e) {
		if ((e.stateMask & SWT.BUTTON_MASK & SWT.BUTTON1) != 0 && !isDragCanvas()) {
			Point p = translate(e);
			desktop.mouseDrag(p);
			Rectangle bounds = getBounds();
			if (!bounds.contains(e.x, e.y)) {
				e.button = 1;
				e.stateMask = 0;
				e.count = 1;
				desktop.mouseStopDrag(p);
				dragDetect(e);
				return;
			}
		}
		super.mouseMove(e);
	}

	@Override
	public void mouseExit(MouseEvent e) {
		super.mouseExit(e);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button == 1) {
			// System.err.println(e);
			Point p = translate(e);
			if (desktop.mouseStartDrag(p)) {
				setDragCanvas(false); // desktop will process the following move & up event itself
			} else {
				setDragCanvas(true);
			}
		}
		super.mouseDown(e);
	}

	@Override
	public void mouseUp(MouseEvent e) {
		if (!isDragCanvas() && e.button == 1) {
			Point p = translate(e);
			desktop.mouseStopDrag(p);
		}
		super.mouseUp(e);
	}

	public void setInput(IFilteredCardStore store) {
		desktop.setInput(store);
	}

	@Override
	public void resize() {
		if (desktop != null)
			desktop.resize();
		else
			super.resize();
	}

	public ISelectionProvider getSelectionProvider() {
		return desktop;
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	public DesktopFigure getDesktop() {
		return desktop;
	}

	@Override
	public void dispose() {
		desktop.dispose();
		super.dispose();
	}
}
