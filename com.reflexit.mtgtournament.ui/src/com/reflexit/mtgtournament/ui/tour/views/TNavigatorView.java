/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.mtgtournament.ui.tour.views;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.mtgtournament.core.model.Cube;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.xml.TournamentManager;
import com.reflexit.mtgtournament.ui.tour.Activator;

/**
 */
public class TNavigatorView extends ViewPart {
	public static final String ID = TNavigatorView.class.getName();
	private TableViewer viewer;
	private Action addTour;
	private Action deleteTour;
	private Action sortAlpha;
	private Action doubleClickAction;
	private Cube cube;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (cube == null)
				return new Object[0];
			List list = cube.getTournamens();
			return list.toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			return Activator.getDefault().getImage("icons/tour_icon_16.png");
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public TNavigatorView() {
	}

	private ViewerComparator alphaSorter = new ViewerComparator();
	private ViewerComparator dateSorter = null;

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setComparator(alphaSorter);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		getViewSite().setSelectionProvider(viewer);
		createModel();
		viewer.setInput(cube);
		// we have at least one
		Tournament t = cube.getTournamens().get(cube.getTournamens().size() - 1);
		viewer.setSelection(new StructuredSelection(t));
	}

	void createModel() {
		cube = null;
		try {
			cube = TournamentManager.getCube();
		} catch (Exception e) {
			showMessage("Cannot load: " + e.getMessage());
			try {
				cube = TournamentManager.getCube();
			} catch (Exception e1) {
				// no
			}
		}
		Tournament t = new Tournament("Tournament 1");
		cube.addTournament(t);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TNavigatorView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		//manager.add(addTour);
		//manager.add(deleteTour);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addTour);
		manager.add(deleteTour);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(addTour);
		manager.add(deleteTour);
		manager.add(sortAlpha);
	}

	private void makeActions() {
		addTour = new Action() {
			@Override
			public void run() {
				addTournament();
			}
		};
		addTour.setText("New Tournament");
		addTour.setToolTipText("Add a New Tournament");
		addTour.setImageDescriptor(Activator.getImageDescriptor("icons/add.gif"));
		deleteTour = new Action() {
			@Override
			public void run() {
				deleteTournaments();
			}
		};
		deleteTour.setText("Delete Tournament");
		deleteTour.setToolTipText("Delete selected tournaments");
		deleteTour.setImageDescriptor(Activator.getImageDescriptor("icons/delete.gif"));
		sortAlpha = new Action("Sort Aphabetically", Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Activator.getImageDescriptor("icons/sort.gif"));
			}

			@Override
			public void run() {
				if (sortAlpha.isChecked())
					viewer.setComparator(alphaSorter);
				else
					viewer.setComparator(dateSorter);
			}
		};
		doubleClickAction = new Action() {
			@Override
			public void run() {
				try {
					getSite().getWorkbenchWindow().getActivePage().showView(TournamentView.ID);
				} catch (PartInitException e) {
					showMessage(e.getMessage());
				}
			}
		};
	}

	/**
	 * 
	 */
	protected void deleteTournaments() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty())
			return;
		boolean delQuestion = MessageDialog.openQuestion(viewer.getControl().getShell(), "Confirmation",
				"You are deleting " + selection.size() + " tournament(s): " + selection + ". Proceed?");
		if (delQuestion) {
			for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
				Tournament t = (Tournament) iterator.next();
				cube.removeTournament(t);
				try {
					TournamentManager.remove(t);
					viewer.refresh(true);
				} catch (Exception e) {
					showMessage(e.getMessage());
				}
			}
		}
	}

	/**
	 * 
	 */
	protected void addTournament() {
		InputDialog dialog = new InputDialog(getSite().getShell(), "Toutnament name",
				"Enter a tournament name", "",
				null);
		if (dialog.open() == Dialog.OK) {
			Tournament t = new Tournament(dialog.getValue());
			cube.addTournament(t);
			try {
				TournamentManager.save(t);
				viewer.refresh(true);
			} catch (Exception e) {
				showMessage(e.getMessage());
			}
		}
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Sample View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}