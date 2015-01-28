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
package com.reflexit.mtgtournament.ui.tour.dialogs;

import java.io.FileNotFoundException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerList;
import com.reflexit.mtgtournament.core.xml.TournamentManager;
import com.reflexit.mtgtournament.ui.tour.Activator;
import com.reflexit.mtgtournament.ui.tour.views.PlayersListComposite;

public class SelectPlayerDialog extends TrayDialog {
	private PlayersListComposite playersListComposite;
	private Text pinFilter;
	private Text nameFilter;
	private PlayerViewerFilter filter;
	private IStructuredSelection sel;
	private Object input;

	class PlayerViewerFilter extends ViewerFilter {
		String name;
		String pin;

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof Player) {
				Player p = (Player) element;
				if (name != null && name.trim().length() > 0) {
					return p.getName().contains(name);
				}
				if (pin != null && pin.trim().length() > 0) {
					return p.getName().contains(pin);
				}
			}
			return true;
		}
	};

	public SelectPlayerDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		filter = new PlayerViewerFilter();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Find Player");
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		pinFilter = createLabelText(comp, "By PIN:");
		pinFilter.setLayoutData(hor.create());
		pinFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = pinFilter.getText();
				filter.pin = text;
				playersListComposite.getViewer().setFilters(new ViewerFilter[] { filter });
				playersListComposite.getViewer().refresh();
			}
		});
		nameFilter = createLabelText(comp, "By Name:");
		nameFilter.setLayoutData(hor.create());
		nameFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = nameFilter.getText();
				filter.name = text;
				playersListComposite.getViewer().setFilters(new ViewerFilter[] { filter });
				playersListComposite.getViewer().refresh();
			}
		});
		playersListComposite = new PlayersListComposite(comp, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER,
				false);
		playersListComposite.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setSelection((IStructuredSelection) event.getSelection());
				updateButtonsEnablement();
			}
		});
		playersListComposite.setLayoutData(GridDataFactory.fillDefaults().span(2, 0).grab(true, true)
				.hint(SWT.DEFAULT, 300).create());
		playersListComposite.getViewer().setInput(input);
		Button add = new Button(comp, SWT.PUSH);
		add.setText("New Player...");
		add.setLayoutData(hor.create());
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewPlayerDialog dialog = new NewPlayerDialog(getShell());
				if (dialog.open() == Dialog.OK) {
					Player player = new Player(dialog.getPin(), dialog.getName());
					if (input instanceof PlayerList) {
						PlayerList list = ((PlayerList) input);
						list.addPlayer(player);
						try {
							TournamentManager.save(list);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (CoreException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						playersListComposite.getViewer().setInput(list);
						StructuredSelection selection = new StructuredSelection(player);
						setSelection(selection);
						playersListComposite.getViewer().setSelection(selection);
					}
				}
			}
		});
		return comp;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control con = super.createContents(parent);
		updateButtonsEnablement();
		return con;
	}

	/**
	 * 
	 */
	protected void updateButtonsEnablement() {
		getButton(IDialogConstants.OK_ID).setEnabled(!(sel == null || sel.isEmpty()));
	}

	public void setInput(Object input) {
		this.input = input;
	}

	protected void setSelection(IStructuredSelection selection) {
		this.sel = selection;
	}

	public Collection<Player> getPlayers() {
		return sel.toList();
	}

	private Text createLabelText(Composite comp, String string) {
		Label label = new Label(comp, SWT.NONE);
		label.setText(string);
		Text text = new Text(comp, SWT.BORDER);
		return text;
	}
}
